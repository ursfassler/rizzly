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

package ast.visitor;

import java.util.ArrayList;
import java.util.Collection;

import ast.data.Ast;
import ast.data.AstList;
import ast.data.Namespace;
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
import ast.data.component.hfsm.StateSimple;
import ast.data.component.hfsm.Transition;
import ast.data.expression.ReferenceExpression;
import ast.data.expression.TypeCast;
import ast.data.expression.binop.And;
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
import ast.data.reference.LinkedAnchor;
import ast.data.reference.LinkedReferenceWithOffset_Implementation;
import ast.data.reference.OffsetReference;
import ast.data.reference.RefCall;
import ast.data.reference.RefIndex;
import ast.data.reference.RefName;
import ast.data.reference.RefTemplCall;
import ast.data.reference.SimpleReference;
import ast.data.reference.UnlinkedAnchor;
import ast.data.reference.UnlinkedReferenceWithOffset_Implementation;
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
import ast.data.variable.FunctionVariable;
import ast.data.variable.GlobalConstant;
import ast.data.variable.StateVariable;
import ast.data.variable.TemplateParameter;
import ast.meta.SourcePosition;

public class DeepFirstTraverser implements Visitor {
  final private VisitExecutorImplementation executor = new VisitExecutorImplementation();
  final private Collection<Visitor> preorderVisitor = new ArrayList<Visitor>();
  final private Collection<Visitor> postorderVisitor = new ArrayList<Visitor>();

  public void addPreorderVisitor(Visitor visitor) {
    preorderVisitor.add(visitor);
  }

  public void addPostorderVisitor(Visitor visitor) {
    postorderVisitor.add(visitor);
  }

  public void visit(NamedElementsValue object) {
    preorderVisit(object);
    executeVisit(object.value);
    postorderVisit(object);
  }

  public void visit(TupleType object) {
    preorderVisit(object);
    executeVisit(object.types);
    postorderVisit(object);
  }

  public void visit(FuncReturnTuple object) {
    preorderVisit(object);
    executeVisit(object.param);
    postorderVisit(object);
  }

  public void visit(FunctionReturnType object) {
    preorderVisit(object);
    executeVisit(object.type);
    postorderVisit(object);
  }

  public void visit(FuncReturnNone object) {
    preorderVisit(object);
    postorderVisit(object);
  }

  public void visit(ImplElementary object) {
    preorderVisit(object);
    executeVisit(object.function);
    executeVisit(object.iface);
    executeVisit(object.queue);
    executeVisit(object.type);
    executeVisit(object.constant);
    executeVisit(object.variable);
    executeVisit(object.component);
    executeVisit(object.subCallback);
    executeVisit(object.entryFunc);
    executeVisit(object.exitFunc);
    postorderVisit(object);
  }

  public void visit(ImplComposition object) {
    preorderVisit(object);
    executeVisit(object.function);
    executeVisit(object.iface);
    executeVisit(object.queue);
    executeVisit(object.component);
    executeVisit(object.connection);
    postorderVisit(object);
  }

  public void visit(ImplHfsm object) {
    preorderVisit(object);
    executeVisit(object.function);
    executeVisit(object.iface);
    executeVisit(object.queue);
    executeVisit(object.topstate);
    postorderVisit(object);
  }

  public void visit(NumberValue object) {
    preorderVisit(object);
    postorderVisit(object);
  }

  public void visit(VarDefStmt object) {
    preorderVisit(object);
    executeVisit(object.variable);
    postorderVisit(object);
  }

  public void visit(CallStmt object) {
    preorderVisit(object);
    executeVisit(object.call);
    postorderVisit(object);
  }

  public void visit(MultiAssignment object) {
    preorderVisit(object);
    executeVisit(object.left);
    executeVisit(object.right);
    postorderVisit(object);
  }

  public void visit(AssignmentSingle object) {
    preorderVisit(object);
    executeVisit(object.left);
    executeVisit(object.right);
    postorderVisit(object);
  }

  public void visit(UnionType object) {
    preorderVisit(object);
    executeVisit(object.tag);
    executeVisit(object.element);
    postorderVisit(object);
  }

  public void visit(UnsafeUnionType object) {
    preorderVisit(object);
    executeVisit(object.element);
    postorderVisit(object);
  }

  public void visit(RecordType object) {
    preorderVisit(object);
    executeVisit(object.element);
    postorderVisit(object);
  }

  public void visit(ComponentType object) {
    preorderVisit(object);
    executeVisit(object.input);
    executeVisit(object.output);
    postorderVisit(object);
  }

  public void visit(RefName object) {
    preorderVisit(object);
    postorderVisit(object);
  }

  public void visit(RefIndex object) {
    preorderVisit(object);
    executeVisit(object.index);
    postorderVisit(object);
  }

  public void visit(RefCall object) {
    preorderVisit(object);
    executeVisit(object.actualParameter);
    postorderVisit(object);
  }

  public void visit(ArrayType object) {
    preorderVisit(object);
    executeVisit(object.type);
    postorderVisit(object);
  }

  public void visit(BooleanType object) {
    preorderVisit(object);
    postorderVisit(object);
  }

  public void visit(EnumType object) {
    preorderVisit(object);
    executeVisit(object.element);
    postorderVisit(object);
  }

  public void visit(EnumElement object) {
    preorderVisit(object);
    postorderVisit(object);
  }

  public void visit(ExpressionReturn object) {
    preorderVisit(object);
    executeVisit(object.expression);
    postorderVisit(object);
  }

  public void visit(VoidReturn object) {
    preorderVisit(object);
    postorderVisit(object);
  }

  public void visit(VoidType object) {
    preorderVisit(object);
    postorderVisit(object);
  }

  public void visit(FunctionVariable object) {
    preorderVisit(object);
    executeVisit(object.type);
    postorderVisit(object);
  }

  public void visit(StateVariable object) {
    preorderVisit(object);
    executeVisit(object.type);
    executeVisit(object.def);
    postorderVisit(object);
  }

  public void visit(ConstPrivate object) {
    preorderVisit(object);
    executeVisit(object.type);
    executeVisit(object.def);
    postorderVisit(object);
  }

  public void visit(GlobalConstant object) {
    preorderVisit(object);
    executeVisit(object.type);
    executeVisit(object.def);
    postorderVisit(object);
  }

  public void visit(SubCallbacks object) {
    preorderVisit(object);
    executeVisit(object.compUse);
    executeVisit(object.func);
    postorderVisit(object);
  }

  public void visit(Namespace object) {
    preorderVisit(object);
    executeVisit(object.children);
    postorderVisit(object);
  }

  public void visit(CaseOptRange object) {
    preorderVisit(object);
    executeVisit(object.start);
    executeVisit(object.end);
    postorderVisit(object);
  }

  public void visit(CaseOptValue object) {
    preorderVisit(object);
    executeVisit(object.value);
    postorderVisit(object);
  }

  public void visit(BooleanValue object) {
    preorderVisit(object);
    postorderVisit(object);
  }

  public void visit(SynchroniusConnection object) {
    preorderVisit(object);
    executeVisit(object.getSrc());
    executeVisit(object.getDst());
    postorderVisit(object);
  }

  public void visit(AsynchroniusConnection object) {
    preorderVisit(object);
    executeVisit(object.getSrc());
    executeVisit(object.getDst());
    postorderVisit(object);
  }

  public void visit(StringValue object) {
    preorderVisit(object);
    postorderVisit(object);
  }

  public void visit(StringType object) {
    preorderVisit(object);
    postorderVisit(object);
  }

  public void visit(ArrayValue object) {
    preorderVisit(object);
    executeVisit(object.value);
    postorderVisit(object);
  }

  public void visit(TupleValue object) {
    preorderVisit(object);
    executeVisit(object.value);
    postorderVisit(object);
  }

  public void visit(StateSimple object) {
    preorderVisit(object);
    executeVisit(object.entryFunc);
    executeVisit(object.exitFunc);
    executeVisit(object.item);
    postorderVisit(object);
  }

  public void visit(StateComposite object) {
    preorderVisit(object);
    executeVisit(object.entryFunc);
    executeVisit(object.exitFunc);
    executeVisit(object.item);
    executeVisit(object.initial);
    postorderVisit(object);
  }

  public void visit(Transition object) {
    preorderVisit(object);
    executeVisit(object.src);
    executeVisit(object.dst);
    executeVisit(object.eventFunc);
    executeVisit(object.param);
    executeVisit(object.guard);
    executeVisit(object.body);
    postorderVisit(object);
  }

  public void visit(LinkedReferenceWithOffset_Implementation object) {
    preorderVisit(object);
    executeVisit(object.getOffset());
    postorderVisit(object);
  }

  public void visit(NaturalType object) {
    preorderVisit(object);
    postorderVisit(object);
  }

  public void visit(IntegerType object) {
    preorderVisit(object);
    postorderVisit(object);
  }

  public void visit(Procedure object) {
    visitFunction(object);
  }

  public void visit(FuncFunction object) {
    visitFunction(object);
  }

  public void visit(FuncSubHandlerQuery object) {
    visitFunction(object);
  }

  public void visit(FuncSubHandlerEvent object) {
    visitFunction(object);
  }

  public void visit(FunctionType object) {
    preorderVisit(object);
    throw new RuntimeException("not yet implemented");
  }

  public void visit(EndpointSelf object) {
    preorderVisit(object);
    executeVisit(object.funcRef);
    postorderVisit(object);
  }

  public void visit(EndpointSub object) {
    preorderVisit(object);
    executeVisit(object.component);
    postorderVisit(object);
  }

  public void visit(EndpointRaw object) {
    preorderVisit(object);
    executeVisit(object.ref);
    postorderVisit(object);
  }

  public void visit(And object) {
    preorderVisit(object);
    executeVisit(object.left);
    executeVisit(object.right);
    postorderVisit(object);
  }

  public void visit(Division object) {
    preorderVisit(object);
    executeVisit(object.left);
    executeVisit(object.right);
    postorderVisit(object);
  }

  public void visit(Equal object) {
    preorderVisit(object);
    executeVisit(object.left);
    executeVisit(object.right);
    postorderVisit(object);
  }

  public void visit(Greater object) {
    preorderVisit(object);
    executeVisit(object.left);
    executeVisit(object.right);
    postorderVisit(object);
  }

  public void visit(GreaterEqual object) {
    preorderVisit(object);
    executeVisit(object.left);
    executeVisit(object.right);
    postorderVisit(object);
  }

  public void visit(Less object) {
    preorderVisit(object);
    executeVisit(object.left);
    executeVisit(object.right);
    postorderVisit(object);
  }

  public void visit(Is object) {
    preorderVisit(object);
    executeVisit(object.left);
    executeVisit(object.right);
    postorderVisit(object);
  }

  public void visit(LessEqual object) {
    preorderVisit(object);
    executeVisit(object.left);
    executeVisit(object.right);
    postorderVisit(object);
  }

  public void visit(Minus object) {
    preorderVisit(object);
    executeVisit(object.left);
    executeVisit(object.right);
    postorderVisit(object);
  }

  public void visit(Modulo object) {
    preorderVisit(object);
    executeVisit(object.left);
    executeVisit(object.right);
    postorderVisit(object);
  }

  public void visit(Multiplication object) {
    preorderVisit(object);
    executeVisit(object.left);
    executeVisit(object.right);
    postorderVisit(object);
  }

  public void visit(Not object) {
    preorderVisit(object);
    executeVisit(object.expression);
    postorderVisit(object);
  }

  public void visit(LogicNot object) {
    preorderVisit(object);
    executeVisit(object.expression);
    postorderVisit(object);
  }

  public void visit(BitNot object) {
    preorderVisit(object);
    executeVisit(object.expression);
    postorderVisit(object);
  }

  public void visit(NotEqual object) {
    preorderVisit(object);
    executeVisit(object.left);
    executeVisit(object.right);
    postorderVisit(object);
  }

  public void visit(Or object) {
    preorderVisit(object);
    executeVisit(object.left);
    executeVisit(object.right);
    postorderVisit(object);
  }

  public void visit(Plus object) {
    preorderVisit(object);
    executeVisit(object.left);
    executeVisit(object.right);
    postorderVisit(object);
  }

  public void visit(Shl object) {
    preorderVisit(object);
    executeVisit(object.left);
    executeVisit(object.right);
    postorderVisit(object);
  }

  public void visit(Shr object) {
    preorderVisit(object);
    executeVisit(object.left);
    executeVisit(object.right);
    postorderVisit(object);
  }

  public void visit(Uminus object) {
    preorderVisit(object);
    executeVisit(object.expression);
    postorderVisit(object);
  }

  public void visit(RangeType object) {
    preorderVisit(object);
    postorderVisit(object);
  }

  public void visit(WhileStmt object) {
    preorderVisit(object);
    executeVisit(object.condition);
    executeVisit(object.body);
    postorderVisit(object);
  }

  public void visit(IfStatement object) {
    preorderVisit(object);
    executeVisit(object.option);
    executeVisit(object.defblock);
    postorderVisit(object);
  }

  public void visit(IfOption object) {
    preorderVisit(object);
    executeVisit(object.condition);
    executeVisit(object.code);
    postorderVisit(object);
  }

  public void visit(Block object) {
    preorderVisit(object);
    executeVisit(object.statements);
    postorderVisit(object);
  }

  public void visit(CaseStmt object) {
    preorderVisit(object);
    executeVisit(object.condition);
    executeVisit(object.option);
    executeVisit(object.otherwise);
    postorderVisit(object);
  }

  public void visit(CaseOpt object) {
    preorderVisit(object);
    executeVisit(object.value);
    executeVisit(object.code);
    postorderVisit(object);
  }

  public void visit(TypeCast object) {
    preorderVisit(object);
    executeVisit(object.cast);
    executeVisit(object.value);
    postorderVisit(object);
  }

  public void visit(BitAnd object) {
    preorderVisit(object);
    executeVisit(object.left);
    executeVisit(object.right);
    postorderVisit(object);
  }

  public void visit(BitOr object) {
    preorderVisit(object);
    executeVisit(object.left);
    executeVisit(object.right);
    postorderVisit(object);
  }

  public void visit(BitXor object) {
    preorderVisit(object);
    executeVisit(object.left);
    executeVisit(object.right);
    postorderVisit(object);
  }

  public void visit(LogicOr object) {
    preorderVisit(object);
    executeVisit(object.left);
    executeVisit(object.right);
    postorderVisit(object);
  }

  public void visit(LogicAnd object) {
    preorderVisit(object);
    executeVisit(object.left);
    executeVisit(object.right);
    postorderVisit(object);
  }

  public void visit(Signal object) {
    visitFunction(object);
  }

  public void visit(FuncQuery object) {
    visitFunction(object);
  }

  public void visit(Slot object) {
    visitFunction(object);
  }

  public void visit(Response object) {
    visitFunction(object);
  }

  public void visit(AnyType object) {
    preorderVisit(object);
    postorderVisit(object);
  }

  public void visit(AnyValue object) {
    preorderVisit(object);
    postorderVisit(object);
  }

  public void visit(NamedValue object) {
    preorderVisit(object);
    executeVisit(object.value);
    postorderVisit(object);
  }

  public void visit(UnionValue object) {
    preorderVisit(object);
    executeVisit(object.tagValue);
    executeVisit(object.contentValue);
    executeVisit(object.type);
    postorderVisit(object);
  }

  public void visit(UnsafeUnionValue object) {
    preorderVisit(object);
    executeVisit(object.contentValue);
    executeVisit(object.type);
    postorderVisit(object);
  }

  public void visit(RecordValue object) {
    preorderVisit(object);
    executeVisit(object.value);
    executeVisit(object.type);
    postorderVisit(object);
  }

  public void visit(MsgPush object) {
    preorderVisit(object);
    executeVisit(object.queue);
    executeVisit(object.func);
    executeVisit(object.data);
    postorderVisit(object);
  }

  public void visit(Queue object) {
    preorderVisit(object);
    postorderVisit(object);
  }

  public void visit(NamedElement object) {
    preorderVisit(object);
    executeVisit(object.typeref);
    postorderVisit(object);
  }

  public void visit(ComponentUse object) {
    preorderVisit(object);
    executeVisit(object.compRef);
    postorderVisit(object);
  }

  public void visit(UIntType object) {
    preorderVisit(object);
    postorderVisit(object);
  }

  public void visit(SIntType object) {
    preorderVisit(object);
    postorderVisit(object);
  }

  public void visit(AliasType object) {
    preorderVisit(object);
    executeVisit(object.ref);
    postorderVisit(object);
  }

  public void visit(ForStmt object) {
    preorderVisit(object);
    executeVisit(object.iterator);
    executeVisit(object.block);
    postorderVisit(object);
  }

  public void visit(DefaultValueTemplate object) {
    preorderVisit(object);
    postorderVisit(object);
  }

  public void visit(RizzlyFile object) {
    preorderVisit(object);
    executeVisit(object.objects);
    postorderVisit(object);
  }

  public void visit(RawElementary object) {
    preorderVisit(object);
    executeVisit(object.getEntryFunc());
    executeVisit(object.getExitFunc());
    executeVisit(object.getIface());
    executeVisit(object.getDeclaration());
    executeVisit(object.getInstantiation());
    postorderVisit(object);
  }

  public void visit(RawComposition object) {
    preorderVisit(object);
    executeVisit(object.getIface());
    executeVisit(object.getInstantiation());
    executeVisit(object.getConnection());
    postorderVisit(object);
  }

  public void visit(RawHfsm object) {
    preorderVisit(object);
    executeVisit(object.getIface());
    executeVisit(object.getTopstate());
    postorderVisit(object);
  }

  public void visit(VarDefInitStmt object) {
    preorderVisit(object);
    executeVisit(object.variable);
    executeVisit(object.initial);
    postorderVisit(object);
  }

  public void visit(RefTemplCall object) {
    preorderVisit(object);
    executeVisit(object.actualParameter);
    postorderVisit(object);
  }

  public void visit(TemplateParameter object) {
    preorderVisit(object);
    executeVisit(object.type);
    postorderVisit(object);
  }

  public void visit(TypeTypeTemplate object) {
    preorderVisit(object);
    postorderVisit(object);
  }

  public void visit(TypeType object) {
    preorderVisit(object);
    executeVisit(object.type);
    postorderVisit(object);
  }

  public void visit(RangeTemplate object) {
    preorderVisit(object);
    postorderVisit(object);
  }

  public void visit(Template object) {
    preorderVisit(object);
    executeVisit(object.getTempl());
    executeVisit(object.getObject());
    postorderVisit(object);
  }

  public void visit(ArrayTemplate object) {
    preorderVisit(object);
    postorderVisit(object);
  }

  public void visit(LinkTarget object) {
    preorderVisit(object);
    postorderVisit(object);
  }

  public void visit(ReferenceExpression object) {
    preorderVisit(object);
    executeVisit(object.reference);
    postorderVisit(object);
  }

  public void visit(TypeReference object) {
    preorderVisit(object);
    executeVisit(object.ref);
    postorderVisit(object);
  }

  public void visit(FunctionReference object) {
    preorderVisit(object);
    executeVisit(object.ref);
    postorderVisit(object);
  }

  public void visit(CompUseRef object) {
    preorderVisit(object);
    executeVisit(object.ref);
    postorderVisit(object);
  }

  public void visit(CaseOptSimple caseOptSimple) {
    throw new RuntimeException("not yet implemented");
  }

  public void visit(FuncInterrupt object) {
    visitFunction(object);
  }

  public void visit(PointerType pointerType) {
    throw new RuntimeException("not yet implemented");
  }

  public void visit(SourcePosition object) {
    preorderVisit(object);
    postorderVisit(object);
  }

  public void visit(UnlinkedReferenceWithOffset_Implementation object) {
    throw new RuntimeException("not yet implemented");
  }

  public void visit(SimpleReference object) {
    preorderVisit(object);
    executeVisit(object.getAnchor());
    postorderVisit(object);
  }

  public void visit(OffsetReference object) {
    preorderVisit(object);
    executeVisit(object.getAnchor());
    executeVisit(object.getOffset());
    postorderVisit(object);
  }

  public void visit(UnlinkedAnchor object) {
    preorderVisit(object);
    postorderVisit(object);
  }

  public void visit(LinkedAnchor object) {
    preorderVisit(object);
    postorderVisit(object);
  }

  private void visitFunction(Function object) {
    preorderVisit(object);
    executeVisit(object.param);
    executeVisit(object.ret);
    executeVisit(object.body);
    postorderVisit(object);
  }

  private void executeVisit(Visitee visitee) {
    execute(this, visitee);
  }

  private void executeVisit(AstList<? extends Ast> list) {
    executor.visit(this, list);
  }

  private void execute(Visitor visitor, Visitee visitee) {
    executor.visit(visitor, visitee);
  }

  private void preorderVisit(Visitee object) {
    visitByAll(object, preorderVisitor);
  }

  private void postorderVisit(Visitee object) {
    visitByAll(object, postorderVisitor);
  }

  private void visitByAll(Visitee visitee, Collection<Visitor> visitors) {
    for (Visitor visitor : visitors) {
      execute(visitor, visitee);
    }
  }

}
