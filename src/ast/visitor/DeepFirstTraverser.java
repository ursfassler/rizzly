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
import ast.data.reference.LinkedReferenceWithOffset_Implementation;
import ast.data.reference.RefCall;
import ast.data.reference.RefIndex;
import ast.data.reference.RefName;
import ast.data.reference.RefTemplCall;
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
  final private Collection<Visitor> preorderVisitor = new ArrayList<Visitor>();
  final private Collection<Visitor> postorderVisitor = new ArrayList<Visitor>();

  public void addPreorderVisitor(Visitor visitor) {
    preorderVisitor.add(visitor);
  }

  public void addPostorderVisitor(Visitor visitor) {
    postorderVisitor.add(visitor);
  }

  @Override
  public void visit(NamedElementsValue object) {
    preorderVisit(object);
    object.value.accept(this);
    postorderVisit(object);
  }

  @Override
  public void visit(TupleType object) {
    preorderVisit(object);
    object.types.accept(this);
    postorderVisit(object);
  }

  @Override
  public void visit(FuncReturnTuple object) {
    preorderVisit(object);
    object.param.accept(this);
    postorderVisit(object);
  }

  @Override
  public void visit(FunctionReturnType object) {
    preorderVisit(object);
    object.type.accept(this);
    postorderVisit(object);
  }

  @Override
  public void visit(FuncReturnNone object) {
    preorderVisit(object);
    postorderVisit(object);
  }

  @Override
  public void visit(ImplElementary object) {
    preorderVisit(object);
    object.function.accept(this);
    object.iface.accept(this);
    object.queue.accept(this);
    object.type.accept(this);
    object.constant.accept(this);
    object.variable.accept(this);
    object.component.accept(this);
    object.subCallback.accept(this);
    object.entryFunc.accept(this);
    object.exitFunc.accept(this);
    postorderVisit(object);
  }

  @Override
  public void visit(ImplComposition object) {
    preorderVisit(object);
    object.function.accept(this);
    object.iface.accept(this);
    object.queue.accept(this);
    object.component.accept(this);
    object.connection.accept(this);
    postorderVisit(object);
  }

  @Override
  public void visit(ImplHfsm object) {
    preorderVisit(object);
    object.function.accept(this);
    object.iface.accept(this);
    object.queue.accept(this);
    object.topstate.accept(this);
    postorderVisit(object);
  }

  @Override
  public void visit(NumberValue object) {
    preorderVisit(object);
    postorderVisit(object);
  }

  @Override
  public void visit(VarDefStmt object) {
    preorderVisit(object);
    object.variable.accept(this);
    postorderVisit(object);
  }

  @Override
  public void visit(CallStmt object) {
    preorderVisit(object);
    object.call.accept(this);
    postorderVisit(object);
  }

  @Override
  public void visit(MultiAssignment object) {
    preorderVisit(object);
    object.left.accept(this);
    object.right.accept(this);
    postorderVisit(object);
  }

  @Override
  public void visit(AssignmentSingle object) {
    preorderVisit(object);
    object.left.accept(this);
    object.right.accept(this);
    postorderVisit(object);
  }

  @Override
  public void visit(UnionType object) {
    preorderVisit(object);
    object.tag.accept(this);
    object.element.accept(this);
    postorderVisit(object);
  }

  @Override
  public void visit(UnsafeUnionType object) {
    preorderVisit(object);
    object.element.accept(this);
    postorderVisit(object);
  }

  @Override
  public void visit(RecordType object) {
    preorderVisit(object);
    object.element.accept(this);
    postorderVisit(object);
  }

  @Override
  public void visit(ComponentType object) {
    preorderVisit(object);
    object.input.accept(this);
    object.output.accept(this);
    postorderVisit(object);
  }

  @Override
  public void visit(RefName object) {
    preorderVisit(object);
    postorderVisit(object);
  }

  @Override
  public void visit(RefIndex object) {
    preorderVisit(object);
    object.index.accept(this);
    postorderVisit(object);
  }

  @Override
  public void visit(RefCall object) {
    preorderVisit(object);
    object.actualParameter.accept(this);
    postorderVisit(object);
  }

  @Override
  public void visit(ArrayType object) {
    preorderVisit(object);
    object.type.accept(this);
    postorderVisit(object);
  }

  @Override
  public void visit(BooleanType object) {
    preorderVisit(object);
    postorderVisit(object);
  }

  @Override
  public void visit(EnumType object) {
    preorderVisit(object);
    object.element.accept(this);
    postorderVisit(object);
  }

  @Override
  public void visit(EnumElement object) {
    preorderVisit(object);
    postorderVisit(object);
  }

  @Override
  public void visit(ExpressionReturn object) {
    preorderVisit(object);
    object.expression.accept(this);
    postorderVisit(object);
  }

  @Override
  public void visit(VoidReturn object) {
    preorderVisit(object);
    postorderVisit(object);
  }

  @Override
  public void visit(VoidType object) {
    preorderVisit(object);
    postorderVisit(object);
  }

  @Override
  public void visit(FunctionVariable object) {
    preorderVisit(object);
    object.type.accept(this);
    postorderVisit(object);
  }

  @Override
  public void visit(StateVariable object) {
    preorderVisit(object);
    object.type.accept(this);
    object.def.accept(this);
    postorderVisit(object);
  }

  @Override
  public void visit(ConstPrivate object) {
    preorderVisit(object);
    object.type.accept(this);
    object.def.accept(this);
    postorderVisit(object);
  }

  @Override
  public void visit(GlobalConstant object) {
    preorderVisit(object);
    object.type.accept(this);
    object.def.accept(this);
    postorderVisit(object);
  }

  @Override
  public void visit(SubCallbacks object) {
    preorderVisit(object);
    object.compUse.accept(this);
    object.func.accept(this);
    postorderVisit(object);
  }

  @Override
  public void visit(Namespace object) {
    preorderVisit(object);
    object.children.accept(this);
    postorderVisit(object);
  }

  @Override
  public void visit(CaseOptRange object) {
    preorderVisit(object);
    object.start.accept(this);
    object.end.accept(this);
    postorderVisit(object);
  }

  @Override
  public void visit(CaseOptValue object) {
    preorderVisit(object);
    object.value.accept(this);
    postorderVisit(object);
  }

  @Override
  public void visit(BooleanValue object) {
    preorderVisit(object);
    postorderVisit(object);
  }

  @Override
  public void visit(SynchroniusConnection object) {
    preorderVisit(object);
    object.getSrc().accept(this);
    object.getDst().accept(this);
    postorderVisit(object);
  }

  @Override
  public void visit(AsynchroniusConnection object) {
    preorderVisit(object);
    object.getSrc().accept(this);
    object.getDst().accept(this);
    postorderVisit(object);
  }

  @Override
  public void visit(StringValue object) {
    preorderVisit(object);
    postorderVisit(object);
  }

  @Override
  public void visit(StringType object) {
    preorderVisit(object);
    postorderVisit(object);
  }

  @Override
  public void visit(ArrayValue object) {
    preorderVisit(object);
    object.value.accept(this);
    postorderVisit(object);
  }

  @Override
  public void visit(TupleValue object) {
    preorderVisit(object);
    object.value.accept(this);
    postorderVisit(object);
  }

  @Override
  public void visit(StateSimple object) {
    preorderVisit(object);
    object.entryFunc.accept(this);
    object.exitFunc.accept(this);
    object.item.accept(this);
    postorderVisit(object);
  }

  @Override
  public void visit(StateComposite object) {
    preorderVisit(object);
    object.entryFunc.accept(this);
    object.exitFunc.accept(this);
    object.item.accept(this);
    object.initial.accept(this);
    postorderVisit(object);
  }

  @Override
  public void visit(Transition object) {
    preorderVisit(object);
    object.src.accept(this);
    object.dst.accept(this);
    object.eventFunc.accept(this);
    object.param.accept(this);
    object.guard.accept(this);
    object.body.accept(this);
    postorderVisit(object);
  }

  @Override
  public void visit(LinkedReferenceWithOffset_Implementation object) {
    preorderVisit(object);
    object.getOffset().accept(this);
    postorderVisit(object);
  }

  @Override
  public void visit(NaturalType object) {
    preorderVisit(object);
    postorderVisit(object);
  }

  @Override
  public void visit(IntegerType object) {
    preorderVisit(object);
    postorderVisit(object);
  }

  @Override
  public void visit(Procedure object) {
    visitFunction(object);
  }

  @Override
  public void visit(FuncFunction object) {
    visitFunction(object);
  }

  @Override
  public void visit(FuncSubHandlerQuery object) {
    visitFunction(object);
  }

  @Override
  public void visit(FuncSubHandlerEvent object) {
    visitFunction(object);
  }

  @Override
  public void visit(FunctionType object) {
    preorderVisit(object);
    throw new RuntimeException("not yet implemented");
  }

  @Override
  public void visit(EndpointSelf object) {
    preorderVisit(object);
    object.funcRef.accept(this);
    postorderVisit(object);
  }

  @Override
  public void visit(EndpointSub object) {
    preorderVisit(object);
    object.component.accept(this);
    postorderVisit(object);
  }

  @Override
  public void visit(EndpointRaw object) {
    preorderVisit(object);
    object.ref.accept(this);
    postorderVisit(object);
  }

  @Override
  public void visit(And object) {
    preorderVisit(object);
    object.left.accept(this);
    object.right.accept(this);
    postorderVisit(object);
  }

  @Override
  public void visit(Division object) {
    preorderVisit(object);
    object.left.accept(this);
    object.right.accept(this);
    postorderVisit(object);
  }

  @Override
  public void visit(Equal object) {
    preorderVisit(object);
    object.left.accept(this);
    object.right.accept(this);
    postorderVisit(object);
  }

  @Override
  public void visit(Greater object) {
    preorderVisit(object);
    object.left.accept(this);
    object.right.accept(this);
    postorderVisit(object);
  }

  @Override
  public void visit(GreaterEqual object) {
    preorderVisit(object);
    object.left.accept(this);
    object.right.accept(this);
    postorderVisit(object);
  }

  @Override
  public void visit(Less object) {
    preorderVisit(object);
    object.left.accept(this);
    object.right.accept(this);
    postorderVisit(object);
  }

  @Override
  public void visit(Is object) {
    preorderVisit(object);
    object.left.accept(this);
    object.right.accept(this);
    postorderVisit(object);
  }

  @Override
  public void visit(LessEqual object) {
    preorderVisit(object);
    object.left.accept(this);
    object.right.accept(this);
    postorderVisit(object);
  }

  @Override
  public void visit(Minus object) {
    preorderVisit(object);
    object.left.accept(this);
    object.right.accept(this);
    postorderVisit(object);
  }

  @Override
  public void visit(Modulo object) {
    preorderVisit(object);
    object.left.accept(this);
    object.right.accept(this);
    postorderVisit(object);
  }

  @Override
  public void visit(Multiplication object) {
    preorderVisit(object);
    object.left.accept(this);
    object.right.accept(this);
    postorderVisit(object);
  }

  @Override
  public void visit(Not object) {
    preorderVisit(object);
    object.expression.accept(this);
    postorderVisit(object);
  }

  @Override
  public void visit(LogicNot object) {
    preorderVisit(object);
    object.expression.accept(this);
    postorderVisit(object);
  }

  @Override
  public void visit(BitNot object) {
    preorderVisit(object);
    object.expression.accept(this);
    postorderVisit(object);
  }

  @Override
  public void visit(NotEqual object) {
    preorderVisit(object);
    object.left.accept(this);
    object.right.accept(this);
    postorderVisit(object);
  }

  @Override
  public void visit(Or object) {
    preorderVisit(object);
    object.left.accept(this);
    object.right.accept(this);
    postorderVisit(object);
  }

  @Override
  public void visit(Plus object) {
    preorderVisit(object);
    object.left.accept(this);
    object.right.accept(this);
    postorderVisit(object);
  }

  @Override
  public void visit(Shl object) {
    preorderVisit(object);
    object.left.accept(this);
    object.right.accept(this);
    postorderVisit(object);
  }

  @Override
  public void visit(Shr object) {
    preorderVisit(object);
    object.left.accept(this);
    object.right.accept(this);
    postorderVisit(object);
  }

  @Override
  public void visit(Uminus object) {
    preorderVisit(object);
    object.expression.accept(this);
    postorderVisit(object);
  }

  @Override
  public void visit(RangeType object) {
    preorderVisit(object);
    postorderVisit(object);
  }

  @Override
  public void visit(WhileStmt object) {
    preorderVisit(object);
    object.condition.accept(this);
    object.body.accept(this);
    postorderVisit(object);
  }

  @Override
  public void visit(IfStatement object) {
    preorderVisit(object);
    object.option.accept(this);
    object.defblock.accept(this);
    postorderVisit(object);
  }

  @Override
  public void visit(IfOption object) {
    preorderVisit(object);
    object.condition.accept(this);
    object.code.accept(this);
    postorderVisit(object);
  }

  @Override
  public void visit(Block object) {
    preorderVisit(object);
    object.statements.accept(this);
    postorderVisit(object);
  }

  @Override
  public void visit(CaseStmt object) {
    preorderVisit(object);
    object.condition.accept(this);
    object.option.accept(this);
    object.otherwise.accept(this);
    postorderVisit(object);
  }

  @Override
  public void visit(CaseOpt object) {
    preorderVisit(object);
    object.value.accept(this);
    object.code.accept(this);
    postorderVisit(object);
  }

  @Override
  public void visit(TypeCast object) {
    preorderVisit(object);
    object.cast.accept(this);
    object.value.accept(this);
    postorderVisit(object);
  }

  @Override
  public void visit(BitAnd object) {
    preorderVisit(object);
    object.left.accept(this);
    object.right.accept(this);
    postorderVisit(object);
  }

  @Override
  public void visit(BitOr object) {
    preorderVisit(object);
    object.left.accept(this);
    object.right.accept(this);
    postorderVisit(object);
  }

  @Override
  public void visit(BitXor object) {
    preorderVisit(object);
    object.left.accept(this);
    object.right.accept(this);
    postorderVisit(object);
  }

  @Override
  public void visit(LogicOr object) {
    preorderVisit(object);
    object.left.accept(this);
    object.right.accept(this);
    postorderVisit(object);
  }

  @Override
  public void visit(LogicAnd object) {
    preorderVisit(object);
    object.left.accept(this);
    object.right.accept(this);
    postorderVisit(object);
  }

  @Override
  public void visit(Signal object) {
    visitFunction(object);
  }

  @Override
  public void visit(FuncQuery object) {
    visitFunction(object);
  }

  @Override
  public void visit(Slot object) {
    visitFunction(object);
  }

  @Override
  public void visit(Response object) {
    visitFunction(object);
  }

  @Override
  public void visit(AnyType object) {
    preorderVisit(object);
    postorderVisit(object);
  }

  @Override
  public void visit(AnyValue object) {
    preorderVisit(object);
    postorderVisit(object);
  }

  @Override
  public void visit(NamedValue object) {
    preorderVisit(object);
    object.value.accept(this);
    postorderVisit(object);
  }

  @Override
  public void visit(UnionValue object) {
    preorderVisit(object);
    object.tagValue.accept(this);
    object.contentValue.accept(this);
    object.type.accept(this);
    postorderVisit(object);
  }

  @Override
  public void visit(UnsafeUnionValue object) {
    preorderVisit(object);
    object.contentValue.accept(this);
    object.type.accept(this);
    postorderVisit(object);
  }

  @Override
  public void visit(RecordValue object) {
    preorderVisit(object);
    object.value.accept(this);
    object.type.accept(this);
    postorderVisit(object);
  }

  @Override
  public void visit(MsgPush object) {
    preorderVisit(object);
    object.queue.accept(this);
    object.func.accept(this);
    object.data.accept(this);
    postorderVisit(object);
  }

  @Override
  public void visit(Queue object) {
    preorderVisit(object);
    postorderVisit(object);
  }

  @Override
  public void visit(NamedElement object) {
    preorderVisit(object);
    object.typeref.accept(this);
    postorderVisit(object);
  }

  @Override
  public void visit(ComponentUse object) {
    preorderVisit(object);
    object.compRef.accept(this);
    postorderVisit(object);
  }

  @Override
  public void visit(UIntType object) {
    preorderVisit(object);
    postorderVisit(object);
  }

  @Override
  public void visit(SIntType object) {
    preorderVisit(object);
    postorderVisit(object);
  }

  @Override
  public void visit(AliasType object) {
    preorderVisit(object);
    object.ref.accept(this);
    postorderVisit(object);
  }

  @Override
  public void visit(ForStmt object) {
    preorderVisit(object);
    object.iterator.accept(this);
    object.block.accept(this);
    postorderVisit(object);
  }

  @Override
  public void visit(DefaultValueTemplate object) {
    preorderVisit(object);
    postorderVisit(object);
  }

  @Override
  public void visit(RizzlyFile object) {
    preorderVisit(object);
    object.objects.accept(this);
    postorderVisit(object);
  }

  @Override
  public void visit(RawElementary object) {
    preorderVisit(object);
    object.getEntryFunc().accept(this);
    object.getExitFunc().accept(this);
    object.getIface().accept(this);
    object.getDeclaration().accept(this);
    object.getInstantiation().accept(this);
    postorderVisit(object);
  }

  @Override
  public void visit(RawComposition object) {
    preorderVisit(object);
    object.getIface().accept(this);
    object.getInstantiation().accept(this);
    object.getConnection().accept(this);
    postorderVisit(object);
  }

  @Override
  public void visit(RawHfsm object) {
    preorderVisit(object);
    object.getIface().accept(this);
    object.getTopstate().accept(this);
    postorderVisit(object);
  }

  @Override
  public void visit(VarDefInitStmt object) {
    preorderVisit(object);
    object.variable.accept(this);
    object.initial.accept(this);
    postorderVisit(object);
  }

  @Override
  public void visit(RefTemplCall object) {
    preorderVisit(object);
    object.actualParameter.accept(this);
    postorderVisit(object);
  }

  @Override
  public void visit(TemplateParameter object) {
    preorderVisit(object);
    object.type.accept(this);
    postorderVisit(object);
  }

  @Override
  public void visit(TypeTypeTemplate object) {
    preorderVisit(object);
    postorderVisit(object);
  }

  @Override
  public void visit(TypeType object) {
    preorderVisit(object);
    object.type.accept(this);
    postorderVisit(object);
  }

  @Override
  public void visit(RangeTemplate object) {
    preorderVisit(object);
    postorderVisit(object);
  }

  @Override
  public void visit(Template object) {
    preorderVisit(object);
    object.getTempl().accept(this);
    object.getObject().accept(this);
    postorderVisit(object);
  }

  @Override
  public void visit(ArrayTemplate object) {
    preorderVisit(object);
    postorderVisit(object);
  }

  @Override
  public void visit(LinkTarget object) {
    preorderVisit(object);
    postorderVisit(object);
  }

  @Override
  public void visit(ReferenceExpression object) {
    preorderVisit(object);
    object.reference.accept(this);
    postorderVisit(object);
  }

  @Override
  public void visit(TypeReference object) {
    preorderVisit(object);
    object.ref.accept(this);
    postorderVisit(object);
  }

  @Override
  public void visit(FunctionReference object) {
    preorderVisit(object);
    object.ref.accept(this);
    postorderVisit(object);
  }

  @Override
  public void visit(StateRef object) {
    preorderVisit(object);
    object.ref.accept(this);
    postorderVisit(object);
  }

  @Override
  public void visit(ComponentReference object) {
    preorderVisit(object);
    object.ref.accept(this);
    postorderVisit(object);
  }

  @Override
  public void visit(CompUseRef object) {
    preorderVisit(object);
    object.ref.accept(this);
    postorderVisit(object);
  }

  @Override
  public void visit(CaseOptSimple caseOptSimple) {
    throw new RuntimeException("not yet implemented");
  }

  @Override
  public void visit(FuncInterrupt object) {
    visitFunction(object);
  }

  @Override
  public void visit(PointerType pointerType) {
    throw new RuntimeException("not yet implemented");
  }

  @Override
  public void visit(SourcePosition object) {
    preorderVisit(object);
    postorderVisit(object);
  }

  private void visitFunction(Function object) {
    preorderVisit(object);
    object.param.accept(this);
    object.ret.accept(this);
    object.body.accept(this);
    postorderVisit(object);
  }

  private void preorderVisit(VisitorAcceptor object) {
    visitByAll(object, preorderVisitor);
  }

  private void postorderVisit(VisitorAcceptor object) {
    visitByAll(object, postorderVisitor);
  }

  private void visitByAll(VisitorAcceptor object, Collection<Visitor> visitors) {
    for (Visitor visitor : visitors) {
      object.accept(visitor);
    }
  }

  @Override
  public void visit(UnlinkedReferenceWithOffset_Implementation object) {
    throw new RuntimeException("not yet implemented");
  }

}
