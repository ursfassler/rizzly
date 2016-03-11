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
import ast.data.AstList;
import ast.data.Named;
import ast.data.Namespace;
import ast.data.component.composition.AsynchroniusConnection;
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
import ast.data.reference.LinkedAnchor;
import ast.data.reference.OffsetReference;
import ast.data.reference.RefCall;
import ast.data.reference.RefIndex;
import ast.data.reference.RefName;
import ast.data.reference.RefTemplCall;
import ast.data.reference.UnlinkedAnchor;
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
import ast.data.variable.DefaultVariable;
import ast.data.variable.FunctionVariable;
import ast.data.variable.GlobalConstant;
import ast.data.variable.PrivateConstant;
import ast.data.variable.StateVariable;
import ast.data.variable.TemplateParameter;
import ast.meta.MetaInformation;
import ast.meta.MetaList;
import ast.meta.SourcePosition;
import ast.pass.output.xml.IdReader;
import ast.visitor.VisitExecutor;
import ast.visitor.Visitee;
import ast.visitor.Visitor;

public class Write implements Visitor {
  private final XmlStreamWriter writer;
  private final IdReader astId;
  private final Visitor idWriter;
  private final VisitExecutor executor;

  public Write(XmlStreamWriter writer, IdReader astId, Visitor idWriter, VisitExecutor executor) {
    this.writer = writer;
    this.astId = astId;
    this.idWriter = idWriter;
    this.executor = executor;
  }

  public void visit(AliasType object) {
    throw new RuntimeException("not yet implemented");
  }

  public void visit(And object) {
    throw new RuntimeException("not yet implemented");
  }

  public void visit(AnyType object) {
    writer.beginNode("AnyType");
    writer.attribute("name", object.getName());
    executor.visit(idWriter, object);
    writer.endNode();
  }

  public void visit(AnyValue object) {
    throw new RuntimeException("not yet implemented");
  }

  public void visit(ArrayTemplate object) {
    namedNode(object);
  }

  public void visit(ArrayType object) {
    throw new RuntimeException("not yet implemented");
  }

  public void visit(ArrayValue object) {
    throw new RuntimeException("not yet implemented");
  }

  public void visit(MultiAssignment object) {
    writer.beginNode("MultiAssignment");
    executor.visit(idWriter, object);
    executor.visit(this, object.metadata());
    executor.visit(this, object.left);
    executor.visit(this, object.right);
    writer.endNode();
  }

  public void visit(AssignmentSingle object) {
    throw new RuntimeException("not yet implemented");
  }

  public void visit(AsynchroniusConnection object) {
    throw new RuntimeException("not yet implemented");
  }

  public void visit(BitAnd object) {
    throw new RuntimeException("not yet implemented");
  }

  public void visit(BitNot object) {
    throw new RuntimeException("not yet implemented");
  }

  public void visit(BitOr object) {
    throw new RuntimeException("not yet implemented");
  }

  public void visit(BitXor object) {
    throw new RuntimeException("not yet implemented");
  }

  public void visit(Block object) {
    writer.beginNode("Block");
    executor.visit(idWriter, object);
    executor.visit(this, object.metadata());
    executor.visit(this, object.statements);
    writer.endNode();
  }

  public void visit(BooleanType object) {
    namedNode(object);
  }

  public void visit(BooleanValue object) {
    writeValueNode(object, "BooleanValue", object.value ? "True" : "False");
  }

  public void visit(CallStmt object) {
    writer.beginNode("CallStatement");
    executor.visit(idWriter, object);
    executor.visit(this, object.metadata());
    executor.visit(this, object.call);
    writer.endNode();
  }

  public void visit(CaseOpt object) {
    throw new RuntimeException("not yet implemented");
  }

  public void visit(CaseOptRange object) {
    throw new RuntimeException("not yet implemented");
  }

  public void visit(CaseOptSimple object) {
    throw new RuntimeException("not yet implemented");
  }

  public void visit(CaseOptValue object) {
    throw new RuntimeException("not yet implemented");
  }

  public void visit(CaseStmt object) {
    throw new RuntimeException("not yet implemented");
  }

  public void visit(ComponentType object) {
    throw new RuntimeException("not yet implemented");
  }

  public void visit(ComponentUse object) {
    writer.beginNode("ComponentUse");
    writer.attribute("name", object.getName());
    executor.visit(idWriter, object);
    executor.visit(this, object.metadata());
    executor.visit(this, object.getCompRef());
    writer.endNode();
  }

  public void visit(GlobalConstant object) {
    visitDefaultVariable("GlobalConstant", object);
  }

  public void visit(PrivateConstant object) {
    visitDefaultVariable("PrivateConstant", object);
  }

  public void visit(DefaultValueTemplate object) {
    namedNode(object);
  }

  public void visit(Division object) {
    writeBinaryExpression("Division", object);
  }

  public void visit(EndpointRaw object) {
    throw new RuntimeException("not yet implemented");
  }

  public void visit(EndpointSelf object) {
    throw new RuntimeException("not yet implemented");
  }

  public void visit(EndpointSub object) {
    throw new RuntimeException("not yet implemented");
  }

  public void visit(EnumElement enumElement) {
    throw new RuntimeException("not yet implemented");
  }

  public void visit(EnumType enumType) {
    throw new RuntimeException("not yet implemented");
  }

  public void visit(Equal object) {
    throw new RuntimeException("not yet implemented");
  }

  public void visit(ForStmt object) {
    throw new RuntimeException("not yet implemented");
  }

  public void visit(FuncFunction object) {
    visitFunction("Function", object);
  }

  public void visit(FuncInterrupt object) {
    throw new RuntimeException("not yet implemented");
  }

  public void visit(Procedure object) {
    visitProcedure("Procedure", object);
  }

  public void visit(FuncQuery object) {
    throw new RuntimeException("not yet implemented");
  }

  public void visit(Response object) {
    visitFunction("Response", object);
  }

  public void visit(FuncReturnNone object) {
    throw new RuntimeException("not yet implemented");
  }

  public void visit(FuncReturnTuple object) {
    throw new RuntimeException("not yet implemented");
  }

  public void visit(FunctionReturnType object) {
    writer.beginNode("ReturnType");
    executor.visit(idWriter, object);
    executor.visit(this, object.metadata());
    executor.visit(this, object.type);
    writer.endNode();
  }

  public void visit(Signal object) {
    visitProcedure("Signal", object);
  }

  public void visit(Slot object) {
    visitProcedure("Slot", object);
  }

  public void visit(FuncSubHandlerEvent object) {
    throw new RuntimeException("not yet implemented");
  }

  public void visit(FuncSubHandlerQuery object) {
    throw new RuntimeException("not yet implemented");
  }

  public void visit(FunctionType object) {
    throw new RuntimeException("not yet implemented");
  }

  public void visit(FunctionVariable object) {
    writer.beginNode("FunctionVariable");
    writer.attribute("name", object.getName());
    executor.visit(idWriter, object);
    executor.visit(this, object.metadata());
    executor.visit(this, object.type);
    writer.endNode();
  }

  public void visit(Greater object) {
    throw new RuntimeException("not yet implemented");
  }

  public void visit(GreaterEqual object) {
    throw new RuntimeException("not yet implemented");
  }

  public void visit(IfOption object) {
    writer.beginNode("IfOption");
    executor.visit(idWriter, object);
    executor.visit(this, object.metadata());
    executor.visit(this, object.condition);
    executor.visit(this, object.code);
    writer.endNode();
  }

  public void visit(IfStatement object) {
    writer.beginNode("IfStatement");
    executor.visit(idWriter, object);
    executor.visit(this, object.metadata());
    executor.visit(this, object.option);
    executor.visit(this, object.defblock);
    writer.endNode();
  }

  public void visit(ImplComposition object) {
    throw new RuntimeException("not yet implemented");
  }

  public void visit(ImplElementary object) {
    writer.beginNode("Elementary");
    writer.attribute("name", object.getName());
    executor.visit(idWriter, object);
    executor.visit(this, object.metadata());
    executor.visit(this, object.queue);
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

  public void visit(ImplHfsm object) {
    throw new RuntimeException("not yet implemented");
  }

  public void visit(IntegerType object) {
    namedNode(object);
  }

  public void visit(Is object) {
    throw new RuntimeException("not yet implemented");
  }

  public void visit(Less object) {
    throw new RuntimeException("not yet implemented");
  }

  public void visit(LessEqual object) {
    throw new RuntimeException("not yet implemented");
  }

  public void visit(LogicAnd object) {
    throw new RuntimeException("not yet implemented");
  }

  public void visit(LogicNot object) {
    throw new RuntimeException("not yet implemented");
  }

  public void visit(LogicOr object) {
    throw new RuntimeException("not yet implemented");
  }

  public void visit(Minus object) {
    writeBinaryExpression("Minus", object);
  }

  public void visit(Modulo object) {
    writeBinaryExpression("Modulo", object);
  }

  public void visit(MsgPush object) {
    throw new RuntimeException("not yet implemented");
  }

  public void visit(Multiplication object) {
    writeBinaryExpression("Multiplication", object);
  }

  public void visit(NamedElement object) {
    throw new RuntimeException("not yet implemented");
  }

  public void visit(NamedElementsValue namedElementsValue) {
    throw new RuntimeException("not yet implemented");
  }

  public void visit(NamedValue namedValue) {
    throw new RuntimeException("not yet implemented");
  }

  public void visit(Namespace object) {
    writer.beginNode("Namespace");
    writer.attribute("name", object.getName());
    executor.visit(idWriter, object);
    executor.visit(this, object.metadata());
    executor.visit(this, object.children);
    writer.endNode();
  }

  public void visit(NaturalType object) {
    namedNode(object);
  }

  public void visit(Not object) {
    writer.beginNode("Not");
    executor.visit(idWriter, object);
    executor.visit(this, object.metadata());
    executor.visit(this, object.expression);
    writer.endNode();
  }

  public void visit(NotEqual object) {
    throw new RuntimeException("not yet implemented");
  }

  public void visit(NumberValue object) {
    writeValueNode(object, "NumberValue", object.toString());
  }

  public void visit(Or object) {
    throw new RuntimeException("not yet implemented");
  }

  public void visit(Plus object) {
    writeBinaryExpression("Plus", object);
  }

  public void visit(PointerType object) {
    throw new RuntimeException("not yet implemented");
  }

  public void visit(Queue object) {
    writer.beginNode("Queue");
    writer.attribute("name", object.getName());
    executor.visit(idWriter, object);
    executor.visit(this, object.metadata());
    writer.endNode();
  }

  public void visit(RangeTemplate object) {
    namedNode(object);
  }

  public void visit(RangeType object) {
    writer.beginNode("RangeType");
    writer.attribute("low", object.range.low.toString());
    writer.attribute("high", object.range.high.toString());
    writer.attribute("name", object.getName());
    executor.visit(idWriter, object);
    executor.visit(this, object.metadata());
    writer.endNode();
  }

  public void visit(RawComposition object) {
    throw new RuntimeException("not yet implemented");
  }

  public void visit(RawElementary object) {
    writer.beginNode("RawElementary");
    writer.attribute("name", object.getName());
    executor.visit(idWriter, object);
    executor.visit(this, object.metadata());
    encapsulate("interface", object.getIface());
    encapsulate("entry", object.getEntryFunc());
    encapsulate("exit", object.getExitFunc());
    encapsulate("declaration", object.getDeclaration());
    encapsulate("instantiation", object.getInstantiation());
    writer.endNode();
  }

  public void visit(RawHfsm object) {
    throw new RuntimeException("not yet implemented");
  }

  public void visit(RecordType object) {
    throw new RuntimeException("not yet implemented");
  }

  public void visit(RecordValue object) {
    throw new RuntimeException("not yet implemented");
  }

  public void visit(OffsetReference object) {
    writer.beginNode("Reference");
    executor.visit(idWriter, object);
    executor.visit(this, object.metadata());
    executor.visit(this, object.getAnchor());
    executor.visit(this, object.getOffset());
    writer.endNode();
  }

  public void visit(RefCall object) {
    writer.beginNode("ReferenceCall");
    executor.visit(idWriter, object);
    executor.visit(this, object.metadata());
    executor.visit(this, object.actualParameter);
    writer.endNode();
  }

  public void visit(LinkedAnchor object) {
    writer.beginNode("LinkedAnchor");
    executor.visit(idWriter, object);

    assert (astId.hasId(object.getLink()));
    writer.attribute("link", astId.getId(object.getLink()));

    executor.visit(this, object.metadata());
    writer.endNode();
  }

  public void visit(UnlinkedAnchor object) {
    writer.beginNode("UnlinkedAnchor");
    executor.visit(idWriter, object);

    writer.attribute("target", object.getLinkName());

    executor.visit(this, object.metadata());
    writer.endNode();
  }

  public void visit(ReferenceExpression object) {
    writer.beginNode("ReferenceExpression");
    executor.visit(idWriter, object);
    executor.visit(this, object.metadata());
    executor.visit(this, object.reference);
    writer.endNode();
  }

  public void visit(RefIndex refIndex) {
    throw new RuntimeException("not yet implemented");
  }

  public void visit(RefName object) {
    throw new RuntimeException("not yet implemented");
  }

  public void visit(RefTemplCall object) {
    throw new RuntimeException("not yet implemented");
  }

  public void visit(ExpressionReturn object) {
    writer.beginNode("ExpressionReturn");
    executor.visit(idWriter, object);
    executor.visit(this, object.metadata());
    executor.visit(this, object.expression);
    writer.endNode();
  }

  public void visit(VoidReturn object) {
    throw new RuntimeException("not yet implemented");
  }

  public void visit(RizzlyFile object) {
    writer.beginNode("RizzlyFile");
    writer.attribute("name", object.getName());
    executor.visit(idWriter, object);
    executor.visit(this, object.metadata());

    for (Designator itr : object.imports) {
      writer.beginNode("import");
      writer.attribute("file", itr.toString("."));
      writer.endNode();
    }

    executor.visit(this, object.objects);

    writer.endNode();
  }

  public void visit(Shl object) {
    throw new RuntimeException("not yet implemented");
  }

  public void visit(Shr shr) {
    throw new RuntimeException("not yet implemented");
  }

  public void visit(SIntType sIntType) {
    throw new RuntimeException("not yet implemented");
  }

  public void visit(StateComposite stateComposite) {
    throw new RuntimeException("not yet implemented");
  }

  public void visit(StateSimple object) {
    throw new RuntimeException("not yet implemented");
  }

  public void visit(StateVariable object) {
    visitDefaultVariable("StateVariable", object);
  }

  public void visit(StringType object) {
    namedNode(object);
  }

  public void visit(StringValue object) {
    throw new RuntimeException("not yet implemented");
  }

  public void visit(SubCallbacks object) {
    throw new RuntimeException("not yet implemented");
  }

  public void visit(SynchroniusConnection object) {
    throw new RuntimeException("not yet implemented");
  }

  public void visit(Template object) {
    writer.beginNode("Template");
    writer.attribute("name", object.getName());
    executor.visit(idWriter, object);
    executor.visit(this, object.metadata());
    executor.visit(this, object.getTempl());
    executor.visit(this, object.getObject());
    writer.endNode();
  }

  public void visit(TemplateParameter object) {
    writer.beginNode("TemplateParameter");
    writer.attribute("name", object.getName());
    executor.visit(idWriter, object);
    executor.visit(this, object.metadata());
    executor.visit(this, object.type);
    writer.endNode();
  }

  public void visit(Transition object) {
    throw new RuntimeException("not yet implemented");
  }

  public void visit(TupleType object) {
    throw new RuntimeException("not yet implemented");
  }

  public void visit(TupleValue object) {
    writer.beginNode("TupleValue");
    executor.visit(idWriter, object);
    executor.visit(this, object.metadata());
    executor.visit(this, object.value);
    writer.endNode();
  }

  public void visit(TypeCast object) {
    throw new RuntimeException("not yet implemented");
  }

  public void visit(TypeType object) {
    writer.beginNode("TypeType");
    writer.attribute("name", object.getName());
    executor.visit(idWriter, object);
    executor.visit(this, object.metadata());
    executor.visit(this, object.type);
    writer.endNode();
  }

  public void visit(TypeTypeTemplate object) {
    namedNode(object);
  }

  public void visit(UIntType object) {
    throw new RuntimeException("not yet implemented");
  }

  public void visit(Uminus object) {
    throw new RuntimeException("not yet implemented");
  }

  public void visit(UnionType object) {
    throw new RuntimeException("not yet implemented");
  }

  public void visit(UnionValue object) {
    throw new RuntimeException("not yet implemented");
  }

  public void visit(UnsafeUnionType object) {
    throw new RuntimeException("not yet implemented");
  }

  public void visit(UnsafeUnionValue object) {
    throw new RuntimeException("not yet implemented");
  }

  public void visit(VarDefInitStmt object) {
    throw new RuntimeException("not yet implemented");
  }

  public void visit(VarDefStmt object) {
    throw new RuntimeException("not yet implemented");
  }

  public void visit(VoidType object) {
    namedNode(object);
  }

  public void visit(WhileStmt object) {
    throw new RuntimeException("not yet implemented");
  }

  public void visit(MetaList list) {
    for (MetaInformation info : list) {
      executor.visit(this, info);
    }
  }

  public void visit(SourcePosition object) {
    writer.beginNode("SourcePosition");
    writer.attribute("filename", object.filename);
    writer.attribute("line", String.valueOf(object.line));
    writer.attribute("row", String.valueOf(object.row));
    writer.endNode();
  }

  private void writeValueNode(Ast object, String name, String value) {
    writer.beginNode(name);
    writer.attribute("value", value);
    executor.visit(idWriter, object);
    executor.visit(this, object.metadata());
    writer.endNode();
  }

  private void writeBinaryExpression(String name, BinaryExpression expression) {
    writer.beginNode(name);
    executor.visit(idWriter, expression);
    executor.visit(this, expression.metadata());
    executor.visit(this, expression.left);
    executor.visit(this, expression.right);
    writer.endNode();
  }

  private void namedNode(Named object) {
    writer.beginNode(object.getName());
    writer.attribute("name", object.getName());
    executor.visit(idWriter, object);
    writer.endNode();
  }

  private void encapsulate(String nodeName, Visitee object) {
    writer.beginNode(nodeName);
    executor.visit(this, object);
    writer.endNode();
  }

  private void encapsulate(String nodeName, AstList<? extends Ast> object) {
    writer.beginNode(nodeName);
    executor.visit(this, object);
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
    executor.visit(idWriter, object);
    executor.visit(this, object.metadata());
    executor.visit(this, object.param);
    executor.visit(this, object.ret);
    executor.visit(this, object.body);
    writer.endNode();
  }

  private void visitProcedure(String typeName, Function object) {
    writer.beginNode(typeName);
    visit(object.property);
    writer.attribute("name", object.getName());
    executor.visit(idWriter, object);
    executor.visit(this, object.metadata());
    executor.visit(this, object.param);
    executor.visit(this, object.body);
    writer.endNode();
  }

  private void visitDefaultVariable(String typeName, DefaultVariable object) {
    writer.beginNode(typeName);
    writer.attribute("name", object.getName());
    executor.visit(idWriter, object);
    executor.visit(this, object.metadata());
    executor.visit(this, object.type);
    executor.visit(this, object.def);
    writer.endNode();
  }

}
