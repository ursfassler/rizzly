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
import pir.expression.reference.CallExpr;
import pir.expression.reference.RefIndex;
import pir.expression.reference.RefName;
import pir.expression.reference.VarRef;
import pir.function.FuncImpl;
import pir.function.FuncProto;
import pir.function.FuncWithBody;
import pir.function.Function;
import pir.other.Constant;
import pir.other.FuncVariable;
import pir.other.Program;
import pir.other.SsaVariable;
import pir.other.StateVariable;
import pir.other.Variable;
import pir.statement.ArithmeticOp;
import pir.statement.Assignment;
import pir.statement.CallStmt;
import pir.statement.Relation;
import pir.statement.StoreStmt;
import pir.statement.VarDefStmt;
import pir.type.Array;
import pir.type.BooleanType;
import pir.type.EnumElement;
import pir.type.EnumType;
import pir.type.NamedElement;
import pir.type.RangeType;
import pir.type.SignedType;
import pir.type.StringType;
import pir.type.StructType;
import pir.type.Type;
import pir.type.TypeAlias;
import pir.type.UnionType;
import pir.type.UnsignedType;
import pir.type.VoidType;
import evl.function.impl.FuncProtoRet;
import evl.statement.VarDefInitStmt;

public class DefTraverser<R, P> extends Traverser<R, P> {

  @Override
  protected R visitProgram(Program obj, P param) {
    for (Type type : obj.getType()) {
      visit(type, param);
    }
    for (Variable var : obj.getVariable()) {
      visit(var, param);
    }
    for (Function func : obj.getFunction()) {
      visit(func, param);
    }
    return null;
  }

  @Override
  protected R visitUnsignedType(UnsignedType obj, P param) {
    return null;
  }

  @Override
  protected R visitCallStmt(CallStmt obj, P param) {
    visit(obj.getCall(), param);
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
    return null;
  }

  @Override
  protected R visitArithmeticOp(ArithmeticOp obj, P param) {
    visit(obj.getLeft(), param);
    visit(obj.getRight(), param);
    return null;
  }

  @Override
  protected R visitUnaryExpr(UnaryExpr obj, P param) {
    visit(obj.getExpr(), param);
    return null;
  }

  @Override
  protected R visitRelation(Relation obj, P param) {
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
    return null;
  }

  @Override
  protected R visitReturnVoid(ReturnVoid obj, P param) {
    return null;
  }

  @Override
  protected R visitReturnExpr(ReturnExpr obj, P param) {
    visit(obj.getExpr(), param);
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
  protected R visitTypeAlias(TypeAlias obj, P param) {
    visit(obj.getRef(), param);
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
    return null;
  }

  @Override
  protected R visitFuncVariable(FuncVariable obj, P param) {
    return null;
  }

  @Override
  protected R visitVarDefStmt(VarDefStmt obj, P param) {
    visit(obj.getVariable(), param);
    return null;
  }

  @Override
  protected R visitArray(Array obj, P param) {
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
  protected R visitFunction(Function obj, P param) {
    visitList(obj.getArgument(), param);
    if (obj instanceof FuncWithBody) {
      visit(((FuncWithBody) obj).getBody(), param);
    }
    return null;
  }

  @Override
  protected R visitRangeType(RangeType obj, P param) {
    return null;
  }

  @Override
  protected R visitBasicBlockList(BasicBlockList obj, P param) {
    visitList(obj.getBasicBlocks(), param);
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
  protected R visitVarDefInitStmt(VarDefInitStmt obj, P param) {
    visit(obj.getVariable(), param);
    visit(obj.getInit(), param);
    return null;
  }

  @Override
  protected R visitSsaVariable(SsaVariable obj, P param) {
    return null;
  }

  @Override
  protected R visitGoto(Goto obj, P param) {
    return null;
  }

  @Override
  protected R visitPhiStmt(PhiStmt obj, P param) {
    visit(obj.getVariable(), param);
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
    throw new RuntimeException("not yet implemented");
  }

  @Override
  protected R visitFuncProto(FuncProto obj, P param) {
    throw new RuntimeException("not yet implemented");
  }

  @Override
  protected R visitCaseGoto(CaseGoto obj, P param) {
    throw new RuntimeException("not yet implemented");
  }

  @Override
  protected R visitCallExpr(CallExpr obj, P param) {
    visitList(obj.getParameter(), param);
    return null;
  }

  @Override
  protected R visitVarRef(VarRef obj, P param) {
    return null;
  }

}
