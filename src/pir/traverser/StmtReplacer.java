package pir.traverser;

import pir.Traverser;
import pir.cfg.CaseGoto;
import pir.cfg.CaseOptRange;
import pir.cfg.CaseOptValue;
import pir.cfg.IfGoto;
import pir.cfg.ReturnExpr;
import pir.cfg.ReturnVoid;
import pir.expression.ArrayValue;
import pir.expression.BoolValue;
import pir.expression.Number;
import pir.expression.Relation;
import pir.expression.StringValue;
import pir.expression.UnaryExpr;
import pir.expression.reference.RefCall;
import pir.expression.reference.RefHead;
import pir.expression.reference.RefIndex;
import pir.expression.reference.RefName;
import pir.expression.reference.Reference;
import pir.function.FuncImpl;
import pir.function.FuncProto;
import pir.function.FuncWithBody;
import pir.function.Function;
import pir.function.impl.FuncImplRet;
import pir.function.impl.FuncProtoRet;
import pir.other.Constant;
import pir.other.FuncVariable;
import pir.other.Program;
import pir.other.StateVariable;
import pir.statement.ArithmeticOp;
import pir.statement.Assignment;
import pir.statement.CallStmt;
import pir.statement.Statement;
import pir.statement.VarDefStmt;
import pir.type.Array;
import pir.type.BooleanType;
import pir.type.EnumElement;
import pir.type.EnumType;
import pir.type.NamedElement;
import pir.type.RangeType;
import pir.type.StringType;
import pir.type.StructType;
import pir.type.TypeAlias;
import pir.type.UnionType;
import pir.type.UnsignedType;
import pir.type.VoidType;

public class StmtReplacer<P> extends Traverser<Statement, P> {
  @Override
  protected Statement visitProgram(Program obj, P param) {
    for (Function func : obj.getFunction()) {
      visit(func, param);
    }
    return null;
  }

  @Override
  protected Statement visitCallStmt(CallStmt obj, P param) {
    return obj;
  }

  @Override
  protected Statement visitAssignment(Assignment obj, P param) {
    return obj;
  }

  @Override
  protected Statement visitBlock(Block obj, P param) {
    for (int i = 0; i < obj.getStatement().size(); i++) {
      Statement stmt = obj.getStatement().get(i);
      Statement nstmt = visit(stmt, param);
      assert (nstmt != null);
      obj.getStatement().set(i, nstmt);
    }
    return obj;
  }

  @Override
  protected Statement visitNumber(Number obj, P param) {
    return null;
  }

  @Override
  protected Statement visitArithmeticOp(ArithmeticOp obj, P param) {
    return null;
  }

  @Override
  protected Statement visitUnaryExpr(UnaryExpr obj, P param) {
    return null;
  }

  @Override
  protected Statement visitVoidType(VoidType obj, P param) {
    return null;
  }

  @Override
  protected Statement visitNamedElement(NamedElement obj, P param) {
    return null;
  }

  @Override
  protected Statement visitReturnVoid(ReturnVoid obj, P param) {
    return obj;
  }

  @Override
  protected Statement visitReturnValue(ReturnValue obj, P param) {
    return obj;
  }

  @Override
  protected Statement visitReference(Reference obj, P param) {
    return null;
  }

  @Override
  protected Statement visitRefCall(RefCall obj, P param) {
    return null;
  }

  @Override
  protected Statement visitRefName(RefName obj, P param) {
    return null;
  }

  @Override
  protected Statement visitRefIndex(RefIndex obj, P param) {
    return null;
  }

  @Override
  protected Statement visitRefHead(RefHead obj, P param) {
    return null;
  }

  @Override
  protected Statement visitStructType(StructType obj, P param) {
    return null;
  }

  @Override
  protected Statement visitUnionType(UnionType obj, P param) {
    return null;
  }

  @Override
  protected Statement visitEnumType(EnumType obj, P param) {
    return null;
  }

  @Override
  protected Statement visitEnumElement(EnumElement obj, P param) {
    return null;
  }

  @Override
  protected Statement visitIfStmt(IfStmt obj, P param) {
    visitList(obj.getOption(), param);
    visit(obj.getDef(), param);
    return obj;
  }

  @Override
  protected Statement visitTypeAlias(TypeAlias obj, P param) {
    return null;
  }

  @Override
  protected Statement visitConstant(Constant obj, P param) {
    return null;
  }

  @Override
  protected Statement visitFuncVariable(FuncVariable obj, P param) {
    return null;
  }

  @Override
  protected Statement visitStateVariable(StateVariable obj, P param) {
    return null;
  }

  @Override
  protected Statement visitVarDefStmt(VarDefStmt obj, P param) {
    return obj;
  }

  @Override
  protected Statement visitArray(Array obj, P param) {
    return null;
  }

  @Override
  protected Statement visitWhile(WhileStmt obj, P param) {
    visit(obj.getBlock(), param);
    return obj;
  }

  @Override
  protected Statement visitCaseEntry(CaseEntry obj, P param) {
    visitList(obj.getValues(), param);
    visit(obj.getCode(), param);
    return null;
  }

  @Override
  protected Statement visitCaseStmt(CaseStmt obj, P param) {
    visitList(obj.getEntries(), param);
    visit(obj.getOtherwise(), param);
    return obj;
  }

  @Override
  protected Statement visitCaseOptValue(CaseOptValue obj, P param) {
    return null;
  }

  @Override
  protected Statement visitCaseOptRange(CaseOptRange obj, P param) {
    return null;
  }

  @Override
  protected Statement visitIfStmtEntry(IfStmtEntry obj, P param) {
    visit(obj.getCode(), param);
    return null;
  }

  @Override
  protected Statement visitBoolValue(BoolValue obj, P param) {
    throw new RuntimeException("not yet implemented");
  }

  @Override
  protected Statement visitRelation(Relation obj, P param) {
    throw new RuntimeException("not yet implemented");
  }

  @Override
  protected Statement visitBooleanType(BooleanType obj, P param) {
    throw new RuntimeException("not yet implemented");
  }

  @Override
  protected Statement visitStringValue(StringValue obj, P param) {
    throw new RuntimeException("not yet implemented");
  }

  @Override
  protected Statement visitStringType(StringType obj, P param) {
    throw new RuntimeException("not yet implemented");
  }

  @Override
  protected Statement visitArrayValue(ArrayValue obj, P param) {
    throw new RuntimeException("not yet implemented");
  }

  @Override
  protected Statement visitFunction(Function obj, P param) {
    if (obj instanceof FuncWithBody) {
      visit(((FuncWithBody) obj).getBody(), param); // FIXME ok?
    }
    return null;
  }

  @Override
  protected Statement visitFuncImpl(FuncImpl obj, P param) {
    throw new RuntimeException("not yet implemented");
  }

  @Override
  protected Statement visitFuncImplRet(FuncImplRet obj, P param) {
    throw new RuntimeException("not yet implemented");
  }

  @Override
  protected Statement visitFuncProto(FuncProto obj, P param) {
    throw new RuntimeException("not yet implemented");
  }

  @Override
  protected Statement visitFuncProtoRet(FuncProtoRet obj, P param) {
    throw new RuntimeException("not yet implemented");
  }

  @Override
  protected Statement visitRangeType(RangeType obj, P param) {
    throw new RuntimeException("not yet implemented");
  }

  @Override
  protected Statement visitUnsignedType(UnsignedType obj, P param) {
    throw new RuntimeException("not yet implemented");
  }

  @Override
  protected Statement visitCaseGoto(CaseGoto obj, P param) {
    throw new RuntimeException("not yet implemented");
  }

  @Override
  protected Statement visitReturnExpr(ReturnExpr obj, P param) {
    throw new RuntimeException("not yet implemented");
  }

  @Override
  protected Statement visitIfGoto(IfGoto obj, P param) {
    throw new RuntimeException("not yet implemented");
  }
}
