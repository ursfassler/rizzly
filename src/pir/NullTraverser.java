package pir;

import pir.cfg.BasicBlock;
import pir.cfg.BasicBlockList;
import pir.expression.ArrayValue;
import pir.expression.BoolValue;
import pir.expression.Number;
import pir.expression.StringValue;
import pir.expression.reference.RefIndex;
import pir.expression.reference.RefName;
import pir.expression.reference.VarRef;
import pir.expression.reference.VarRefConst;
import pir.expression.reference.VarRefSimple;
import pir.expression.reference.VarRefStatevar;
import pir.function.FuncImpl;
import pir.function.FuncProto;
import pir.other.Constant;
import pir.other.FuncVariable;
import pir.other.Program;
import pir.other.SsaVariable;
import pir.other.StateVariable;
import pir.statement.bbend.CaseGoto;
import pir.statement.bbend.CaseGotoOpt;
import pir.statement.bbend.CaseOptRange;
import pir.statement.bbend.CaseOptValue;
import pir.statement.bbend.Goto;
import pir.statement.bbend.IfGoto;
import pir.statement.bbend.ReturnExpr;
import pir.statement.bbend.ReturnVoid;
import pir.statement.bbend.Unreachable;
import pir.statement.normal.Assignment;
import pir.statement.normal.CallAssignment;
import pir.statement.normal.CallStmt;
import pir.statement.normal.GetElementPtr;
import pir.statement.normal.LoadStmt;
import pir.statement.normal.StackMemoryAlloc;
import pir.statement.normal.StoreStmt;
import pir.statement.normal.binop.And;
import pir.statement.normal.binop.Div;
import pir.statement.normal.binop.Equal;
import pir.statement.normal.binop.Greater;
import pir.statement.normal.binop.Greaterequal;
import pir.statement.normal.binop.Less;
import pir.statement.normal.binop.Lessequal;
import pir.statement.normal.binop.Minus;
import pir.statement.normal.binop.Mod;
import pir.statement.normal.binop.Mul;
import pir.statement.normal.binop.Notequal;
import pir.statement.normal.binop.Or;
import pir.statement.normal.binop.Plus;
import pir.statement.normal.binop.Shl;
import pir.statement.normal.binop.Shr;
import pir.statement.normal.convert.SignExtendValue;
import pir.statement.normal.convert.TruncValue;
import pir.statement.normal.convert.TypeCast;
import pir.statement.normal.convert.ZeroExtendValue;
import pir.statement.normal.unop.Not;
import pir.statement.normal.unop.Uminus;
import pir.statement.phi.PhiStmt;
import pir.type.ArrayType;
import pir.type.BooleanType;
import pir.type.NamedElement;
import pir.type.NoSignType;
import pir.type.PointerType;
import pir.type.RangeType;
import pir.type.SignedType;
import pir.type.StringType;
import pir.type.StructType;
import pir.type.TypeRef;
import pir.type.UnionType;
import pir.type.UnsignedType;
import pir.type.VoidType;

abstract public class NullTraverser<R, P> extends Traverser<R, P> {

  abstract protected R doDefault(PirObject obj, P param);

  @Override
  protected R visitArrayValue(ArrayValue obj, P param) {
    return doDefault(obj, param);
  }

  @Override
  protected R visitRefIndex(RefIndex obj, P param) {
    return doDefault(obj, param);
  }

  @Override
  protected R visitRefName(RefName obj, P param) {
    return doDefault(obj, param);
  }

  @Override
  protected R visitReturnVoid(ReturnVoid obj, P param) {
    return doDefault(obj, param);
  }

  @Override
  protected R visitReturnExpr(ReturnExpr obj, P param) {
    return doDefault(obj, param);
  }

  @Override
  protected R visitConstant(Constant obj, P param) {
    return doDefault(obj, param);
  }

  @Override
  protected R visitStateVariable(StateVariable obj, P param) {
    return doDefault(obj, param);
  }

  @Override
  protected R visitFuncVariable(FuncVariable obj, P param) {
    return doDefault(obj, param);
  }

  @Override
  protected R visitNamedElement(NamedElement obj, P param) {
    return doDefault(obj, param);
  }

  @Override
  protected R visitVoidType(VoidType obj, P param) {
    return doDefault(obj, param);
  }

  @Override
  protected R visitBoolValue(BoolValue obj, P param) {
    return doDefault(obj, param);
  }

  @Override
  protected R visitNumber(Number obj, P param) {
    return doDefault(obj, param);
  }

  @Override
  protected R visitCallStmt(CallStmt obj, P param) {
    return doDefault(obj, param);
  }

  @Override
  protected R visitAssignment(Assignment obj, P param) {
    return doDefault(obj, param);
  }

  @Override
  protected R visitStructType(StructType obj, P param) {
    return doDefault(obj, param);
  }

  @Override
  protected R visitUnionType(UnionType obj, P param) {
    return doDefault(obj, param);
  }

  @Override
  protected R visitBooleanType(BooleanType obj, P param) {
    return doDefault(obj, param);
  }

  @Override
  protected R visitArray(ArrayType obj, P param) {
    return doDefault(obj, param);
  }

  @Override
  protected R visitUnsignedType(UnsignedType obj, P param) {
    return doDefault(obj, param);
  }

  @Override
  protected R visitProgram(Program obj, P param) {
    return doDefault(obj, param);
  }

  @Override
  protected R visitStringValue(StringValue obj, P param) {
    return doDefault(obj, param);
  }

  @Override
  protected R visitStringType(StringType obj, P param) {
    return doDefault(obj, param);
  }

  @Override
  protected R visitFuncImpl(FuncImpl obj, P param) {
    return doDefault(obj, param);
  }

  @Override
  protected R visitFuncProto(FuncProto obj, P param) {
    return doDefault(obj, param);
  }

  @Override
  protected R visitRangeType(RangeType obj, P param) {
    return doDefault(obj, param);
  }

  @Override
  protected R visitCaseGoto(CaseGoto obj, P param) {
    return doDefault(obj, param);
  }

  @Override
  protected R visitIfGoto(IfGoto obj, P param) {
    return doDefault(obj, param);
  }

  @Override
  protected R visitSignedType(SignedType obj, P param) {
    return doDefault(obj, param);
  }

  @Override
  protected R visitPhiStmt(PhiStmt obj, P param) {
    return doDefault(obj, param);
  }

  @Override
  protected R visitGoto(Goto obj, P param) {
    return doDefault(obj, param);
  }

  @Override
  protected R visitSsaVariable(SsaVariable obj, P param) {
    return doDefault(obj, param);
  }

  @Override
  protected R visitBasicBlock(BasicBlock obj, P param) {
    return doDefault(obj, param);
  }

  @Override
  protected R visitBasicBlockList(BasicBlockList obj, P param) {
    return doDefault(obj, param);
  }

  @Override
  protected R visitStoreStmt(StoreStmt obj, P param) {
    return doDefault(obj, param);
  }

  @Override
  protected R visitVarRef(VarRef obj, P param) {
    return doDefault(obj, param);
  }

  @Override
  protected R visitLoadStmt(LoadStmt obj, P param) {
    return doDefault(obj, param);
  }

  @Override
  protected R visitCallAssignment(CallAssignment obj, P param) {
    return doDefault(obj, param);
  }

  @Override
  protected R visitGetElementPtr(GetElementPtr obj, P param) {
    return doDefault(obj, param);
  }

  @Override
  protected R visitTruncValue(TruncValue obj, P param) {
    return doDefault(obj, param);
  }

  @Override
  protected R visitSignExtendValue(SignExtendValue obj, P param) {
    return doDefault(obj, param);
  }

  @Override
  protected R visitZeroExtendValue(ZeroExtendValue obj, P param) {
    return doDefault(obj, param);
  }

  @Override
  protected R visitVarRefSimple(VarRefSimple obj, P param) {
    return doDefault(obj, param);
  }

  @Override
  protected R visitTypeCast(TypeCast obj, P param) {
    return doDefault(obj, param);
  }

  @Override
  protected R visitTypeRef(TypeRef obj, P param) {
    return doDefault(obj, param);
  }

  @Override
  protected R visitNoSignType(NoSignType obj, P param) {
    return doDefault(obj, param);
  }

  @Override
  protected R visitCaseOptValue(CaseOptValue obj, P param) {
    return doDefault(obj, param);
  }

  @Override
  protected R visitCaseOptRange(CaseOptRange obj, P param) {
    return doDefault(obj, param);
  }

  @Override
  protected R visitCaseGotoOpt(CaseGotoOpt obj, P param) {
    return doDefault(obj, param);
  }

  @Override
  protected R visitVarRefStatevar(VarRefStatevar obj, P param) {
    return doDefault(obj, param);
  }

  @Override
  protected R visitPointerType(PointerType obj, P param) {
    return doDefault(obj, param);
  }

  @Override
  protected R visitStackMemoryAlloc(StackMemoryAlloc obj, P param) {
    return doDefault(obj, param);
  }

  @Override
  protected R visitVarRefConst(VarRefConst obj, P param) {
    return doDefault(obj, param);
  }

  @Override
  protected R visitUnreachable(Unreachable obj, P param) {
    return doDefault(obj, param);
  }

  @Override
  protected R visitAnd(And obj, P param) {
    return doDefault(obj, param);
  }

  @Override
  protected R visitDiv(Div obj, P param) {
    return doDefault(obj, param);
  }

  @Override
  protected R visitEqual(Equal obj, P param) {
    return doDefault(obj, param);
  }

  @Override
  protected R visitGreater(Greater obj, P param) {
    return doDefault(obj, param);
  }

  @Override
  protected R visitGreaterequal(Greaterequal obj, P param) {
    return doDefault(obj, param);
  }

  @Override
  protected R visitLess(Less obj, P param) {
    return doDefault(obj, param);
  }

  @Override
  protected R visitLessequal(Lessequal obj, P param) {
    return doDefault(obj, param);
  }

  @Override
  protected R visitMinus(Minus obj, P param) {
    return doDefault(obj, param);
  }

  @Override
  protected R visitMod(Mod obj, P param) {
    return doDefault(obj, param);
  }

  @Override
  protected R visitMul(Mul obj, P param) {
    return doDefault(obj, param);
  }

  @Override
  protected R visitNotequal(Notequal obj, P param) {
    return doDefault(obj, param);
  }

  @Override
  protected R visitOr(Or obj, P param) {
    return doDefault(obj, param);
  }

  @Override
  protected R visitPlus(Plus obj, P param) {
    return doDefault(obj, param);
  }

  @Override
  protected R visitShl(Shl obj, P param) {
    return doDefault(obj, param);
  }

  @Override
  protected R visitShr(Shr obj, P param) {
    return doDefault(obj, param);
  }

  @Override
  protected R visitNot(Not obj, P param) {
    return doDefault(obj, param);
  }

  @Override
  protected R visitUminus(Uminus obj, P param) {
    return doDefault(obj, param);
  }
}
