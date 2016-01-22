/**
 *  This file is part of Rizzly.
 *
 *  Rizzly is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Rizzly is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with Rizzly.  If not, see <http://www.gnu.org/licenses/>.
 */

package ast.pass.output.xml.visitor;

import ast.Designator;
import ast.data.Ast;
import ast.data.Namespace;
import ast.data.component.ComponentReference;
import ast.data.component.composition.AsynchroniusConnection;
import ast.data.component.composition.CompUseRef;
import ast.data.component.composition.ComponentUse;
import ast.data.component.composition.EndpointRaw;
import ast.data.component.composition.EndpointSelf;
import ast.data.component.composition.EndpointSub;
import ast.data.component.composition.ImplComposition;
import ast.data.component.composition.Queue;
import ast.data.component.composition.SubCallbacks;
import ast.data.component.composition.SynchroniusConnection;
import ast.data.component.elementary.ImplElementary;
import ast.data.component.hfsm.ImplHfsm;
import ast.data.component.hfsm.StateComposite;
import ast.data.component.hfsm.StateRef;
import ast.data.component.hfsm.StateSimple;
import ast.data.component.hfsm.Transition;
import ast.data.expression.ReferenceExpression;
import ast.data.expression.TypeCast;
import ast.data.expression.binop.And;
import ast.data.expression.binop.BinaryExpression;
import ast.data.expression.binop.BitAnd;
import ast.data.expression.binop.BitOr;
import ast.data.expression.binop.BitXor;
import ast.data.expression.binop.Division;
import ast.data.expression.binop.Equal;
import ast.data.expression.binop.Greater;
import ast.data.expression.binop.GreaterEqual;
import ast.data.expression.binop.Is;
import ast.data.expression.binop.Less;
import ast.data.expression.binop.LessEqual;
import ast.data.expression.binop.LogicAnd;
import ast.data.expression.binop.LogicOr;
import ast.data.expression.binop.Minus;
import ast.data.expression.binop.Modulo;
import ast.data.expression.binop.Multiplication;
import ast.data.expression.binop.NotEqual;
import ast.data.expression.binop.Or;
import ast.data.expression.binop.Plus;
import ast.data.expression.binop.Shl;
import ast.data.expression.binop.Shr;
import ast.data.expression.unop.BitNot;
import ast.data.expression.unop.LogicNot;
import ast.data.expression.unop.Not;
import ast.data.expression.unop.Uminus;
import ast.data.expression.value.AnyValue;
import ast.data.expression.value.ArrayValue;
import ast.data.expression.value.BooleanValue;
import ast.data.expression.value.NamedElementsValue;
import ast.data.expression.value.NamedValue;
import ast.data.expression.value.NumberValue;
import ast.data.expression.value.RecordValue;
import ast.data.expression.value.StringValue;
import ast.data.expression.value.TupleValue;
import ast.data.expression.value.UnionValue;
import ast.data.expression.value.UnsafeUnionValue;
import ast.data.file.RizzlyFile;
import ast.data.function.Function;
import ast.data.function.FunctionProperty;
import ast.data.function.FunctionReference;
import ast.data.function.header.FuncFunction;
import ast.data.function.header.FuncInterrupt;
import ast.data.function.header.FuncQuery;
import ast.data.function.header.FuncSubHandlerEvent;
import ast.data.function.header.FuncSubHandlerQuery;
import ast.data.function.header.Procedure;
import ast.data.function.header.Response;
import ast.data.function.header.Signal;
import ast.data.function.header.Slot;
import ast.data.function.ret.FuncReturnNone;
import ast.data.function.ret.FuncReturnTuple;
import ast.data.function.ret.FunctionReturnType;
import ast.data.function.template.DefaultValueTemplate;
import ast.data.raw.RawComposition;
import ast.data.raw.RawElementary;
import ast.data.raw.RawHfsm;
import ast.data.reference.LinkTarget;
import ast.data.reference.RefCall;
import ast.data.reference.RefIndex;
import ast.data.reference.RefName;
import ast.data.reference.RefTemplCall;
import ast.data.reference.LinkedReferenceWithOffset_Implementation;
import ast.data.reference.TypedReference;
import ast.data.statement.AssignmentSingle;
import ast.data.statement.Block;
import ast.data.statement.CallStmt;
import ast.data.statement.CaseOpt;
import ast.data.statement.CaseOptRange;
import ast.data.statement.CaseOptSimple;
import ast.data.statement.CaseOptValue;
import ast.data.statement.CaseStmt;
import ast.data.statement.ExpressionReturn;
import ast.data.statement.ForStmt;
import ast.data.statement.IfOption;
import ast.data.statement.IfStatement;
import ast.data.statement.MsgPush;
import ast.data.statement.MultiAssignment;
import ast.data.statement.VarDefInitStmt;
import ast.data.statement.VarDefStmt;
import ast.data.statement.VoidReturn;
import ast.data.statement.WhileStmt;
import ast.data.template.Template;
import ast.data.type.TypeReference;
import ast.data.type.base.ArrayType;
import ast.data.type.base.BooleanType;
import ast.data.type.base.EnumElement;
import ast.data.type.base.EnumType;
import ast.data.type.base.FunctionType;
import ast.data.type.base.RangeType;
import ast.data.type.base.StringType;
import ast.data.type.base.TupleType;
import ast.data.type.composed.NamedElement;
import ast.data.type.composed.RecordType;
import ast.data.type.composed.UnionType;
import ast.data.type.composed.UnsafeUnionType;
import ast.data.type.out.AliasType;
import ast.data.type.out.PointerType;
import ast.data.type.out.SIntType;
import ast.data.type.out.UIntType;
import ast.data.type.special.AnyType;
import ast.data.type.special.ComponentType;
import ast.data.type.special.IntegerType;
import ast.data.type.special.NaturalType;
import ast.data.type.special.TypeType;
import ast.data.type.special.VoidType;
import ast.data.type.template.ArrayTemplate;
import ast.data.type.template.RangeTemplate;
import ast.data.type.template.TypeTypeTemplate;
import ast.data.variable.ConstPrivate;
import ast.data.variable.DefaultVariable;
import ast.data.variable.FunctionVariable;
import ast.data.variable.GlobalConstant;
import ast.data.variable.StateVariable;
import ast.data.variable.TemplateParameter;
import ast.meta.SourcePosition;
import ast.pass.output.xml.IdReader;
import ast.visitor.Visitor;
import ast.visitor.VisitorAcceptor;

public class Write implements Visitor {

  private final XmlStreamWriter writer;
  private final IdReader astId;
  private final Visitor idWriter;

  public Write(XmlStreamWriter writer, IdReader astId, Visitor idWriter) {
    this.writer = writer;
    this.astId = astId;
    this.idWriter = idWriter;
  }

  @Override
  public void visit(AliasType object) {
    throw new RuntimeException("not yet implemented");
  }

  @Override
  public void visit(And object) {
    throw new RuntimeException("not yet implemented");
  }

  @Override
  public void visit(AnyType object) {
    node("AnyType", object);
  }

  @Override
  public void visit(AnyValue object) {
    throw new RuntimeException("not yet implemented");
  }

  @Override
  public void visit(ArrayTemplate object) {
    node("ArrayTemplate", object);
  }

  @Override
  public void visit(ArrayType object) {
    throw new RuntimeException("not yet implemented");
  }

  @Override
  public void visit(ArrayValue object) {
    throw new RuntimeException("not yet implemented");
  }

  @Override
  public void visit(MultiAssignment object) {
    writer.beginNode("MultiAssignment");
    object.accept(idWriter);
    object.metadata().accept(this);
    object.left.accept(this);
    object.right.accept(this);
    writer.endNode();
  }

  @Override
  public void visit(AssignmentSingle object) {
    throw new RuntimeException("not yet implemented");
  }

  @Override
  public void visit(AsynchroniusConnection object) {
    throw new RuntimeException("not yet implemented");
  }

  @Override
  public void visit(BitAnd object) {
    throw new RuntimeException("not yet implemented");
  }

  @Override
  public void visit(BitNot object) {
    throw new RuntimeException("not yet implemented");
  }

  @Override
  public void visit(BitOr object) {
    throw new RuntimeException("not yet implemented");
  }

  @Override
  public void visit(BitXor object) {
    throw new RuntimeException("not yet implemented");
  }

  @Override
  public void visit(Block object) {
    writer.beginNode("Block");
    object.accept(idWriter);
    object.metadata().accept(this);
    object.statements.accept(this);
    writer.endNode();
  }

  @Override
  public void visit(BooleanType object) {
    node("Boolean", object);
  }

  @Override
  public void visit(BooleanValue object) {
    writeValueNode(object, "BooleanValue", object.value ? "True" : "False");
  }

  @Override
  public void visit(CallStmt object) {
    writer.beginNode("CallStatement");
    object.accept(idWriter);
    object.metadata().accept(this);
    object.call.accept(this);
    writer.endNode();
  }

  @Override
  public void visit(CaseOpt object) {
    throw new RuntimeException("not yet implemented");
  }

  @Override
  public void visit(CaseOptRange object) {
    throw new RuntimeException("not yet implemented");
  }

  @Override
  public void visit(CaseOptSimple object) {
    throw new RuntimeException("not yet implemented");
  }

  @Override
  public void visit(CaseOptValue object) {
    throw new RuntimeException("not yet implemented");
  }

  @Override
  public void visit(CaseStmt object) {
    throw new RuntimeException("not yet implemented");
  }

  @Override
  public void visit(ComponentType object) {
    throw new RuntimeException("not yet implemented");
  }

  @Override
  public void visit(ComponentReference object) {
    visitTypedReference("ComponentReference", object);
  }

  @Override
  public void visit(ComponentUse object) {
    writer.beginNode("ComponentUse");
    writer.attribute("name", object.getName());
    object.accept(idWriter);
    object.metadata().accept(this);
    object.compRef.accept(this);
    writer.endNode();
  }

  @Override
  public void visit(CompUseRef object) {
    throw new RuntimeException("not yet implemented");
  }

  @Override
  public void visit(GlobalConstant object) {
    visitDefaultVariable("GlobalConstant", object);
  }

  @Override
  public void visit(ConstPrivate object) {
    throw new RuntimeException("not yet implemented");
  }

  @Override
  public void visit(DefaultValueTemplate object) {
    node("DefaultValueTemplate", object);
  }

  @Override
  public void visit(Division object) {
    writeBinaryExpression("Division", object);
  }

  @Override
  public void visit(LinkTarget object) {
    writer.beginNode("LinkTarget");
    writer.attribute("name", object.getName());
    object.accept(idWriter);
    object.metadata().accept(this);
    writer.endNode();
  }

  @Override
  public void visit(EndpointRaw object) {
    throw new RuntimeException("not yet implemented");
  }

  @Override
  public void visit(EndpointSelf object) {
    throw new RuntimeException("not yet implemented");
  }

  @Override
  public void visit(EndpointSub object) {
    throw new RuntimeException("not yet implemented");
  }

  @Override
  public void visit(EnumElement enumElement) {
    throw new RuntimeException("not yet implemented");
  }

  @Override
  public void visit(EnumType enumType) {
    throw new RuntimeException("not yet implemented");
  }

  @Override
  public void visit(Equal object) {
    throw new RuntimeException("not yet implemented");
  }

  @Override
  public void visit(ForStmt object) {
    throw new RuntimeException("not yet implemented");
  }

  @Override
  public void visit(FuncFunction object) {
    visitFunction("Function", object);
  }

  @Override
  public void visit(FuncInterrupt object) {
    throw new RuntimeException("not yet implemented");
  }

  @Override
  public void visit(Procedure object) {
    visitProcedure("Procedure", object);
  }

  @Override
  public void visit(FuncQuery object) {
    throw new RuntimeException("not yet implemented");
  }

  @Override
  public void visit(FunctionReference object) {
    visitTypedReference("FunctionReference", object);
  }

  @Override
  public void visit(Response object) {
    visitFunction("Response", object);
  }

  @Override
  public void visit(FuncReturnNone object) {
    throw new RuntimeException("not yet implemented");
  }

  @Override
  public void visit(FuncReturnTuple object) {
    throw new RuntimeException("not yet implemented");
  }

  @Override
  public void visit(FunctionReturnType object) {
    writer.beginNode("ReturnType");
    object.accept(idWriter);
    object.metadata().accept(this);
    object.type.accept(this);
    writer.endNode();
  }

  @Override
  public void visit(Signal object) {
    visitProcedure("Signal", object);
  }

  @Override
  public void visit(Slot object) {
    visitProcedure("Slot", object);
  }

  @Override
  public void visit(FuncSubHandlerEvent object) {
    throw new RuntimeException("not yet implemented");
  }

  @Override
  public void visit(FuncSubHandlerQuery object) {
    throw new RuntimeException("not yet implemented");
  }

  @Override
  public void visit(FunctionType object) {
    throw new RuntimeException("not yet implemented");
  }

  @Override
  public void visit(FunctionVariable object) {
    writer.beginNode("FunctionVariable");
    writer.attribute("name", object.getName());
    object.accept(idWriter);
    object.metadata().accept(this);
    object.type.accept(this);
    writer.endNode();
  }

  @Override
  public void visit(Greater object) {
    throw new RuntimeException("not yet implemented");
  }

  @Override
  public void visit(GreaterEqual object) {
    throw new RuntimeException("not yet implemented");
  }

  @Override
  public void visit(IfOption object) {
    writer.beginNode("IfOption");
    object.accept(idWriter);
    object.metadata().accept(this);
    object.condition.accept(this);
    object.code.accept(this);
    writer.endNode();
  }

  @Override
  public void visit(IfStatement object) {
    writer.beginNode("IfStatement");
    object.accept(idWriter);
    object.metadata().accept(this);
    object.option.accept(this);
    object.defblock.accept(this);
    writer.endNode();
  }

  @Override
  public void visit(ImplComposition object) {
    throw new RuntimeException("not yet implemented");
  }

  @Override
  public void visit(ImplElementary object) {
    writer.beginNode("Elementary");
    writer.attribute("name", object.getName());
    object.accept(idWriter);
    object.metadata().accept(this);
    object.queue.accept(this);
    encapsulate("interface", object.iface);
    encapsulate("function", object.function);
    encapsulate("entry", object.entryFunc);
    encapsulate("exit", object.exitFunc);
    encapsulate("type", object.type);
    encapsulate("variable", object.variable);
    encapsulate("constant", object.constant);
    encapsulate("component", object.component);
    encapsulate("subCallback", object.subCallback);
    writer.endNode();
  }

  @Override
  public void visit(ImplHfsm object) {
    throw new RuntimeException("not yet implemented");
  }

  @Override
  public void visit(IntegerType object) {
    node("Integer", object);
  }

  @Override
  public void visit(Is object) {
    throw new RuntimeException("not yet implemented");
  }

  @Override
  public void visit(Less object) {
    throw new RuntimeException("not yet implemented");
  }

  @Override
  public void visit(LessEqual object) {
    throw new RuntimeException("not yet implemented");
  }

  @Override
  public void visit(LogicAnd object) {
    throw new RuntimeException("not yet implemented");
  }

  @Override
  public void visit(LogicNot object) {
    throw new RuntimeException("not yet implemented");
  }

  @Override
  public void visit(LogicOr object) {
    throw new RuntimeException("not yet implemented");
  }

  @Override
  public void visit(Minus object) {
    writeBinaryExpression("Minus", object);
  }

  @Override
  public void visit(Modulo object) {
    writeBinaryExpression("Modulo", object);
  }

  @Override
  public void visit(MsgPush object) {
    throw new RuntimeException("not yet implemented");
  }

  @Override
  public void visit(Multiplication object) {
    writeBinaryExpression("Multiplication", object);
  }

  @Override
  public void visit(NamedElement object) {
    throw new RuntimeException("not yet implemented");
  }

  @Override
  public void visit(NamedElementsValue namedElementsValue) {
    throw new RuntimeException("not yet implemented");
  }

  @Override
  public void visit(NamedValue namedValue) {
    throw new RuntimeException("not yet implemented");
  }

  @Override
  public void visit(Namespace object) {
    writer.beginNode("Namespace");
    writer.attribute("name", object.getName());
    object.accept(idWriter);
    object.metadata().accept(this);
    object.children.accept(this);
    writer.endNode();
  }

  @Override
  public void visit(NaturalType object) {
    node("Natural", object);
  }

  @Override
  public void visit(Not object) {
    writer.beginNode("Not");
    object.accept(idWriter);
    object.metadata().accept(this);
    object.expression.accept(this);
    writer.endNode();
  }

  @Override
  public void visit(NotEqual object) {
    throw new RuntimeException("not yet implemented");
  }

  @Override
  public void visit(NumberValue object) {
    writeValueNode(object, "NumberValue", object.toString());
  }

  @Override
  public void visit(Or object) {
    throw new RuntimeException("not yet implemented");
  }

  @Override
  public void visit(Plus object) {
    writeBinaryExpression("Plus", object);
  }

  @Override
  public void visit(PointerType object) {
    throw new RuntimeException("not yet implemented");
  }

  @Override
  public void visit(Queue object) {
    writer.beginNode("Queue");
    writer.attribute("name", object.getName());
    object.accept(idWriter);
    object.metadata().accept(this);
    writer.endNode();
  }

  @Override
  public void visit(RangeTemplate object) {
    node("RangeTemplate", object);
  }

  @Override
  public void visit(RangeType object) {
    writer.beginNode("RangeType");
    writer.attribute("low", object.range.low.toString());
    writer.attribute("high", object.range.high.toString());
    writer.attribute("name", object.getName());
    object.accept(idWriter);
    object.metadata().accept(this);
    writer.endNode();
  }

  @Override
  public void visit(RawComposition object) {
    throw new RuntimeException("not yet implemented");
  }

  @Override
  public void visit(RawElementary object) {
    writer.beginNode("RawElementary");
    writer.attribute("name", object.getName());
    object.accept(idWriter);
    object.metadata().accept(this);
    encapsulate("interface", object.getIface());
    encapsulate("entry", object.getEntryFunc());
    encapsulate("exit", object.getExitFunc());
    encapsulate("declaration", object.getDeclaration());
    encapsulate("instantiation", object.getInstantiation());
    writer.endNode();
  }

  @Override
  public void visit(RawHfsm rawHfsm) {
    throw new RuntimeException("not yet implemented");
  }

  @Override
  public void visit(RecordType recordType) {
    throw new RuntimeException("not yet implemented");
  }

  @Override
  public void visit(RecordValue recordValue) {
    throw new RuntimeException("not yet implemented");
  }

  @Override
  public void visit(RefCall object) {
    writer.beginNode("ReferenceCall");
    object.accept(idWriter);
    object.metadata().accept(this);
    object.actualParameter.accept(this);
    writer.endNode();
  }

  @Override
  public void visit(LinkedReferenceWithOffset_Implementation object) {
    writer.beginNode("Reference");
    object.accept(idWriter);

    // FIXME That is a bit hacky. Split Reference to a linked and a unlinked implementation.
    if (astId.hasId(object.getLink())) {
      writer.attribute("link", astId.getId(object.getLink()));
    } else {
      object.getLink().accept(this);
    }

    object.metadata().accept(this);
    object.getOffset().accept(this);
    writer.endNode();
  }

  @Override
  public void visit(ReferenceExpression object) {
    writer.beginNode("ReferenceExpression");
    object.accept(idWriter);
    object.metadata().accept(this);
    object.reference.accept(this);
    writer.endNode();
  }

  @Override
  public void visit(RefIndex refIndex) {
    throw new RuntimeException("not yet implemented");
  }

  @Override
  public void visit(RefName object) {
    throw new RuntimeException("not yet implemented");
  }

  @Override
  public void visit(RefTemplCall object) {
    throw new RuntimeException("not yet implemented");
  }

  @Override
  public void visit(ExpressionReturn object) {
    writer.beginNode("ExpressionReturn");
    object.accept(idWriter);
    object.metadata().accept(this);
    object.expression.accept(this);
    writer.endNode();
  }

  @Override
  public void visit(VoidReturn object) {
    throw new RuntimeException("not yet implemented");
  }

  @Override
  public void visit(RizzlyFile object) {
    writer.beginNode("RizzlyFile");
    writer.attribute("name", object.getName());
    object.accept(idWriter);
    object.metadata().accept(this);

    for (Designator itr : object.imports) {
      writer.beginNode("import");
      writer.attribute("file", itr.toString("."));
      writer.endNode();
    }

    object.objects.accept(this);

    writer.endNode();
  }

  @Override
  public void visit(Shl object) {
    throw new RuntimeException("not yet implemented");
  }

  @Override
  public void visit(Shr shr) {
    throw new RuntimeException("not yet implemented");
  }

  @Override
  public void visit(SIntType sIntType) {
    throw new RuntimeException("not yet implemented");
  }

  @Override
  public void visit(StateComposite stateComposite) {
    throw new RuntimeException("not yet implemented");
  }

  @Override
  public void visit(StateRef object) {
    throw new RuntimeException("not yet implemented");
  }

  @Override
  public void visit(StateSimple object) {
    throw new RuntimeException("not yet implemented");
  }

  @Override
  public void visit(StateVariable object) {
    visitDefaultVariable("StateVariable", object);
  }

  @Override
  public void visit(StringType object) {
    node("String", object);
  }

  @Override
  public void visit(StringValue object) {
    throw new RuntimeException("not yet implemented");
  }

  @Override
  public void visit(SubCallbacks object) {
    throw new RuntimeException("not yet implemented");
  }

  @Override
  public void visit(SynchroniusConnection object) {
    throw new RuntimeException("not yet implemented");
  }

  @Override
  public void visit(Template object) {
    writer.beginNode("Template");
    writer.attribute("name", object.getName());
    object.accept(idWriter);
    object.metadata().accept(this);
    object.getTempl().accept(this);
    object.getObject().accept(this);
    writer.endNode();
  }

  @Override
  public void visit(TemplateParameter object) {
    writer.beginNode("TemplateParameter");
    writer.attribute("name", object.getName());
    object.accept(idWriter);
    object.metadata().accept(this);
    object.type.accept(this);
    writer.endNode();
  }

  @Override
  public void visit(Transition object) {
    throw new RuntimeException("not yet implemented");
  }

  @Override
  public void visit(TupleType object) {
    throw new RuntimeException("not yet implemented");
  }

  @Override
  public void visit(TupleValue object) {
    writer.beginNode("TupleValue");
    object.accept(idWriter);
    object.metadata().accept(this);
    object.value.accept(this);
    writer.endNode();
  }

  @Override
  public void visit(TypeCast object) {
    throw new RuntimeException("not yet implemented");
  }

  @Override
  public void visit(TypeReference object) {
    visitTypedReference("TypeReference", object);
  }

  @Override
  public void visit(TypeType object) {
    writer.beginNode("TypeType");
    writer.attribute("name", object.getName());
    object.accept(idWriter);
    object.metadata().accept(this);
    object.type.accept(this);
    writer.endNode();
  }

  @Override
  public void visit(TypeTypeTemplate object) {
    node("TypeTypeTemplate", object);
  }

  @Override
  public void visit(UIntType object) {
    throw new RuntimeException("not yet implemented");
  }

  @Override
  public void visit(Uminus object) {
    throw new RuntimeException("not yet implemented");
  }

  @Override
  public void visit(UnionType object) {
    throw new RuntimeException("not yet implemented");
  }

  @Override
  public void visit(UnionValue object) {
    throw new RuntimeException("not yet implemented");
  }

  @Override
  public void visit(UnsafeUnionType object) {
    throw new RuntimeException("not yet implemented");
  }

  @Override
  public void visit(UnsafeUnionValue unsafeUnionValue) {
    throw new RuntimeException("not yet implemented");
  }

  @Override
  public void visit(VarDefInitStmt varDefInitStmt) {
    throw new RuntimeException("not yet implemented");
  }

  @Override
  public void visit(VarDefStmt varDefStmt) {
    throw new RuntimeException("not yet implemented");
  }

  @Override
  public void visit(VoidType object) {
    node("Void", object);
  }

  @Override
  public void visit(WhileStmt whileStmt) {
    throw new RuntimeException("not yet implemented");
  }

  @Override
  public void visit(SourcePosition elementInfo) {
    writer.beginNode("SourcePosition");
    writer.attribute("filename", elementInfo.filename);
    writer.attribute("line", String.valueOf(elementInfo.line));
    writer.attribute("row", String.valueOf(elementInfo.row));
    writer.endNode();
  }

  private void writeValueNode(Ast object, String name, String value) {
    writer.beginNode(name);
    writer.attribute("value", value);
    object.accept(idWriter);
    object.metadata().accept(this);
    writer.endNode();
  }

  private void writeBinaryExpression(String name, BinaryExpression expression) {
    writer.beginNode(name);
    expression.accept(idWriter);
    expression.metadata().accept(this);
    expression.left.accept(this);
    expression.right.accept(this);
    writer.endNode();
  }

  private void node(String name, Ast object) {
    writer.beginNode(name);
    object.accept(idWriter);
    writer.endNode();
  }

  private void encapsulate(String nodeName, VisitorAcceptor object) {
    writer.beginNode(nodeName);
    object.accept(this);
    writer.endNode();
  }

  private void visit(FunctionProperty property) {
    String text = "";
    switch (property) {
      case External:
        text = "extern";
        break;
      case Private:
        text = "private";
        break;
      case Public:
        text = "public";
        break;
    }
    writer.attribute("scope", text);
  }

  private void visitFunction(String typeName, Function object) {
    writer.beginNode(typeName);
    visit(object.property);
    writer.attribute("name", object.getName());
    object.accept(idWriter);
    object.metadata().accept(this);
    object.param.accept(this);
    object.ret.accept(this);
    object.body.accept(this);
    writer.endNode();
  }

  private void visitProcedure(String typeName, Function object) {
    writer.beginNode(typeName);
    visit(object.property);
    writer.attribute("name", object.getName());
    object.accept(idWriter);
    object.metadata().accept(this);
    object.param.accept(this);
    object.body.accept(this);
    writer.endNode();
  }

  private <T extends Ast> void visitTypedReference(String typeName, TypedReference<T> object) {
    writer.beginNode(typeName);
    object.accept(idWriter);
    object.metadata().accept(this);
    object.ref.accept(this);
    writer.endNode();
  }

  private void visitDefaultVariable(String typeName, DefaultVariable object) {
    writer.beginNode(typeName);
    writer.attribute("name", object.getName());
    object.accept(idWriter);
    object.metadata().accept(this);
    object.type.accept(this);
    object.def.accept(this);
    writer.endNode();
  }

}
