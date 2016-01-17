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

import ast.data.Namespace;
import ast.data.component.CompRef;
import ast.data.component.composition.AsynchroniusConnection;
import ast.data.component.composition.CompUse;
import ast.data.component.composition.CompUseRef;
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
import ast.data.function.FuncRef;
import ast.data.function.Function;
import ast.data.function.header.FuncFunction;
import ast.data.function.header.FuncInterrupt;
import ast.data.function.header.FuncProcedure;
import ast.data.function.header.FuncQuery;
import ast.data.function.header.FuncResponse;
import ast.data.function.header.FuncSubHandlerEvent;
import ast.data.function.header.FuncSubHandlerQuery;
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
import ast.data.reference.Reference;
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
import ast.data.variable.ConstGlobal;
import ast.data.variable.ConstPrivate;
import ast.data.variable.FunctionVariable;
import ast.data.variable.StateVariable;
import ast.data.variable.TemplateParameter;
import ast.meta.SourcePosition;

public class DeepFirstTraverser implements Visitor {
  private Visitor preorderVisitor = new NullVisitor();
  private Visitor postorderVisitor = new NullVisitor();

  public Visitor getPreorderVisitor() {
    return preorderVisitor;
  }

  public void setPreorderVisitor(Visitor preorderVisitor) {
    this.preorderVisitor = preorderVisitor;
  }

  public Visitor getPostorderVisitor() {
    return postorderVisitor;
  }

  public void setPostorderVisitor(Visitor postorderVisitor) {
    this.postorderVisitor = postorderVisitor;
  }

  @Override
  public void visit(NamedElementsValue obj) {
    obj.accept(preorderVisitor);
    obj.value.accept(this);
    obj.accept(postorderVisitor);
  }

  @Override
  public void visit(TupleType obj) {
    obj.accept(preorderVisitor);
    obj.types.accept(this);
    obj.accept(postorderVisitor);
  }

  @Override
  public void visit(FuncReturnTuple obj) {
    obj.accept(preorderVisitor);
    obj.param.accept(this);
    obj.accept(postorderVisitor);
  }

  @Override
  public void visit(FunctionReturnType obj) {
    obj.accept(preorderVisitor);
    obj.type.accept(this);
    obj.accept(postorderVisitor);
  }

  @Override
  public void visit(FuncReturnNone obj) {
    obj.accept(preorderVisitor);
    obj.accept(postorderVisitor);
  }

  @Override
  public void visit(ImplElementary obj) {
    obj.accept(preorderVisitor);
    obj.function.accept(this);
    obj.iface.accept(this);
    obj.queue.accept(this);
    obj.type.accept(this);
    obj.constant.accept(this);
    obj.variable.accept(this);
    obj.component.accept(this);
    obj.subCallback.accept(this);
    obj.entryFunc.accept(this);
    obj.exitFunc.accept(this);
    obj.accept(postorderVisitor);
  }

  @Override
  public void visit(ImplComposition obj) {
    obj.accept(preorderVisitor);
    obj.function.accept(this);
    obj.iface.accept(this);
    obj.queue.accept(this);
    obj.component.accept(this);
    obj.connection.accept(this);
    obj.accept(postorderVisitor);
  }

  @Override
  public void visit(ImplHfsm obj) {
    obj.accept(preorderVisitor);
    obj.function.accept(this);
    obj.iface.accept(this);
    obj.queue.accept(this);
    obj.topstate.accept(this);
    obj.accept(postorderVisitor);
  }

  @Override
  public void visit(NumberValue obj) {
    obj.accept(preorderVisitor);
    obj.accept(postorderVisitor);
  }

  @Override
  public void visit(VarDefStmt obj) {
    obj.accept(preorderVisitor);
    obj.variable.accept(this);
    obj.accept(postorderVisitor);
  }

  @Override
  public void visit(CallStmt obj) {
    obj.accept(preorderVisitor);
    obj.call.accept(this);
    obj.accept(postorderVisitor);
  }

  @Override
  public void visit(MultiAssignment obj) {
    obj.accept(preorderVisitor);
    obj.left.accept(this);
    obj.right.accept(this);
    obj.accept(postorderVisitor);
  }

  @Override
  public void visit(AssignmentSingle obj) {
    obj.accept(preorderVisitor);
    obj.left.accept(this);
    obj.right.accept(this);
    obj.accept(postorderVisitor);
  }

  @Override
  public void visit(UnionType obj) {
    obj.accept(preorderVisitor);
    obj.tag.accept(this);
    obj.element.accept(this);
    obj.accept(postorderVisitor);
  }

  @Override
  public void visit(UnsafeUnionType obj) {
    obj.accept(preorderVisitor);
    obj.element.accept(this);
    obj.accept(postorderVisitor);
  }

  @Override
  public void visit(RecordType obj) {
    obj.accept(preorderVisitor);
    obj.element.accept(this);
    obj.accept(postorderVisitor);
  }

  @Override
  public void visit(ComponentType obj) {
    obj.accept(preorderVisitor);
    obj.input.accept(this);
    obj.output.accept(this);
    obj.accept(postorderVisitor);
  }

  @Override
  public void visit(RefName obj) {
    obj.accept(preorderVisitor);
    obj.accept(postorderVisitor);
  }

  @Override
  public void visit(RefIndex obj) {
    obj.accept(preorderVisitor);
    obj.index.accept(this);
    obj.accept(postorderVisitor);
  }

  @Override
  public void visit(RefCall obj) {
    obj.accept(preorderVisitor);
    obj.actualParameter.accept(this);
    obj.accept(postorderVisitor);
  }

  @Override
  public void visit(ArrayType obj) {
    obj.accept(preorderVisitor);
    obj.type.accept(this);
    obj.accept(postorderVisitor);
  }

  @Override
  public void visit(BooleanType obj) {
    obj.accept(preorderVisitor);
    obj.accept(postorderVisitor);
  }

  @Override
  public void visit(EnumType obj) {
    obj.accept(preorderVisitor);
    obj.element.accept(this);
    obj.accept(postorderVisitor);
  }

  @Override
  public void visit(EnumElement obj) {
    obj.accept(preorderVisitor);
    obj.accept(postorderVisitor);
  }

  @Override
  public void visit(ExpressionReturn obj) {
    obj.accept(preorderVisitor);
    obj.expression.accept(this);
    obj.accept(postorderVisitor);
  }

  @Override
  public void visit(VoidReturn obj) {
    obj.accept(preorderVisitor);
    obj.accept(postorderVisitor);
  }

  @Override
  public void visit(VoidType obj) {
    obj.accept(preorderVisitor);
    obj.accept(postorderVisitor);
  }

  @Override
  public void visit(FunctionVariable obj) {
    obj.accept(preorderVisitor);
    obj.type.accept(this);
    obj.accept(postorderVisitor);
  }

  @Override
  public void visit(StateVariable obj) {
    obj.accept(preorderVisitor);
    obj.type.accept(this);
    obj.def.accept(this);
    obj.accept(postorderVisitor);
  }

  @Override
  public void visit(ConstPrivate obj) {
    obj.accept(preorderVisitor);
    obj.type.accept(this);
    obj.def.accept(this);
    obj.accept(postorderVisitor);
  }

  @Override
  public void visit(ConstGlobal obj) {
    obj.accept(preorderVisitor);
    obj.type.accept(this);
    obj.def.accept(this);
    obj.accept(postorderVisitor);
  }

  @Override
  public void visit(SubCallbacks obj) {
    obj.accept(preorderVisitor);
    obj.compUse.accept(this);
    obj.func.accept(this);
    obj.accept(postorderVisitor);
  }

  @Override
  public void visit(Namespace obj) {
    obj.accept(preorderVisitor);
    obj.children.accept(this);
    obj.accept(postorderVisitor);
  }

  @Override
  public void visit(CaseOptRange obj) {
    obj.accept(preorderVisitor);
    obj.start.accept(this);
    obj.end.accept(this);
    obj.accept(postorderVisitor);
  }

  @Override
  public void visit(CaseOptValue obj) {
    obj.accept(preorderVisitor);
    obj.value.accept(this);
    obj.accept(postorderVisitor);
  }

  @Override
  public void visit(BooleanValue obj) {
    obj.accept(preorderVisitor);
    obj.accept(postorderVisitor);
  }

  @Override
  public void visit(SynchroniusConnection obj) {
    obj.accept(preorderVisitor);
    obj.getSrc().accept(this);
    obj.getDst().accept(this);
    obj.accept(postorderVisitor);
  }

  @Override
  public void visit(AsynchroniusConnection obj) {
    obj.accept(preorderVisitor);
    obj.getSrc().accept(this);
    obj.getDst().accept(this);
    obj.accept(postorderVisitor);
  }

  @Override
  public void visit(StringValue obj) {
    obj.accept(preorderVisitor);
    obj.accept(postorderVisitor);
  }

  @Override
  public void visit(StringType obj) {
    obj.accept(preorderVisitor);
    obj.accept(postorderVisitor);
  }

  @Override
  public void visit(ArrayValue obj) {
    obj.accept(preorderVisitor);
    obj.value.accept(this);
    obj.accept(postorderVisitor);
  }

  @Override
  public void visit(TupleValue obj) {
    obj.accept(preorderVisitor);
    obj.value.accept(this);
    obj.accept(postorderVisitor);
  }

  @Override
  public void visit(StateSimple obj) {
    obj.accept(preorderVisitor);
    obj.entryFunc.accept(this);
    obj.exitFunc.accept(this);
    obj.item.accept(this);
    obj.accept(postorderVisitor);
  }

  @Override
  public void visit(StateComposite obj) {
    obj.accept(preorderVisitor);
    obj.entryFunc.accept(this);
    obj.exitFunc.accept(this);
    obj.item.accept(this);
    obj.initial.accept(this);
    obj.accept(postorderVisitor);
  }

  @Override
  public void visit(Transition obj) {
    obj.accept(preorderVisitor);
    obj.src.accept(this);
    obj.dst.accept(this);
    obj.eventFunc.accept(this);
    obj.param.accept(this);
    obj.guard.accept(this);
    obj.body.accept(this);
    obj.accept(postorderVisitor);
  }

  @Override
  public void visit(Reference obj) {
    obj.accept(preorderVisitor);
    obj.offset.accept(this);
    obj.accept(postorderVisitor);
  }

  @Override
  public void visit(NaturalType obj) {
    obj.accept(preorderVisitor);
    obj.accept(postorderVisitor);
  }

  @Override
  public void visit(IntegerType obj) {
    obj.accept(preorderVisitor);
    obj.accept(postorderVisitor);
  }

  @Override
  public void visit(FuncProcedure obj) {
    visitFunction(obj);
  }

  @Override
  public void visit(FuncFunction obj) {
    visitFunction(obj);
  }

  @Override
  public void visit(FuncSubHandlerQuery obj) {
    visitFunction(obj);
  }

  @Override
  public void visit(FuncSubHandlerEvent obj) {
    visitFunction(obj);
  }

  @Override
  public void visit(FunctionType obj) {
    obj.accept(preorderVisitor);
    throw new RuntimeException("not yet implemented");
  }

  @Override
  public void visit(EndpointSelf obj) {
    obj.accept(preorderVisitor);
    obj.funcRef.accept(this);
    obj.accept(postorderVisitor);
  }

  @Override
  public void visit(EndpointSub obj) {
    obj.accept(preorderVisitor);
    obj.component.accept(this);
    obj.accept(postorderVisitor);
  }

  @Override
  public void visit(EndpointRaw obj) {
    obj.accept(preorderVisitor);
    obj.ref.accept(this);
    obj.accept(postorderVisitor);
  }

  @Override
  public void visit(And obj) {
    obj.accept(preorderVisitor);
    obj.left.accept(this);
    obj.right.accept(this);
    obj.accept(postorderVisitor);
  }

  @Override
  public void visit(Division obj) {
    obj.accept(preorderVisitor);
    obj.left.accept(this);
    obj.right.accept(this);
    obj.accept(postorderVisitor);
  }

  @Override
  public void visit(Equal obj) {
    obj.accept(preorderVisitor);
    obj.left.accept(this);
    obj.right.accept(this);
    obj.accept(postorderVisitor);
  }

  @Override
  public void visit(Greater obj) {
    obj.accept(preorderVisitor);
    obj.left.accept(this);
    obj.right.accept(this);
    obj.accept(postorderVisitor);
  }

  @Override
  public void visit(GreaterEqual obj) {
    obj.accept(preorderVisitor);
    obj.left.accept(this);
    obj.right.accept(this);
    obj.accept(postorderVisitor);
  }

  @Override
  public void visit(Less obj) {
    obj.accept(preorderVisitor);
    obj.left.accept(this);
    obj.right.accept(this);
    obj.accept(postorderVisitor);
  }

  @Override
  public void visit(Is obj) {
    obj.accept(preorderVisitor);
    obj.left.accept(this);
    obj.right.accept(this);
    obj.accept(postorderVisitor);
  }

  @Override
  public void visit(LessEqual obj) {
    obj.accept(preorderVisitor);
    obj.left.accept(this);
    obj.right.accept(this);
    obj.accept(postorderVisitor);
  }

  @Override
  public void visit(Minus obj) {
    obj.accept(preorderVisitor);
    obj.left.accept(this);
    obj.right.accept(this);
    obj.accept(postorderVisitor);
  }

  @Override
  public void visit(Modulo obj) {
    obj.accept(preorderVisitor);
    obj.left.accept(this);
    obj.right.accept(this);
    obj.accept(postorderVisitor);
  }

  @Override
  public void visit(Multiplication obj) {
    obj.accept(preorderVisitor);
    obj.left.accept(this);
    obj.right.accept(this);
    obj.accept(postorderVisitor);
  }

  @Override
  public void visit(Not obj) {
    obj.accept(preorderVisitor);
    obj.expression.accept(this);
    obj.accept(postorderVisitor);
  }

  @Override
  public void visit(LogicNot obj) {
    obj.accept(preorderVisitor);
    obj.expression.accept(this);
    obj.accept(postorderVisitor);
  }

  @Override
  public void visit(BitNot obj) {
    obj.accept(preorderVisitor);
    obj.expression.accept(this);
    obj.accept(postorderVisitor);
  }

  @Override
  public void visit(NotEqual obj) {
    obj.accept(preorderVisitor);
    obj.left.accept(this);
    obj.right.accept(this);
    obj.accept(postorderVisitor);
  }

  @Override
  public void visit(Or obj) {
    obj.accept(preorderVisitor);
    obj.left.accept(this);
    obj.right.accept(this);
    obj.accept(postorderVisitor);
  }

  @Override
  public void visit(Plus obj) {
    obj.accept(preorderVisitor);
    obj.left.accept(this);
    obj.right.accept(this);
    obj.accept(postorderVisitor);
  }

  @Override
  public void visit(Shl obj) {
    obj.accept(preorderVisitor);
    obj.left.accept(this);
    obj.right.accept(this);
    obj.accept(postorderVisitor);
  }

  @Override
  public void visit(Shr obj) {
    obj.accept(preorderVisitor);
    obj.left.accept(this);
    obj.right.accept(this);
    obj.accept(postorderVisitor);
  }

  @Override
  public void visit(Uminus obj) {
    obj.accept(preorderVisitor);
    obj.expression.accept(this);
    obj.accept(postorderVisitor);
  }

  @Override
  public void visit(RangeType obj) {
    obj.accept(preorderVisitor);
    obj.accept(postorderVisitor);
  }

  @Override
  public void visit(WhileStmt obj) {
    obj.accept(preorderVisitor);
    obj.condition.accept(this);
    obj.body.accept(this);
    obj.accept(postorderVisitor);
  }

  @Override
  public void visit(IfStatement obj) {
    obj.accept(preorderVisitor);
    obj.option.accept(this);
    obj.defblock.accept(this);
    obj.accept(postorderVisitor);
  }

  @Override
  public void visit(IfOption obj) {
    obj.accept(preorderVisitor);
    obj.condition.accept(this);
    obj.code.accept(this);
    obj.accept(postorderVisitor);
  }

  @Override
  public void visit(Block obj) {
    obj.accept(preorderVisitor);
    obj.statements.accept(this);
    obj.accept(postorderVisitor);
  }

  @Override
  public void visit(CaseStmt obj) {
    obj.accept(preorderVisitor);
    obj.condition.accept(this);
    obj.option.accept(this);
    obj.otherwise.accept(this);
    obj.accept(postorderVisitor);
  }

  @Override
  public void visit(CaseOpt obj) {
    obj.accept(preorderVisitor);
    obj.value.accept(this);
    obj.code.accept(this);
    obj.accept(postorderVisitor);
  }

  @Override
  public void visit(TypeCast obj) {
    obj.accept(preorderVisitor);
    obj.cast.accept(this);
    obj.value.accept(this);
    obj.accept(postorderVisitor);
  }

  @Override
  public void visit(BitAnd obj) {
    obj.accept(preorderVisitor);
    obj.left.accept(this);
    obj.right.accept(this);
    obj.accept(postorderVisitor);
  }

  @Override
  public void visit(BitOr obj) {
    obj.accept(preorderVisitor);
    obj.left.accept(this);
    obj.right.accept(this);
    obj.accept(postorderVisitor);
  }

  @Override
  public void visit(BitXor obj) {
    obj.accept(preorderVisitor);
    obj.left.accept(this);
    obj.right.accept(this);
    obj.accept(postorderVisitor);
  }

  @Override
  public void visit(LogicOr obj) {
    obj.accept(preorderVisitor);
    obj.left.accept(this);
    obj.right.accept(this);
    obj.accept(postorderVisitor);
  }

  @Override
  public void visit(LogicAnd obj) {
    obj.accept(preorderVisitor);
    obj.left.accept(this);
    obj.right.accept(this);
    obj.accept(postorderVisitor);
  }

  @Override
  public void visit(Signal obj) {
    visitFunction(obj);
  }

  @Override
  public void visit(FuncQuery obj) {
    visitFunction(obj);
  }

  @Override
  public void visit(Slot obj) {
    visitFunction(obj);
  }

  @Override
  public void visit(FuncResponse obj) {
    visitFunction(obj);
  }

  @Override
  public void visit(AnyType obj) {
    obj.accept(preorderVisitor);
    obj.accept(postorderVisitor);
  }

  @Override
  public void visit(AnyValue obj) {
    obj.accept(preorderVisitor);
    obj.accept(postorderVisitor);
  }

  @Override
  public void visit(NamedValue obj) {
    obj.accept(preorderVisitor);
    obj.value.accept(this);
    obj.accept(postorderVisitor);
  }

  @Override
  public void visit(UnionValue obj) {
    obj.accept(preorderVisitor);
    obj.tagValue.accept(this);
    obj.contentValue.accept(this);
    obj.type.accept(this);
    obj.accept(postorderVisitor);
  }

  @Override
  public void visit(UnsafeUnionValue obj) {
    obj.accept(preorderVisitor);
    obj.contentValue.accept(this);
    obj.type.accept(this);
    obj.accept(postorderVisitor);
  }

  @Override
  public void visit(RecordValue obj) {
    obj.accept(preorderVisitor);
    obj.value.accept(this);
    obj.type.accept(this);
    obj.accept(postorderVisitor);
  }

  @Override
  public void visit(MsgPush obj) {
    obj.accept(preorderVisitor);
    obj.queue.accept(this);
    obj.func.accept(this);
    obj.data.accept(this);
    obj.accept(postorderVisitor);
  }

  @Override
  public void visit(Queue obj) {
    obj.accept(preorderVisitor);
    obj.accept(postorderVisitor);
  }

  @Override
  public void visit(NamedElement obj) {
    obj.accept(preorderVisitor);
    obj.typeref.accept(this);
    obj.accept(postorderVisitor);
  }

  @Override
  public void visit(CompUse obj) {
    obj.accept(preorderVisitor);
    obj.compRef.accept(this);
    obj.accept(postorderVisitor);
  }

  @Override
  public void visit(UIntType obj) {
    obj.accept(preorderVisitor);
    obj.accept(postorderVisitor);
  }

  @Override
  public void visit(SIntType obj) {
    obj.accept(preorderVisitor);
    obj.accept(postorderVisitor);
  }

  @Override
  public void visit(AliasType obj) {
    obj.accept(preorderVisitor);
    obj.ref.accept(this);
    obj.accept(postorderVisitor);
  }

  @Override
  public void visit(ForStmt obj) {
    obj.accept(preorderVisitor);
    obj.iterator.accept(this);
    obj.block.accept(this);
    obj.accept(postorderVisitor);
  }

  @Override
  public void visit(DefaultValueTemplate obj) {
    obj.accept(preorderVisitor);
    obj.accept(postorderVisitor);
  }

  @Override
  public void visit(RizzlyFile obj) {
    obj.accept(preorderVisitor);
    obj.objects.accept(this);
    obj.accept(postorderVisitor);
  }

  @Override
  public void visit(RawElementary obj) {
    obj.accept(preorderVisitor);
    obj.getEntryFunc().accept(this);
    obj.getExitFunc().accept(this);
    obj.getIface().accept(this);
    obj.getDeclaration().accept(this);
    obj.getInstantiation().accept(this);
    obj.accept(postorderVisitor);
  }

  @Override
  public void visit(RawComposition obj) {
    obj.accept(preorderVisitor);
    obj.getIface().accept(this);
    obj.getInstantiation().accept(this);
    obj.getConnection().accept(this);
    obj.accept(postorderVisitor);
  }

  @Override
  public void visit(RawHfsm obj) {
    obj.accept(preorderVisitor);
    obj.getIface().accept(this);
    obj.getTopstate().accept(this);
    obj.accept(postorderVisitor);
  }

  @Override
  public void visit(VarDefInitStmt obj) {
    obj.accept(preorderVisitor);
    obj.variable.accept(this);
    obj.initial.accept(this);
    obj.accept(postorderVisitor);
  }

  @Override
  public void visit(RefTemplCall obj) {
    obj.accept(preorderVisitor);
    obj.actualParameter.accept(this);
    obj.accept(postorderVisitor);
  }

  @Override
  public void visit(TemplateParameter obj) {
    obj.accept(preorderVisitor);
    obj.type.accept(this);
    obj.accept(postorderVisitor);
  }

  @Override
  public void visit(TypeTypeTemplate obj) {
    obj.accept(preorderVisitor);
    obj.accept(postorderVisitor);
  }

  @Override
  public void visit(TypeType obj) {
    obj.accept(preorderVisitor);
    obj.accept(postorderVisitor);
  }

  @Override
  public void visit(RangeTemplate obj) {
    obj.accept(preorderVisitor);
    obj.accept(postorderVisitor);
  }

  @Override
  public void visit(Template obj) {
    obj.accept(preorderVisitor);
    obj.getTempl().accept(this);
    obj.getObject().accept(this);
    obj.accept(postorderVisitor);
  }

  @Override
  public void visit(ArrayTemplate obj) {
    obj.accept(preorderVisitor);
    obj.accept(postorderVisitor);
  }

  @Override
  public void visit(LinkTarget obj) {
    obj.accept(preorderVisitor);
    obj.accept(postorderVisitor);
  }

  @Override
  public void visit(ReferenceExpression obj) {
    obj.accept(preorderVisitor);
    obj.reference.accept(this);
    obj.accept(postorderVisitor);
  }

  @Override
  public void visit(TypeReference obj) {
    obj.accept(preorderVisitor);
    obj.ref.accept(this);
    obj.accept(postorderVisitor);
  }

  @Override
  public void visit(FuncRef obj) {
    obj.accept(preorderVisitor);
    obj.ref.accept(this);
    obj.accept(postorderVisitor);
  }

  @Override
  public void visit(StateRef obj) {
    obj.accept(preorderVisitor);
    obj.ref.accept(this);
    obj.accept(postorderVisitor);
  }

  @Override
  public void visit(CompRef obj) {
    obj.accept(preorderVisitor);
    obj.ref.accept(this);
    obj.accept(postorderVisitor);
  }

  @Override
  public void visit(CompUseRef obj) {
    obj.accept(preorderVisitor);
    obj.ref.accept(this);
    obj.accept(postorderVisitor);
  }

  @Override
  public void visit(CaseOptSimple caseOptSimple) {
    throw new RuntimeException("not yet implemented");
  }

  @Override
  public void visit(FuncInterrupt obj) {
    visitFunction(obj);
  }

  @Override
  public void visit(PointerType pointerType) {
    throw new RuntimeException("not yet implemented");
  }

  @Override
  public void visit(SourcePosition obj) {
    obj.accept(preorderVisitor);
    obj.accept(postorderVisitor);
  }

  private void visitFunction(Function obj) {
    obj.accept(preorderVisitor);
    obj.param.accept(this);
    obj.ret.accept(this);
    obj.body.accept(this);
    obj.accept(postorderVisitor);
  }

}
