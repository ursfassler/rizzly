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

package evl.traverser;

import common.Direction;

import evl.data.Namespace;
import evl.data.component.composition.AsynchroniusConnection;
import evl.data.component.composition.CompUse;
import evl.data.component.composition.EndpointSelf;
import evl.data.component.composition.EndpointSub;
import evl.data.component.composition.ImplComposition;
import evl.data.component.composition.Queue;
import evl.data.component.composition.SubCallbacks;
import evl.data.component.composition.SynchroniusConnection;
import evl.data.component.elementary.ImplElementary;
import evl.data.component.hfsm.ImplHfsm;
import evl.data.component.hfsm.StateComposite;
import evl.data.component.hfsm.StateSimple;
import evl.data.component.hfsm.Transition;
import evl.data.expression.AnyValue;
import evl.data.expression.ArrayValue;
import evl.data.expression.BoolValue;
import evl.data.expression.NamedElementsValue;
import evl.data.expression.NamedValue;
import evl.data.expression.Number;
import evl.data.expression.RecordValue;
import evl.data.expression.StringValue;
import evl.data.expression.TupleValue;
import evl.data.expression.TypeCast;
import evl.data.expression.UnionValue;
import evl.data.expression.UnsafeUnionValue;
import evl.data.expression.binop.And;
import evl.data.expression.binop.BitAnd;
import evl.data.expression.binop.BitOr;
import evl.data.expression.binop.BitXor;
import evl.data.expression.binop.Div;
import evl.data.expression.binop.Equal;
import evl.data.expression.binop.Greater;
import evl.data.expression.binop.Greaterequal;
import evl.data.expression.binop.Is;
import evl.data.expression.binop.Less;
import evl.data.expression.binop.Lessequal;
import evl.data.expression.binop.LogicAnd;
import evl.data.expression.binop.LogicOr;
import evl.data.expression.binop.Minus;
import evl.data.expression.binop.Mod;
import evl.data.expression.binop.Mul;
import evl.data.expression.binop.Notequal;
import evl.data.expression.binop.Or;
import evl.data.expression.binop.Plus;
import evl.data.expression.binop.Shl;
import evl.data.expression.binop.Shr;
import evl.data.expression.reference.RefCall;
import evl.data.expression.reference.RefIndex;
import evl.data.expression.reference.RefName;
import evl.data.expression.reference.Reference;
import evl.data.expression.reference.SimpleRef;
import evl.data.expression.unop.BitNot;
import evl.data.expression.unop.LogicNot;
import evl.data.expression.unop.Not;
import evl.data.expression.unop.Uminus;
import evl.data.function.Function;
import evl.data.function.header.FuncCtrlInDataIn;
import evl.data.function.header.FuncCtrlInDataOut;
import evl.data.function.header.FuncCtrlOutDataIn;
import evl.data.function.header.FuncCtrlOutDataOut;
import evl.data.function.header.FuncGlobal;
import evl.data.function.header.FuncPrivateRet;
import evl.data.function.header.FuncPrivateVoid;
import evl.data.function.header.FuncSubHandlerEvent;
import evl.data.function.header.FuncSubHandlerQuery;
import evl.data.function.ret.FuncReturnNone;
import evl.data.function.ret.FuncReturnTuple;
import evl.data.function.ret.FuncReturnType;
import evl.data.statement.AssignmentMulti;
import evl.data.statement.AssignmentSingle;
import evl.data.statement.Block;
import evl.data.statement.CallStmt;
import evl.data.statement.CaseOpt;
import evl.data.statement.CaseOptRange;
import evl.data.statement.CaseOptValue;
import evl.data.statement.CaseStmt;
import evl.data.statement.ForStmt;
import evl.data.statement.IfOption;
import evl.data.statement.IfStmt;
import evl.data.statement.ReturnExpr;
import evl.data.statement.ReturnVoid;
import evl.data.statement.Statement;
import evl.data.statement.VarDefStmt;
import evl.data.statement.WhileStmt;
import evl.data.statement.intern.MsgPush;
import evl.data.type.base.ArrayType;
import evl.data.type.base.BooleanType;
import evl.data.type.base.EnumElement;
import evl.data.type.base.EnumType;
import evl.data.type.base.FunctionType;
import evl.data.type.base.RangeType;
import evl.data.type.base.StringType;
import evl.data.type.base.TupleType;
import evl.data.type.composed.NamedElement;
import evl.data.type.composed.RecordType;
import evl.data.type.composed.UnionType;
import evl.data.type.composed.UnsafeUnionType;
import evl.data.type.out.AliasType;
import evl.data.type.out.SIntType;
import evl.data.type.out.UIntType;
import evl.data.type.special.AnyType;
import evl.data.type.special.ComponentType;
import evl.data.type.special.IntegerType;
import evl.data.type.special.NaturalType;
import evl.data.type.special.VoidType;
import evl.data.variable.ConstGlobal;
import evl.data.variable.ConstPrivate;
import evl.data.variable.FuncVariable;
import evl.data.variable.StateVariable;

public class DefTraverser<R, P> extends Traverser<R, P> {

  @Override
  protected R visitNamedElementsValue(NamedElementsValue obj, P param) {
    visitList(obj.value, param);
    return null;
  }

  @Override
  protected R visitTupleType(TupleType obj, P param) {
    visitList(obj.types, param);
    return null;
  }

  @Override
  protected R visitFuncReturnTuple(FuncReturnTuple obj, P param) {
    visitList(obj.param, param);
    return null;
  }

  @Override
  protected R visitFuncReturnType(FuncReturnType obj, P param) {
    visit(obj.type, param);
    return null;
  }

  @Override
  protected R visitFuncReturnNone(FuncReturnNone obj, P param) {
    return null;
  }

  @Override
  protected R visitImplElementary(ImplElementary obj, P param) {
    visitList(obj.function, param);
    visitList(obj.iface, param);
    visit(obj.queue, param);
    visitList(obj.type, param);
    visitList(obj.constant, param);
    visitList(obj.variable, param);
    visitList(obj.component, param);
    visitList(obj.subCallback, param);
    visit(obj.entryFunc, param);
    visit(obj.exitFunc, param);
    return null;
  }

  @Override
  protected R visitImplComposition(ImplComposition obj, P param) {
    visitList(obj.function, param);
    visitList(obj.iface, param);
    visit(obj.queue, param);
    visitList(obj.component, param);
    visitList(obj.connection, param);
    return null;
  }

  @Override
  protected R visitImplHfsm(ImplHfsm obj, P param) {
    visitList(obj.function, param);
    visitList(obj.iface, param);
    visit(obj.queue, param);
    visit(obj.topstate, param);
    return null;
  }

  @Override
  protected R visitNumber(Number obj, P param) {
    return null;
  }

  @Override
  protected R visitVarDef(VarDefStmt obj, P param) {
    visit(obj.variable, param);
    return null;
  }

  @Override
  protected R visitCallStmt(CallStmt obj, P param) {
    visit(obj.call, param);
    return null;
  }

  @Override
  protected R visitAssignmentMulti(AssignmentMulti obj, P param) {
    visitList(obj.left, param);
    visit(obj.right, param);
    return null;
  }

  @Override
  protected R visitAssignmentSingle(AssignmentSingle obj, P param) {
    visit(obj.left, param);
    visit(obj.right, param);
    return null;
  }

  @Override
  protected R visitUnionType(UnionType obj, P param) {
    visit(obj.tag, param);
    visitList(obj.element, param);
    return null;
  }

  @Override
  protected R visitUnsafeUnionType(UnsafeUnionType obj, P param) {
    visitList(obj.element, param);
    return null;
  }

  @Override
  protected R visitRecordType(RecordType obj, P param) {
    visitList(obj.element, param);
    return null;
  }

  @Override
  protected R visitComponentType(ComponentType obj, P param) {
    visitList(obj.input, param);
    visitList(obj.output, param);
    return null;
  }

  @Override
  protected R visitRefName(RefName obj, P param) {
    return null;
  }

  @Override
  protected R visitRefIndex(RefIndex obj, P param) {
    visit(obj.index, param);
    return null;
  }

  @Override
  protected R visitRefCall(RefCall obj, P param) {
    visit(obj.actualParameter, param);
    return null;
  }

  @Override
  protected R visitArrayType(ArrayType obj, P param) {
    visit(obj.type, param);
    return null;
  }

  @Override
  protected R visitBooleanType(BooleanType obj, P param) {
    return null;
  }

  @Override
  protected R visitEnumType(EnumType obj, P param) {
    visitList(obj.getElement(), param);
    return null;
  }

  @Override
  protected R visitEnumElement(EnumElement obj, P param) {
    return null;
  }

  @Override
  protected R visitReturnExpr(ReturnExpr obj, P param) {
    visit(obj.expr, param);
    return null;
  }

  @Override
  protected R visitReturnVoid(ReturnVoid obj, P param) {
    return null;
  }

  @Override
  protected R visitVoidType(VoidType obj, P param) {
    return null;
  }

  @Override
  protected R visitFuncVariable(FuncVariable obj, P param) {
    visit(obj.type, param);
    return null;
  }

  @Override
  protected R visitStateVariable(StateVariable obj, P param) {
    visit(obj.type, param);
    visit(obj.def, param);
    return null;
  }

  @Override
  protected R visitConstPrivate(ConstPrivate obj, P param) {
    visit(obj.type, param);
    visit(obj.def, param);
    return null;
  }

  @Override
  protected R visitConstGlobal(ConstGlobal obj, P param) {
    visit(obj.type, param);
    visit(obj.def, param);
    return null;
  }

  @Override
  protected R visitSubCallbacks(SubCallbacks obj, P param) {
    visit(obj.compUse, param);
    visitList(obj.func, param);
    return null;
  }

  @Override
  protected R visitNamespace(Namespace obj, P param) {
    visitList(obj.children, param);
    return null;
  }

  @Override
  protected R visitCaseOptRange(CaseOptRange obj, P param) {
    visit(obj.start, param);
    visit(obj.end, param);
    return null;
  }

  @Override
  protected R visitCaseOptValue(CaseOptValue obj, P param) {
    visit(obj.value, param);
    return null;
  }

  @Override
  protected R visitBoolValue(BoolValue obj, P param) {
    return null;
  }

  @Override
  protected R visitSynchroniusConnection(SynchroniusConnection obj, P param) {
    visit(obj.endpoint.get(Direction.in), param);
    visit(obj.endpoint.get(Direction.out), param);
    return null;
  }

  @Override
  protected R visitAsynchroniusConnection(AsynchroniusConnection obj, P param) {
    visit(obj.endpoint.get(Direction.in), param);
    visit(obj.endpoint.get(Direction.out), param);
    return null;
  }

  @Override
  protected R visitStringValue(StringValue obj, P param) {
    return null;
  }

  @Override
  protected R visitStringType(StringType obj, P param) {
    return null;
  }

  @Override
  protected R visitArrayValue(ArrayValue obj, P param) {
    visitList(obj.value, param);
    return null;
  }

  @Override
  protected R visitTupleValue(TupleValue obj, P param) {
    visitList(obj.value, param);
    return null;
  }

  @Override
  protected R visitStateSimple(StateSimple obj, P param) {
    visit(obj.entryFunc, param);
    visit(obj.exitFunc, param);
    visitList(obj.item, param);
    return null;
  }

  @Override
  protected R visitStateComposite(StateComposite obj, P param) {
    visit(obj.entryFunc, param);
    visit(obj.exitFunc, param);
    visitList(obj.item, param);
    visit(obj.initial, param);
    return null;
  }

  @Override
  protected R visitTransition(Transition obj, P param) {
    visitList(obj.param, param);
    visit(obj.guard, param);
    visit(obj.body, param);
    return null;
  }

  @Override
  protected R visitSimpleRef(SimpleRef obj, P param) {
    return null;
  }

  @Override
  protected R visitReference(Reference obj, P param) {
    visitList(obj.offset, param);
    return null;
  }

  @Override
  protected R visitNaturalType(NaturalType obj, P param) {
    return null;
  }

  @Override
  protected R visitIntegerType(IntegerType obj, P param) {
    return null;
  }

  @Override
  protected R visitFuncPrivateVoid(FuncPrivateVoid obj, P param) {
    return null;
  }

  @Override
  protected R visitFuncPrivateRet(FuncPrivateRet obj, P param) {
    return null;
  }

  @Override
  protected R visitFuncGlobal(FuncGlobal obj, P param) {
    return null;
  }

  @Override
  protected R visitFuncSubHandlerQuery(FuncSubHandlerQuery obj, P param) {
    return null;
  }

  @Override
  protected R visitFuncSubHandlerEvent(FuncSubHandlerEvent obj, P param) {
    return null;
  }

  @Override
  protected R visitFunctionType(FunctionType obj, P param) {
    throw new RuntimeException("not yet implemented");
  }

  @Override
  protected R visitEndpointSelf(EndpointSelf obj, P param) {
    return null;
  }

  @Override
  protected R visitEndpointSub(EndpointSub obj, P param) {
    return null;
  }

  @Override
  protected R visitAnd(And obj, P param) {
    visit(obj.left, param);
    visit(obj.right, param);
    return null;
  }

  @Override
  protected R visitDiv(Div obj, P param) {
    visit(obj.left, param);
    visit(obj.right, param);
    return null;
  }

  @Override
  protected R visitEqual(Equal obj, P param) {
    visit(obj.left, param);
    visit(obj.right, param);
    return null;
  }

  @Override
  protected R visitGreater(Greater obj, P param) {
    visit(obj.left, param);
    visit(obj.right, param);
    return null;
  }

  @Override
  protected R visitGreaterequal(Greaterequal obj, P param) {
    visit(obj.left, param);
    visit(obj.right, param);
    return null;
  }

  @Override
  protected R visitLess(Less obj, P param) {
    visit(obj.left, param);
    visit(obj.right, param);
    return null;
  }

  @Override
  protected R visitIs(Is obj, P param) {
    visit(obj.left, param);
    visit(obj.right, param);
    return null;
  }

  @Override
  protected R visitLessequal(Lessequal obj, P param) {
    visit(obj.left, param);
    visit(obj.right, param);
    return null;
  }

  @Override
  protected R visitMinus(Minus obj, P param) {
    visit(obj.left, param);
    visit(obj.right, param);
    return null;
  }

  @Override
  protected R visitMod(Mod obj, P param) {
    visit(obj.left, param);
    visit(obj.right, param);
    return null;
  }

  @Override
  protected R visitMul(Mul obj, P param) {
    visit(obj.left, param);
    visit(obj.right, param);
    return null;
  }

  @Override
  protected R visitNot(Not obj, P param) {
    visit(obj.expr, param);
    return null;
  }

  @Override
  protected R visitLogicNot(LogicNot obj, P param) {
    visit(obj.expr, param);
    return null;
  }

  @Override
  protected R visitBitNot(BitNot obj, P param) {
    visit(obj.expr, param);
    return null;
  }

  @Override
  protected R visitNotequal(Notequal obj, P param) {
    visit(obj.left, param);
    visit(obj.right, param);
    return null;
  }

  @Override
  protected R visitOr(Or obj, P param) {
    visit(obj.left, param);
    visit(obj.right, param);
    return null;
  }

  @Override
  protected R visitPlus(Plus obj, P param) {
    visit(obj.left, param);
    visit(obj.right, param);
    return null;
  }

  @Override
  protected R visitShl(Shl obj, P param) {
    visit(obj.left, param);
    visit(obj.right, param);
    return null;
  }

  @Override
  protected R visitShr(Shr obj, P param) {
    visit(obj.left, param);
    visit(obj.right, param);
    return null;
  }

  @Override
  protected R visitUminus(Uminus obj, P param) {
    visit(obj.expr, param);
    return null;
  }

  @Override
  protected R visitRangeType(RangeType obj, P param) {
    return null;
  }

  @Override
  protected R visitWhileStmt(WhileStmt obj, P param) {
    visit(obj.condition, param);
    visit(obj.body, param);
    return null;
  }

  @Override
  protected R visitIfStmt(IfStmt obj, P param) {
    visitList(obj.option, param);
    visit(obj.defblock, param);
    return null;
  }

  @Override
  protected R visitIfOption(IfOption obj, P param) {
    visit(obj.condition, param);
    visit(obj.code, param);
    return null;
  }

  @Override
  protected R visitBlock(Block obj, P param) {
    for (Statement stmt : obj.statements) {
      visit(stmt, param);
    }
    return null;
  }

  @Override
  protected R visitCaseStmt(CaseStmt obj, P param) {
    visit(obj.condition, param);
    visitList(obj.option, param);
    visit(obj.otherwise, param);
    return null;
  }

  @Override
  protected R visitCaseOpt(CaseOpt obj, P param) {
    visitList(obj.value, param);
    visit(obj.code, param);
    return null;
  }

  @Override
  protected R visitTypeCast(TypeCast obj, P param) {
    visit(obj.cast, param);
    visit(obj.value, param);
    return null;
  }

  @Override
  protected R visitBitAnd(BitAnd obj, P param) {
    visit(obj.left, param);
    visit(obj.right, param);
    return null;
  }

  @Override
  protected R visitBitOr(BitOr obj, P param) {
    visit(obj.left, param);
    visit(obj.right, param);
    return null;
  }

  @Override
  protected R visitBitXor(BitXor obj, P param) {
    visit(obj.left, param);
    visit(obj.right, param);
    return null;
  }

  @Override
  protected R visitLogicOr(LogicOr obj, P param) {
    visit(obj.left, param);
    visit(obj.right, param);
    return null;
  }

  @Override
  protected R visitLogicAnd(LogicAnd obj, P param) {
    visit(obj.left, param);
    visit(obj.right, param);
    return null;
  }

  @Override
  protected R visitFuncIfaceOutVoid(FuncCtrlOutDataOut obj, P param) {
    return null;
  }

  @Override
  protected R visitFuncIfaceOutRet(FuncCtrlOutDataIn obj, P param) {
    return null;
  }

  @Override
  protected R visitFuncIfaceInVoid(FuncCtrlInDataIn obj, P param) {
    return null;
  }

  @Override
  protected R visitFuncIfaceInRet(FuncCtrlInDataOut obj, P param) {
    return null;
  }

  @Override
  protected R visitAnyType(AnyType obj, P param) {
    return null;
  }

  @Override
  protected R visitAnyValue(AnyValue obj, P param) {
    return null;
  }

  @Override
  protected R visitNamedValue(NamedValue obj, P param) {
    visit(obj.value, param);
    return null;
  }

  @Override
  protected R visitUnionValue(UnionValue obj, P param) {
    visit(obj.tagValue, param);
    visit(obj.contentValue, param);
    visit(obj.type, param);
    return null;
  }

  @Override
  protected R visitUnsafeUnionValue(UnsafeUnionValue obj, P param) {
    visit(obj.contentValue, param);
    visit(obj.type, param);
    return null;
  }

  @Override
  protected R visitRecordValue(RecordValue obj, P param) {
    visitList(obj.value, param);
    visit(obj.type, param);
    return null;
  }

  @Override
  protected R visitMsgPush(MsgPush obj, P param) {
    visit(obj.queue, param);
    visit(obj.func, param);
    visitList(obj.data, param);
    return null;
  }

  @Override
  protected R visitQueue(Queue obj, P param) {
    return null;
  }

  @Override
  protected R visitFunction(Function obj, P param) {
    super.visitFunction(obj, param);
    visitList(obj.param, param);
    visit(obj.ret, param);
    visit(obj.body, param);
    return null;
  }

  @Override
  protected R visitNamedElement(NamedElement obj, P param) {
    visit(obj.ref, param);
    return null;
  }

  @Override
  protected R visitCompUse(CompUse obj, P param) {
    visit(obj.instref, param);
    return null;
  }

  @Override
  protected R visitUIntType(UIntType obj, P param) {
    return null;
  }

  @Override
  protected R visitSIntType(SIntType obj, P param) {
    return null;
  }

  @Override
  protected R visitAliasType(AliasType obj, P param) {
    visit(obj.ref, param);
    return null;
  }

  @Override
  protected R visitForStmt(ForStmt obj, P param) {
    visit(obj.iterator, param);
    visit(obj.block, param);
    return null;
  }

}
