package cir.traverser;

import cir.CirBase;
import cir.NullTraverser;
import cir.expression.BinaryOp;
import cir.expression.Number;
import cir.expression.UnaryOp;
import cir.expression.reference.RefCall;
import cir.expression.reference.RefIndex;
import cir.expression.reference.RefName;
import cir.expression.reference.Reference;
import cir.function.Function;
import cir.function.FunctionImpl;
import cir.function.FunctionPrototype;
import cir.other.Constant;
import cir.other.FuncVariable;
import cir.other.Program;
import cir.other.StateVariable;
import cir.statement.Assignment;
import cir.statement.Block;
import cir.statement.CallStmt;
import cir.statement.CaseEntry;
import cir.statement.CaseStmt;
import cir.statement.IfStmt;
import cir.statement.ReturnExpr;
import cir.statement.ReturnVoid;
import cir.statement.Statement;
import cir.statement.VarDefStmt;
import cir.statement.WhileStmt;
import cir.type.ArrayType;
import cir.type.EnumElement;
import cir.type.EnumType;
import cir.type.IntType;
import cir.type.NamedElement;
import cir.type.StructType;
import cir.type.TypeAlias;
import cir.type.UnionType;
import cir.type.VoidType;

public class StmtReplacer<P> extends NullTraverser<Statement, P> {
  @Override
  protected Statement visitDefault(CirBase obj, P param) {
    throw new RuntimeException("not yet implemented: " + obj.getClass().getCanonicalName());
  }

  @Override
  protected Statement visitProgram(Program obj, P param) {
    for (Function func : obj.getFunction()) {
      visit(func, param);
    }
    return null;
  }

  @Override
  protected Statement visitIntType(IntType obj, P param) {
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
  protected Statement visitFunctionImpl(FunctionImpl obj, P param) {
    visit(obj.getBody(), param);
    return null;
  }

  @Override
  protected Statement visitFunctionPrototype(FunctionPrototype obj, P param) {
    return null;
  }

  @Override
  protected Statement visitNumber(Number obj, P param) {
    return null;
  }

  @Override
  protected Statement visitBinaryOp(BinaryOp obj, P param) {
    return null;
  }

  @Override
  protected Statement visitUnaryOp(UnaryOp obj, P param) {
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
  protected Statement visitReturnValue(ReturnExpr obj, P param) {
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
  protected Statement visitIf(IfStmt obj, P param) {
    visit(obj.getCondition(), param);
    obj.setThenBlock(visit(obj.getThenBlock(), param));
    obj.setElseBlock(visit(obj.getElseBlock(), param));
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
  protected Statement visitArrayType(ArrayType obj, P param) {
    return null;
  }

  @Override
  protected Statement visitWhile(WhileStmt obj, P param) {
    obj.setBlock(visit(obj.getBlock(), param));
    return obj;
  }

  @Override
  protected Statement visitCaseEntry(CaseEntry obj, P param) {
    obj.setCode(visit(obj.getCode(), param));
    return null;
  }

  @Override
  protected Statement visitCaseStmt(CaseStmt obj, P param) {
    visitList(obj.getEntries(), param);
    obj.setOtherwise(visit(obj.getOtherwise(), param));
    return obj;
  }

}
