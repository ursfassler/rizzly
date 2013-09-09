package pir;

import pir.cfg.BasicBlock;
import pir.cfg.BasicBlockList;
import pir.cfg.CaseGoto;
import pir.cfg.CaseGotoOpt;
import pir.cfg.CaseOptRange;
import pir.cfg.CaseOptValue;
import pir.cfg.Goto;
import pir.cfg.IfGoto;
import pir.cfg.PhiStmt;
import pir.cfg.ReturnExpr;
import pir.cfg.ReturnVoid;
import pir.expression.ArrayValue;
import pir.expression.BoolValue;
import pir.expression.Number;
import pir.expression.StringValue;
import pir.expression.reference.RefIndex;
import pir.expression.reference.RefName;
import pir.expression.reference.VarRef;
import pir.expression.reference.VarRefSimple;
import pir.expression.reference.VarRefStatevar;
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
import pir.statement.GetElementPtr;
import pir.statement.LoadStmt;
import pir.statement.Relation;
import pir.statement.StackMemoryAlloc;
import pir.statement.StoreStmt;
import pir.statement.UnaryOp;
import pir.statement.convert.SignExtendValue;
import pir.statement.convert.TruncValue;
import pir.statement.convert.TypeCast;
import pir.statement.convert.ZeroExtendValue;
import pir.type.ArrayType;
import pir.type.BooleanType;
import pir.type.EnumElement;
import pir.type.EnumType;
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

public class DefTraverser<R, P> extends Traverser<R, P> {

  @Override
  protected R visitProgram(Program obj, P param) {
    visitList(obj.getType(), param);
    visitList(obj.getVariable(), param);
    visitList(obj.getFunction(), param);
    return null;
  }

  @Override
  protected R visitUnsignedType(UnsignedType obj, P param) {
    return null;
  }

  @Override
  protected R visitCallStmt(CallStmt obj, P param) {
    visitList(obj.getParameter(), param);
    return null;
  }

  @Override
  protected R visitStoreStmt(StoreStmt obj, P param) {
    visit(obj.getSrc(), param);
    visit(obj.getDst(), param);
    return null;
  }

  @Override
  protected R visitAssignment(Assignment obj, P param) {
    visit(obj.getSrc(), param);
    visit(obj.getVariable(), param);
    return null;
  }

  @Override
  protected R visitNumber(Number obj, P param) {
    visit(obj.getType(), param);
    return null;
  }

  @Override
  protected R visitArithmeticOp(ArithmeticOp obj, P param) {
    visit(obj.getVariable(), param);
    visit(obj.getLeft(), param);
    visit(obj.getRight(), param);
    return null;
  }

  @Override
  protected R visitRelation(Relation obj, P param) {
    visit(obj.getVariable(), param);
    visit(obj.getLeft(), param);
    visit(obj.getRight(), param);
    return null;
  }

  @Override
  protected R visitVoidType(VoidType obj, P param) {
    return null;
  }

  @Override
  protected R visitNamedElement(NamedElement obj, P param) {
    visit(obj.getType(), param);
    return null;
  }

  @Override
  protected R visitReturnVoid(ReturnVoid obj, P param) {
    return null;
  }

  @Override
  protected R visitReturnExpr(ReturnExpr obj, P param) {
    visit(obj.getValue(), param);
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
  protected R visitStructType(StructType obj, P param) {
    visitList(obj.getElements(), param);
    return null;
  }

  @Override
  protected R visitUnionType(UnionType obj, P param) {
    visitList(obj.getElements(), param);
    return null;
  }

  @Override
  protected R visitEnumType(EnumType obj, P param) {
    visitList(obj.getElements(), param);
    return null;
  }

  @Override
  protected R visitEnumElement(EnumElement obj, P param) {
    return null;
  }

  @Override
  protected R visitBooleanType(BooleanType obj, P param) {
    return null;
  }

  @Override
  protected R visitConstant(Constant obj, P param) {
    visit(obj.getDef(), param);
    return null;
  }

  @Override
  protected R visitStateVariable(StateVariable obj, P param) {
    visit(obj.getType(), param);
    return null;
  }

  @Override
  protected R visitFuncVariable(FuncVariable obj, P param) {
    visit(obj.getType(), param);
    return null;
  }

  @Override
  protected R visitArray(ArrayType obj, P param) {
    return null;
  }

  @Override
  protected R visitBoolValue(BoolValue obj, P param) {
    return null;
  }

  @Override
  protected R visitStringValue(StringValue obj, P param) {
    return null;
  }

  @Override
  protected R visitArrayValue(ArrayValue obj, P param) {
    visitList(obj.getValue(), param);
    return null;
  }

  @Override
  protected R visitStringType(StringType obj, P param) {
    return null;
  }

  @Override
  protected R visitRangeType(RangeType obj, P param) {
    return null;
  }

  @Override
  protected R visitBasicBlockList(BasicBlockList obj, P param) {
    visit(obj.getEntry(), param);
    visitList(obj.getBasicBlocks(), param);
    visit(obj.getExit(), param);
    return null;
  }

  @Override
  protected R visitBasicBlock(BasicBlock obj, P param) {
    visitList(obj.getPhi(), param);
    visitList(obj.getCode(), param);
    visit(obj.getEnd(), param);
    return null;
  }

  @Override
  protected R visitSsaVariable(SsaVariable obj, P param) {
    visit(obj.getType(), param);
    return null;
  }

  @Override
  protected R visitGoto(Goto obj, P param) {
    return null;
  }

  @Override
  protected R visitPhiStmt(PhiStmt obj, P param) {
    visit(obj.getVariable(), param);
    visitList(obj.getReferences(), param);
    return null;
  }

  @Override
  protected R visitIfGoto(IfGoto obj, P param) {
    visit(obj.getCondition(), param);
    return null;
  }

  @Override
  protected R visitSignedType(SignedType obj, P param) {
    return null;
  }

  @Override
  protected R visitFuncImpl(FuncImpl obj, P param) {
    visitList(obj.getArgument(), param);
    visit(obj.getRetType(), param);
    visit(obj.getBody(), param);
    return null;
  }

  @Override
  protected R visitFuncProto(FuncProto obj, P param) {
    visitList(obj.getArgument(), param);
    visit(obj.getRetType(), param);
    return null;
  }

  @Override
  protected R visitCaseGoto(CaseGoto obj, P param) {
    visit(obj.getCondition(), param);
    visitList(obj.getOption(), param);
    return null;
  }

  @Override
  protected R visitVarRef(VarRef obj, P param) {
    visitList(obj.getOffset(), param);
    return null;
  }

  @Override
  protected R visitVarRefStatevar(VarRefStatevar obj, P param) {
    return null;
  }

  @Override
  protected R visitVarRefSimple(VarRefSimple obj, P param) {
    return null;
  }

  @Override
  protected R visitLoadStmt(LoadStmt obj, P param) {
    visit(obj.getSrc(), param);
    visit(obj.getVariable(), param);
    return null;
  }

  @Override
  protected R visitCallAssignment(CallAssignment obj, P param) {
    visitList(obj.getParameter(), param);
    visit(obj.getVariable(), param);
    return null;
  }

  @Override
  protected R visitGetElementPtr(GetElementPtr obj, P param) {
    visit(obj.getBase(), param);
    visitList(obj.getOffset(), param);
    visit(obj.getVariable(), param);
    return null;
  }

  @Override
  protected R visitTruncValue(TruncValue obj, P param) {
    visit(obj.getOriginal(), param);
    visit(obj.getVariable(), param);
    return null;
  }

  @Override
  protected R visitSignExtendValue(SignExtendValue obj, P param) {
    visit(obj.getOriginal(), param);
    visit(obj.getVariable(), param);
    return null;
  }

  @Override
  protected R visitZeroExtendValue(ZeroExtendValue obj, P param) {
    visit(obj.getOriginal(), param);
    visit(obj.getVariable(), param);
    return null;
  }

  @Override
  protected R visitTypeCast(TypeCast obj, P param) {
    visit(obj.getOriginal(), param);
    visit(obj.getVariable(), param);
    return null;
  }

  @Override
  protected R visitTypeRef(TypeRef obj, P param) {
    return null;
  }

  @Override
  protected R visitNoSignType(NoSignType obj, P param) {
    return null;
  }

  @Override
  protected R visitCaseOptValue(CaseOptValue obj, P param) {
    return null;
  }

  @Override
  protected R visitCaseOptRange(CaseOptRange obj, P param) {
    return null;
  }

  @Override
  protected R visitCaseGotoOpt(CaseGotoOpt obj, P param) {
    visitList(obj.getValue(), param);
    return null;
  }

  @Override
  protected R visitUnaryOp(UnaryOp obj, P param) {
    visit(obj.getVariable(), param);
    visit(obj.getExpr(), param);
    return null;
  }

  @Override
  protected R visitPointerType(PointerType obj, P param) {
    visit(obj.getType(), param);
    return null;
  }

  @Override
  protected R visitStackMemoryAlloc(StackMemoryAlloc obj, P param) {
    visit(obj.getVariable(), param);
    return null;
  }
}
