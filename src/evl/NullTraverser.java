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

package evl;

import evl.composition.Connection;
import evl.composition.EndpointSelf;
import evl.composition.EndpointSub;
import evl.composition.ImplComposition;
import evl.expression.AnyValue;
import evl.expression.ArrayValue;
import evl.expression.BoolValue;
import evl.expression.ExprList;
import evl.expression.NamedElementValue;
import evl.expression.Number;
import evl.expression.RangeValue;
import evl.expression.RecordValue;
import evl.expression.StringValue;
import evl.expression.TypeCast;
import evl.expression.UnionValue;
import evl.expression.UnsafeUnionValue;
import evl.expression.binop.And;
import evl.expression.binop.BitAnd;
import evl.expression.binop.BitOr;
import evl.expression.binop.Div;
import evl.expression.binop.Equal;
import evl.expression.binop.Greater;
import evl.expression.binop.Greaterequal;
import evl.expression.binop.Is;
import evl.expression.binop.Less;
import evl.expression.binop.Lessequal;
import evl.expression.binop.LogicAnd;
import evl.expression.binop.LogicOr;
import evl.expression.binop.Minus;
import evl.expression.binop.Mod;
import evl.expression.binop.Mul;
import evl.expression.binop.Notequal;
import evl.expression.binop.Or;
import evl.expression.binop.Plus;
import evl.expression.binop.Shl;
import evl.expression.binop.Shr;
import evl.expression.reference.RefCall;
import evl.expression.reference.RefIndex;
import evl.expression.reference.RefName;
import evl.expression.reference.Reference;
import evl.expression.reference.SimpleRef;
import evl.expression.unop.BitNot;
import evl.expression.unop.LogicNot;
import evl.expression.unop.Not;
import evl.expression.unop.Uminus;
import evl.function.header.FuncCtrlInDataIn;
import evl.function.header.FuncCtrlInDataOut;
import evl.function.header.FuncCtrlOutDataIn;
import evl.function.header.FuncCtrlOutDataOut;
import evl.function.header.FuncGlobal;
import evl.function.header.FuncPrivateRet;
import evl.function.header.FuncPrivateVoid;
import evl.function.header.FuncSubHandlerEvent;
import evl.function.header.FuncSubHandlerQuery;
import evl.hfsm.ImplHfsm;
import evl.hfsm.StateComposite;
import evl.hfsm.StateSimple;
import evl.hfsm.Transition;
import evl.other.CompUse;
import evl.other.ImplElementary;
import evl.other.Namespace;
import evl.other.Queue;
import evl.other.SubCallbacks;
import evl.statement.Assignment;
import evl.statement.Block;
import evl.statement.CallStmt;
import evl.statement.CaseOpt;
import evl.statement.CaseOptRange;
import evl.statement.CaseOptValue;
import evl.statement.CaseStmt;
import evl.statement.IfOption;
import evl.statement.IfStmt;
import evl.statement.ReturnExpr;
import evl.statement.ReturnVoid;
import evl.statement.VarDefStmt;
import evl.statement.WhileStmt;
import evl.statement.intern.MsgPush;
import evl.type.base.ArrayType;
import evl.type.base.BooleanType;
import evl.type.base.EnumElement;
import evl.type.base.EnumType;
import evl.type.base.FunctionType;
import evl.type.base.RangeType;
import evl.type.base.StringType;
import evl.type.composed.NamedElement;
import evl.type.composed.RecordType;
import evl.type.composed.UnionType;
import evl.type.composed.UnsafeUnionType;
import evl.type.out.SIntType;
import evl.type.out.UIntType;
import evl.type.special.AnyType;
import evl.type.special.ComponentType;
import evl.type.special.IntegerType;
import evl.type.special.NaturalType;
import evl.type.special.VoidType;
import evl.variable.ConstGlobal;
import evl.variable.ConstPrivate;
import evl.variable.FuncVariable;
import evl.variable.StateVariable;

abstract public class NullTraverser<R, P> extends Traverser<R, P> {

  abstract protected R visitDefault(Evl obj, P param);

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
  protected R visitExprList(ExprList obj, P param) {
    return visitDefault(obj, param);
  }

  @Override
  protected R visitUnionValue(UnionValue obj, P param) {
    return visitDefault(obj, param);
  }

  @Override
  protected R visitNamedElementValue(NamedElementValue obj, P param) {
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
  protected R visitTypeRef(SimpleRef obj, P param) {
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
  protected R visitRangeValue(RangeValue obj, P param) {
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
  protected R visitAssignment(Assignment obj, P param) {
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
