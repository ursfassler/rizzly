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

import evl.data.Evl;
import evl.data.Namespace;
import evl.data.component.composition.CompUse;
import evl.data.component.composition.Connection;
import evl.data.component.composition.EndpointSelf;
import evl.data.component.composition.EndpointSub;
import evl.data.component.composition.ImplComposition;
import evl.data.component.composition.Queue;
import evl.data.component.composition.SubCallbacks;
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

abstract public class NullTraverser<R, P> extends Traverser<R, P> {

  abstract protected R visitDefault(Evl obj, P param);

  @Override
  protected R visitForStmt(ForStmt obj, P param) {
    return visitDefault(obj, param);
  }

  @Override
  protected R visitBitXor(BitXor obj, P param) {
    return visitDefault(obj, param);
  }

  @Override
  protected R visitAssignmentSingle(AssignmentSingle obj, P param) {
    return visitDefault(obj, param);
  }

  @Override
  protected R visitNamedElementsValue(NamedElementsValue obj, P param) {
    return visitDefault(obj, param);
  }

  @Override
  protected R visitTupleType(TupleType obj, P param) {
    return visitDefault(obj, param);
  }

  @Override
  protected R visitFuncReturnTuple(FuncReturnTuple obj, P param) {
    return visitDefault(obj, param);
  }

  @Override
  protected R visitFuncReturnType(FuncReturnType obj, P param) {
    return visitDefault(obj, param);
  }

  @Override
  protected R visitFuncReturnNone(FuncReturnNone obj, P param) {
    return visitDefault(obj, param);
  }

  @Override
  protected R visitAliasType(AliasType obj, P param) {
    return visitDefault(obj, param);
  }

  @Override
  protected R visitUIntType(UIntType obj, P param) {
    return visitDefault(obj, param);
  }

  @Override
  protected R visitSIntType(SIntType obj, P param) {
    return visitDefault(obj, param);
  }

  @Override
  protected R visitCompUse(CompUse obj, P param) {
    return visitDefault(obj, param);
  }

  @Override
  protected R visitNamedElement(NamedElement obj, P param) {
    return visitDefault(obj, param);
  }

  @Override
  protected R visitQueue(Queue obj, P param) {
    return visitDefault(obj, param);
  }

  @Override
  protected R visitMsgPush(MsgPush obj, P param) {
    return visitDefault(obj, param);
  }

  @Override
  protected R visitUnsafeUnionValue(UnsafeUnionValue obj, P param) {
    return visitDefault(obj, param);
  }

  @Override
  protected R visitRecordValue(RecordValue obj, P param) {
    return visitDefault(obj, param);
  }

  @Override
  protected R visitTupleValue(TupleValue obj, P param) {
    return visitDefault(obj, param);
  }

  @Override
  protected R visitUnionValue(UnionValue obj, P param) {
    return visitDefault(obj, param);
  }

  @Override
  protected R visitNamedValue(NamedValue obj, P param) {
    return visitDefault(obj, param);
  }

  @Override
  protected R visitAnyType(AnyType obj, P param) {
    return visitDefault(obj, param);
  }

  @Override
  protected R visitAnyValue(AnyValue obj, P param) {
    return visitDefault(obj, param);
  }

  @Override
  protected R visitFuncIfaceOutVoid(FuncCtrlOutDataOut obj, P param) {
    return visitDefault(obj, param);
  }

  @Override
  protected R visitFuncIfaceOutRet(FuncCtrlOutDataIn obj, P param) {
    return visitDefault(obj, param);
  }

  @Override
  protected R visitFuncIfaceInVoid(FuncCtrlInDataIn obj, P param) {
    return visitDefault(obj, param);
  }

  @Override
  protected R visitFuncIfaceInRet(FuncCtrlInDataOut obj, P param) {
    return visitDefault(obj, param);
  }

  @Override
  protected R visitBitAnd(BitAnd obj, P param) {
    return visitDefault(obj, param);
  }

  @Override
  protected R visitBitOr(BitOr obj, P param) {
    return visitDefault(obj, param);
  }

  @Override
  protected R visitLogicOr(LogicOr obj, P param) {
    return visitDefault(obj, param);
  }

  @Override
  protected R visitLogicAnd(LogicAnd obj, P param) {
    return visitDefault(obj, param);
  }

  @Override
  protected R visitArrayValue(ArrayValue obj, P param) {
    return visitDefault(obj, param);
  }

  @Override
  protected R visitVoidType(VoidType obj, P param) {
    return visitDefault(obj, param);
  }

  @Override
  protected R visitBooleanType(BooleanType obj, P param) {
    return visitDefault(obj, param);
  }

  @Override
  protected R visitStringType(StringType obj, P param) {
    return visitDefault(obj, param);
  }

  @Override
  protected R visitRefCall(RefCall obj, P param) {
    return visitDefault(obj, param);
  }

  @Override
  protected R visitRefName(RefName obj, P param) {
    return visitDefault(obj, param);
  }

  @Override
  protected R visitRefIndex(RefIndex obj, P param) {
    return visitDefault(obj, param);
  }

  @Override
  protected R visitImplElementary(ImplElementary obj, P param) {
    return visitDefault(obj, param);
  }

  @Override
  protected R visitFuncGlobal(FuncGlobal obj, P param) {
    return visitDefault(obj, param);
  }

  @Override
  protected R visitRecordType(RecordType obj, P param) {
    return visitDefault(obj, param);
  }

  @Override
  protected R visitUnionType(UnionType obj, P param) {
    return visitDefault(obj, param);
  }

  @Override
  protected R visitUnsafeUnionType(UnsafeUnionType obj, P param) {
    return visitDefault(obj, param);
  }

  @Override
  protected R visitEnumType(EnumType obj, P param) {
    return visitDefault(obj, param);
  }

  @Override
  protected R visitEnumElement(EnumElement obj, P param) {
    return visitDefault(obj, param);
  }

  @Override
  protected R visitNumber(Number obj, P param) {
    return visitDefault(obj, param);
  }

  @Override
  protected R visitStringValue(StringValue obj, P param) {
    return visitDefault(obj, param);
  }

  @Override
  protected R visitStateVariable(StateVariable obj, P param) {
    return visitDefault(obj, param);
  }

  @Override
  protected R visitSubCallbacks(SubCallbacks obj, P param) {
    return visitDefault(obj, param);
  }

  @Override
  protected R visitNamespace(Namespace obj, P param) {
    return visitDefault(obj, param);
  }

  @Override
  protected R visitConstPrivate(ConstPrivate obj, P param) {
    return visitDefault(obj, param);
  }

  @Override
  protected R visitConstGlobal(ConstGlobal obj, P param) {
    return visitDefault(obj, param);
  }

  @Override
  protected R visitArrayType(ArrayType obj, P param) {
    return visitDefault(obj, param);
  }

  @Override
  protected R visitFuncVariable(FuncVariable obj, P param) {
    return visitDefault(obj, param);
  }

  @Override
  protected R visitBoolValue(BoolValue obj, P param) {
    return visitDefault(obj, param);
  }

  @Override
  protected R visitImplComposition(ImplComposition obj, P param) {
    return visitDefault(obj, param);
  }

  @Override
  protected R visitConnection(Connection obj, P param) {
    return visitDefault(obj, param);
  }

  @Override
  protected R visitImplHfsm(ImplHfsm obj, P param) {
    return visitDefault(obj, param);
  }

  @Override
  protected R visitStateSimple(StateSimple obj, P param) {
    return visitDefault(obj, param);
  }

  @Override
  protected R visitStateComposite(StateComposite obj, P param) {
    return visitDefault(obj, param);
  }

  @Override
  protected R visitTransition(Transition obj, P param) {
    return visitDefault(obj, param);
  }

  @Override
  protected R visitReference(Reference obj, P param) {
    return visitDefault(obj, param);
  }

  @Override
  protected R visitNaturalType(NaturalType obj, P param) {
    return visitDefault(obj, param);
  }

  @Override
  protected R visitIntegerType(IntegerType obj, P param) {
    return visitDefault(obj, param);
  }

  @Override
  protected R visitFuncPrivateVoid(FuncPrivateVoid obj, P param) {
    return visitDefault(obj, param);
  }

  @Override
  protected R visitFuncPrivateRet(FuncPrivateRet obj, P param) {
    return visitDefault(obj, param);
  }

  @Override
  protected R visitFuncSubHandlerQuery(FuncSubHandlerQuery obj, P param) {
    return visitDefault(obj, param);
  }

  @Override
  protected R visitFuncSubHandlerEvent(FuncSubHandlerEvent obj, P param) {
    return visitDefault(obj, param);
  }

  @Override
  protected R visitFunctionType(FunctionType obj, P param) {
    return visitDefault(obj, param);
  }

  @Override
  protected R visitEndpointSelf(EndpointSelf obj, P param) {
    return visitDefault(obj, param);
  }

  @Override
  protected R visitEndpointSub(EndpointSub obj, P param) {
    return visitDefault(obj, param);
  }

  @Override
  protected R visitComponentType(ComponentType obj, P param) {
    return visitDefault(obj, param);
  }

  @Override
  protected R visitSimpleRef(SimpleRef obj, P param) {
    return visitDefault(obj, param);
  }

  @Override
  protected R visitAnd(And obj, P param) {
    return visitDefault(obj, param);
  }

  @Override
  protected R visitDiv(Div obj, P param) {
    return visitDefault(obj, param);
  }

  @Override
  protected R visitEqual(Equal obj, P param) {
    return visitDefault(obj, param);
  }

  @Override
  protected R visitGreater(Greater obj, P param) {
    return visitDefault(obj, param);
  }

  @Override
  protected R visitGreaterequal(Greaterequal obj, P param) {
    return visitDefault(obj, param);
  }

  @Override
  protected R visitLess(Less obj, P param) {
    return visitDefault(obj, param);
  }

  @Override
  protected R visitLessequal(Lessequal obj, P param) {
    return visitDefault(obj, param);
  }

  @Override
  protected R visitMinus(Minus obj, P param) {
    return visitDefault(obj, param);
  }

  @Override
  protected R visitMod(Mod obj, P param) {
    return visitDefault(obj, param);
  }

  @Override
  protected R visitMul(Mul obj, P param) {
    return visitDefault(obj, param);
  }

  @Override
  protected R visitNot(Not obj, P param) {
    return visitDefault(obj, param);
  }

  @Override
  protected R visitNotequal(Notequal obj, P param) {
    return visitDefault(obj, param);
  }

  @Override
  protected R visitOr(Or obj, P param) {
    return visitDefault(obj, param);
  }

  @Override
  protected R visitPlus(Plus obj, P param) {
    return visitDefault(obj, param);
  }

  @Override
  protected R visitShl(Shl obj, P param) {
    return visitDefault(obj, param);
  }

  @Override
  protected R visitShr(Shr obj, P param) {
    return visitDefault(obj, param);
  }

  @Override
  protected R visitUminus(Uminus obj, P param) {
    return visitDefault(obj, param);
  }

  @Override
  protected R visitRangeType(RangeType obj, P param) {
    return visitDefault(obj, param);
  }

  @Override
  protected R visitWhileStmt(WhileStmt obj, P param) {
    return visitDefault(obj, param);
  }

  @Override
  protected R visitCaseStmt(CaseStmt obj, P param) {
    return visitDefault(obj, param);
  }

  @Override
  protected R visitCaseOptRange(CaseOptRange obj, P param) {
    return visitDefault(obj, param);
  }

  @Override
  protected R visitCaseOptValue(CaseOptValue obj, P param) {
    return visitDefault(obj, param);
  }

  @Override
  protected R visitCaseOpt(CaseOpt obj, P param) {
    return visitDefault(obj, param);
  }

  @Override
  protected R visitIfOption(IfOption obj, P param) {
    return visitDefault(obj, param);
  }

  @Override
  protected R visitVarDef(VarDefStmt obj, P param) {
    return visitDefault(obj, param);
  }

  @Override
  protected R visitIfStmt(IfStmt obj, P param) {
    return visitDefault(obj, param);
  }

  @Override
  protected R visitCallStmt(CallStmt obj, P param) {
    return visitDefault(obj, param);
  }

  @Override
  protected R visitAssignmentMulti(AssignmentMulti obj, P param) {
    return visitDefault(obj, param);
  }

  @Override
  protected R visitReturnExpr(ReturnExpr obj, P param) {
    return visitDefault(obj, param);
  }

  @Override
  protected R visitReturnVoid(ReturnVoid obj, P param) {
    return visitDefault(obj, param);
  }

  @Override
  protected R visitBlock(Block obj, P param) {
    return visitDefault(obj, param);
  }

  @Override
  protected R visitTypeCast(TypeCast obj, P param) {
    return visitDefault(obj, param);
  }

  @Override
  protected R visitLogicNot(LogicNot obj, P param) {
    return visitDefault(obj, param);
  }

  @Override
  protected R visitBitNot(BitNot obj, P param) {
    return visitDefault(obj, param);
  }

  @Override
  protected R visitIs(Is obj, P param) {
    return visitDefault(obj, param);
  }
}
