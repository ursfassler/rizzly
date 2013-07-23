package pir;

import pir.expression.ArithmeticOp;
import pir.expression.ArrayValue;
import pir.expression.BoolValue;
import pir.expression.Number;
import pir.expression.Reference;
import pir.expression.Relation;
import pir.expression.StringValue;
import pir.expression.UnaryExpr;
import pir.expression.reference.RefCall;
import pir.expression.reference.RefHead;
import pir.expression.reference.RefIndex;
import pir.expression.reference.RefName;
import pir.function.impl.FuncImplRet;
import pir.function.impl.FuncImplVoid;
import pir.function.impl.FuncProtoRet;
import pir.function.impl.FuncProtoVoid;
import pir.other.Constant;
import pir.other.FuncVariable;
import pir.other.Program;
import pir.other.StateVariable;
import pir.statement.Assignment;
import pir.statement.Block;
import pir.statement.CallStmt;
import pir.statement.CaseEntry;
import pir.statement.CaseOptRange;
import pir.statement.CaseOptValue;
import pir.statement.CaseStmt;
import pir.statement.IfStmt;
import pir.statement.IfStmtEntry;
import pir.statement.ReturnValue;
import pir.statement.ReturnVoid;
import pir.statement.VarDefStmt;
import pir.statement.WhileStmt;
import pir.type.Array;
import pir.type.BooleanType;
import pir.type.EnumElement;
import pir.type.EnumType;
import pir.type.NamedElement;
import pir.type.StringType;
import pir.type.StructType;
import pir.type.TypeAlias;
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
  protected R visitRefHead(RefHead obj, P param) {
    return doDefault(obj, param);
  }

  @Override
  protected R visitRefCall(RefCall obj, P param) {
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
  protected R visitReturnValue(ReturnValue obj, P param) {
    return doDefault(obj, param);
  }

  @Override
  protected R visitEnumElement(EnumElement obj, P param) {
    return doDefault(obj, param);
  }

  @Override
  protected R visitIfStmtEntry(IfStmtEntry obj, P param) {
    return doDefault(obj, param);
  }

  @Override
  protected R visitCaseEntry(CaseEntry obj, P param) {
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
  protected R visitReference(Reference obj, P param) {
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
  protected R visitCaseStmt(CaseStmt obj, P param) {
    return doDefault(obj, param);
  }

  @Override
  protected R visitWhile(WhileStmt obj, P param) {
    return doDefault(obj, param);
  }

  @Override
  protected R visitIfStmt(IfStmt obj, P param) {
    return doDefault(obj, param);
  }

  @Override
  protected R visitBlock(Block obj, P param) {
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
  protected R visitArray(Array obj, P param) {
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
  protected R visitFuncImplVoid(FuncImplVoid obj, P param) {
    return doDefault(obj, param);
  }

  @Override
  protected R visitFuncImplRet(FuncImplRet obj, P param) {
    return doDefault(obj, param);
  }

  @Override
  protected R visitFuncProtoVoid(FuncProtoVoid obj, P param) {
    return doDefault(obj, param);
  }

  @Override
  protected R visitFuncProtoRet(FuncProtoRet obj, P param) {
    return doDefault(obj, param);
  }

}
