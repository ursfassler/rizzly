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

import common.Direction;

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
import evl.function.Function;
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
import evl.other.Component;
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
import evl.statement.Statement;
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

public class DefTraverser<R, P> extends Traverser<R, P> {

  @Override
  protected R visitComponent(Component obj, P param) {
    visitList(obj.getFunction(), param);
    visitList(obj.getIface(), param);
    visit(obj.getQueue(), param);
    return super.visitComponent(obj, param);
  }

  @Override
  protected R visitImplElementary(ImplElementary obj, P param) {
    visitList(obj.getConstant(), param);
    visitList(obj.getVariable(), param);
    visitList(obj.getComponent(), param);
    visitList(obj.getSubCallback(), param);
    visit(obj.getEntryFunc(), param);
    visit(obj.getExitFunc(), param);
    return null;
  }

  @Override
  protected R visitImplComposition(ImplComposition obj, P param) {
    visitList(obj.getComponent(), param);
    visitList(obj.getConnection(), param);
    return null;
  }

  @Override
  protected R visitImplHfsm(ImplHfsm obj, P param) {
    visit(obj.getTopstate(), param);
    return null;
  }

  @Override
  protected R visitNumber(Number obj, P param) {
    return null;
  }

  @Override
  protected R visitVarDef(VarDefStmt obj, P param) {
    visit(obj.getVariable(), param);
    return null;
  }

  @Override
  protected R visitCallStmt(CallStmt obj, P param) {
    visit(obj.getCall(), param);
    return null;
  }

  @Override
  protected R visitAssignment(Assignment obj, P param) {
    visit(obj.getRight(), param);
    visit(obj.getLeft(), param);
    return null;
  }

  @Override
  protected R visitUnionType(UnionType obj, P param) {
    visit(obj.getTag(), param);
    visitList(obj.getElement(), param);
    return null;
  }

  @Override
  protected R visitUnsafeUnionType(UnsafeUnionType obj, P param) {
    visitList(obj.getElement(), param);
    return null;
  }

  @Override
  protected R visitRecordType(RecordType obj, P param) {
    visitList(obj.getElement(), param);
    return null;
  }

  @Override
  protected R visitComponentType(ComponentType obj, P param) {
    visitList(obj.getInput(), param);
    visitList(obj.getOutput(), param);
    return null;
  }

  @Override
  protected R visitRefName(RefName obj, P param) {
    return null;
  }

  @Override
  protected R visitRefIndex(RefIndex obj, P param) {
    visit(obj.getIndex(), param);
    return null;
  }

  @Override
  protected R visitRefCall(RefCall obj, P param) {
    visitList(obj.getActualParameter(), param);
    return null;
  }

  @Override
  protected R visitArrayType(ArrayType obj, P param) {
    visit(obj.getType(), param);
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
    visit(obj.getExpr(), param);
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
    visit(obj.getType(), param);
    return null;
  }

  @Override
  protected R visitStateVariable(StateVariable obj, P param) {
    visit(obj.getType(), param);
    visit(obj.getDef(), param);
    return null;
  }

  @Override
  protected R visitConstPrivate(ConstPrivate obj, P param) {
    visit(obj.getType(), param);
    visit(obj.getDef(), param);
    return null;
  }

  @Override
  protected R visitConstGlobal(ConstGlobal obj, P param) {
    visit(obj.getType(), param);
    visit(obj.getDef(), param);
    return null;
  }

  @Override
  protected R visitSubCallbacks(SubCallbacks obj, P param) {
    visit(obj.getCompUse(), param);
    visitList(obj.getFunc(), param);
    return null;
  }

  @Override
  protected R visitNamespace(Namespace obj, P param) {
    visitList(obj.getChildren(), param);
    return null;
  }

  @Override
  protected R visitCaseOptRange(CaseOptRange obj, P param) {
    visit(obj.getStart(), param);
    visit(obj.getEnd(), param);
    return null;
  }

  @Override
  protected R visitCaseOptValue(CaseOptValue obj, P param) {
    visit(obj.getValue(), param);
    return null;
  }

  @Override
  protected R visitBoolValue(BoolValue obj, P param) {
    return null;
  }

  @Override
  protected R visitConnection(Connection obj, P param) {
    visit(obj.getEndpoint(Direction.in), param);
    visit(obj.getEndpoint(Direction.out), param);
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
    visitList(obj.getValue(), param);
    return null;
  }

  @Override
  protected R visitExprList(ExprList obj, P param) {
    visitList(obj.getValue(), param);
    return null;
  }

  @Override
  protected R visitStateSimple(StateSimple obj, P param) {
    visit(obj.getEntryFunc(), param);
    visit(obj.getExitFunc(), param);
    visitList(obj.getItem(), param);
    return null;
  }

  @Override
  protected R visitStateComposite(StateComposite obj, P param) {
    visit(obj.getEntryFunc(), param);
    visit(obj.getExitFunc(), param);
    visitList(obj.getItem(), param);
    visit(obj.getInitial(), param);
    return null;
  }

  @Override
  protected R visitTransition(Transition obj, P param) {
    visitList(obj.getParam(), param);
    visit(obj.getGuard(), param);
    visit(obj.getBody(), param);
    return null;
  }

  @Override
  protected R visitTypeRef(SimpleRef obj, P param) {
    return null;
  }

  @Override
  protected R visitReference(Reference obj, P param) {
    visitList(obj.getOffset(), param);
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
    visit(obj.getLeft(), param);
    visit(obj.getRight(), param);
    return null;
  }

  @Override
  protected R visitDiv(Div obj, P param) {
    visit(obj.getLeft(), param);
    visit(obj.getRight(), param);
    return null;
  }

  @Override
  protected R visitEqual(Equal obj, P param) {
    visit(obj.getLeft(), param);
    visit(obj.getRight(), param);
    return null;
  }

  @Override
  protected R visitGreater(Greater obj, P param) {
    visit(obj.getLeft(), param);
    visit(obj.getRight(), param);
    return null;
  }

  @Override
  protected R visitGreaterequal(Greaterequal obj, P param) {
    visit(obj.getLeft(), param);
    visit(obj.getRight(), param);
    return null;
  }

  @Override
  protected R visitLess(Less obj, P param) {
    visit(obj.getLeft(), param);
    visit(obj.getRight(), param);
    return null;
  }

  @Override
  protected R visitIs(Is obj, P param) {
    visit(obj.getLeft(), param);
    visit(obj.getRight(), param);
    return null;
  }

  @Override
  protected R visitLessequal(Lessequal obj, P param) {
    visit(obj.getLeft(), param);
    visit(obj.getRight(), param);
    return null;
  }

  @Override
  protected R visitMinus(Minus obj, P param) {
    visit(obj.getLeft(), param);
    visit(obj.getRight(), param);
    return null;
  }

  @Override
  protected R visitMod(Mod obj, P param) {
    visit(obj.getLeft(), param);
    visit(obj.getRight(), param);
    return null;
  }

  @Override
  protected R visitMul(Mul obj, P param) {
    visit(obj.getLeft(), param);
    visit(obj.getRight(), param);
    return null;
  }

  @Override
  protected R visitNot(Not obj, P param) {
    visit(obj.getExpr(), param);
    return null;
  }

  @Override
  protected R visitLogicNot(LogicNot obj, P param) {
    visit(obj.getExpr(), param);
    return null;
  }

  @Override
  protected R visitBitNot(BitNot obj, P param) {
    visit(obj.getExpr(), param);
    return null;
  }

  @Override
  protected R visitNotequal(Notequal obj, P param) {
    visit(obj.getLeft(), param);
    visit(obj.getRight(), param);
    return null;
  }

  @Override
  protected R visitOr(Or obj, P param) {
    visit(obj.getLeft(), param);
    visit(obj.getRight(), param);
    return null;
  }

  @Override
  protected R visitPlus(Plus obj, P param) {
    visit(obj.getLeft(), param);
    visit(obj.getRight(), param);
    return null;
  }

  @Override
  protected R visitShl(Shl obj, P param) {
    visit(obj.getLeft(), param);
    visit(obj.getRight(), param);
    return null;
  }

  @Override
  protected R visitShr(Shr obj, P param) {
    visit(obj.getLeft(), param);
    visit(obj.getRight(), param);
    return null;
  }

  @Override
  protected R visitUminus(Uminus obj, P param) {
    visit(obj.getExpr(), param);
    return null;
  }

  @Override
  protected R visitRangeType(RangeType obj, P param) {
    return null;
  }

  @Override
  protected R visitRangeValue(RangeValue obj, P param) {
    return null;
  }

  @Override
  protected R visitWhileStmt(WhileStmt obj, P param) {
    visit(obj.getCondition(), param);
    visit(obj.getBody(), param);
    return null;
  }

  @Override
  protected R visitIfStmt(IfStmt obj, P param) {
    visitList(obj.getOption(), param);
    visit(obj.getDefblock(), param);
    return null;
  }

  @Override
  protected R visitIfOption(IfOption obj, P param) {
    visit(obj.getCondition(), param);
    visit(obj.getCode(), param);
    return null;
  }

  @Override
  protected R visitBlock(Block obj, P param) {
    for (Statement stmt : obj.getStatements()) {
      visit(stmt, param);
    }
    return null;
  }

  @Override
  protected R visitCaseStmt(CaseStmt obj, P param) {
    visit(obj.getCondition(), param);
    visitList(obj.getOption(), param);
    visit(obj.getOtherwise(), param);
    return null;
  }

  @Override
  protected R visitCaseOpt(CaseOpt obj, P param) {
    visitList(obj.getValue(), param);
    visit(obj.getCode(), param);
    return null;
  }

  @Override
  protected R visitTypeCast(TypeCast obj, P param) {
    visit(obj.getCast(), param);
    visit(obj.getValue(), param);
    return null;
  }

  @Override
  protected R visitBitAnd(BitAnd obj, P param) {
    visit(obj.getLeft(), param);
    visit(obj.getRight(), param);
    return null;
  }

  @Override
  protected R visitBitOr(BitOr obj, P param) {
    visit(obj.getLeft(), param);
    visit(obj.getRight(), param);
    return null;
  }

  @Override
  protected R visitLogicOr(LogicOr obj, P param) {
    visit(obj.getLeft(), param);
    visit(obj.getRight(), param);
    return null;
  }

  @Override
  protected R visitLogicAnd(LogicAnd obj, P param) {
    visit(obj.getLeft(), param);
    visit(obj.getRight(), param);
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
  protected R visitNamedElementValue(NamedElementValue obj, P param) {
    visit(obj.getValue(), param);
    return null;
  }

  @Override
  protected R visitUnionValue(UnionValue obj, P param) {
    visit(obj.getTagValue(), param);
    visit(obj.getContentValue(), param);
    visit(obj.getType(), param);
    return null;
  }

  @Override
  protected R visitUnsafeUnionValue(UnsafeUnionValue obj, P param) {
    visit(obj.getContentValue(), param);
    visit(obj.getType(), param);
    return null;
  }

  @Override
  protected R visitRecordValue(RecordValue obj, P param) {
    visitList(obj.getValue(), param);
    visit(obj.getType(), param);
    return null;
  }

  @Override
  protected R visitMsgPush(MsgPush obj, P param) {
    visit(obj.getQueue(), param);
    visit(obj.getFunc(), param);
    visitList(obj.getData(), param);
    return null;
  }

  @Override
  protected R visitQueue(Queue obj, P param) {
    return null;
  }

  @Override
  protected R visitFunctionImpl(Function obj, P param) {
    super.visitFunctionImpl(obj, param);
    visitList(obj.getParam(), param);
    visit(obj.getRet(), param);
    visit(obj.getBody(), param);
    return null;
  }

  @Override
  protected R visitNamedElement(NamedElement obj, P param) {
    visit(obj.getRef(), param);
    return null;
  }

  @Override
  protected R visitCompUse(CompUse obj, P param) {
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

}
