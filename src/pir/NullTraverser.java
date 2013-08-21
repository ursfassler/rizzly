package pir;

import pir.cfg.BasicBlock;
import pir.cfg.BasicBlockList;
import pir.cfg.CaseGoto;
import pir.cfg.Goto;
import pir.cfg.IfGoto;
import pir.cfg.PhiStmt;
import pir.cfg.ReturnExpr;
import pir.cfg.ReturnVoid;
import pir.expression.ArrayValue;
import pir.expression.BoolValue;
import pir.expression.Number;
import pir.expression.StringValue;
import pir.expression.UnaryExpr;
import pir.expression.reference.RefIndex;
import pir.expression.reference.RefName;
import pir.expression.reference.VarRef;
import pir.expression.reference.VarRefSimple;
import pir.function.FuncImpl;
import pir.function.FuncProto;
import pir.other.Constant;
import pir.other.FuncVariable;
import pir.other.Program;
import pir.other.SsaVariable;
import pir.other.StateVariable;
import pir.statement.ArithmeticOp;
import pir.statement.Assignment;
import pir.statement.CallAssignment;
import pir.statement.CallStmt;
import pir.statement.ComplexWriter;
import pir.statement.GetElementPtr;
import pir.statement.LoadStmt;
import pir.statement.Relation;
import pir.statement.StoreStmt;
import pir.statement.VarDefStmt;
import pir.statement.convert.SignExtendValue;
import pir.statement.convert.TruncValue;
import pir.statement.convert.TypeCast;
import pir.statement.convert.ZeroExtendValue;
import pir.type.ArrayType;
import pir.type.BooleanType;
import pir.type.EnumElement;
import pir.type.EnumType;
import pir.type.NamedElement;
import pir.type.RangeType;
import pir.type.SignedType;
import pir.type.StringType;
import pir.type.StructType;
import pir.type.TypeAlias;
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
  protected R visitEnumElement(EnumElement obj, P param) {
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
  protected R visitTypeAlias(TypeAlias obj, P param) {
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
  protected R visitArithmeticOp(ArithmeticOp obj, P param) {
    return doDefault(obj, param);
  }

  @Override
  protected R visitUnaryExpr(UnaryExpr obj, P param) {
    return doDefault(obj, param);
  }

  @Override
  protected R visitVarDefStmt(VarDefStmt obj, P param) {
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
  protected R visitEnumType(EnumType obj, P param) {
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
  protected R visitRelation(Relation obj, P param) {
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
  protected R visitCallExpr(CallAssignment obj, P param) {
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
  protected R visitComplexWriter(ComplexWriter obj, P param) {
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

}
