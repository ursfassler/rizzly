package cir;

import cir.expression.ArrayValue;
import cir.expression.BinaryOp;
import cir.expression.BoolValue;
import cir.expression.Number;
import cir.expression.StringValue;
import cir.expression.TypeCast;
import cir.expression.UnaryOp;
import cir.expression.reference.RefCall;
import cir.expression.reference.RefIndex;
import cir.expression.reference.RefName;
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
import cir.statement.VarDefStmt;
import cir.statement.WhileStmt;
import cir.type.ArrayType;
import cir.type.BooleanType;
import cir.type.EnumElement;
import cir.type.EnumType;
import cir.type.NamedElement;
import cir.type.PointerType;
import cir.type.RangeType;
import cir.type.SIntType;
import cir.type.StringType;
import cir.type.StructType;
import cir.type.TypeAlias;
import cir.type.TypeRef;
import cir.type.UIntType;
import cir.type.UnionType;
import cir.type.UnsafeUnionType;
import cir.type.VoidType;

public abstract class NullTraverser<R, P> extends Traverser<R, P> {

  protected abstract R visitDefault(CirBase obj, P param);

  @Override
  protected R visitRangeType(RangeType obj, P param) {
    return visitDefault(obj, param);
  }

  @Override
  protected R visitPointerType(PointerType obj, P param) {
    return visitDefault(obj, param);
  }

  @Override
  protected R visitTypeRef(TypeRef obj, P param) {
    return visitDefault(obj, param);
  }

  @Override
  protected R visitTypeCast(TypeCast obj, P param) {
    return visitDefault(obj, param);
  }

  @Override
  protected R visitReference(cir.expression.reference.Reference obj, P param) {
    return visitDefault(obj, param);
  }

  @Override
  protected R visitArrayValue(ArrayValue obj, P param) {
    return visitDefault(obj, param);
  }

  @Override
  protected R visitStringValue(StringValue obj, P param) {
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
  protected R visitRefIndex(RefIndex obj, P param) {
    return visitDefault(obj, param);
  }

  @Override
  protected R visitRefName(RefName obj, P param) {
    return visitDefault(obj, param);
  }

  @Override
  protected R visitReturnVoid(ReturnVoid obj, P param) {
    return visitDefault(obj, param);
  }

  @Override
  protected R visitReturnValue(ReturnExpr obj, P param) {
    return visitDefault(obj, param);
  }

  @Override
  protected R visitEnumElement(EnumElement obj, P param) {
    return visitDefault(obj, param);
  }

  @Override
  protected R visitCaseEntry(CaseEntry obj, P param) {
    return visitDefault(obj, param);
  }

  @Override
  protected R visitFuncVariable(FuncVariable obj, P param) {
    return visitDefault(obj, param);
  }

  @Override
  protected R visitConstant(Constant obj, P param) {
    return visitDefault(obj, param);
  }

  @Override
  protected R visitStateVariable(StateVariable obj, P param) {
    return visitDefault(obj, param);
  }

  @Override
  protected R visitNamedElement(NamedElement obj, P param) {
    return visitDefault(obj, param);
  }

  @Override
  protected R visitTypeAlias(TypeAlias obj, P param) {
    return visitDefault(obj, param);
  }

  @Override
  protected R visitVoidType(VoidType obj, P param) {
    return visitDefault(obj, param);
  }

  @Override
  protected R visitNumber(Number obj, P param) {
    return visitDefault(obj, param);
  }

  @Override
  protected R visitBinaryOp(BinaryOp obj, P param) {
    return visitDefault(obj, param);
  }

  @Override
  protected R visitUnaryOp(UnaryOp obj, P param) {
    return visitDefault(obj, param);
  }

  @Override
  protected R visitFunctionImpl(FunctionImpl obj, P param) {
    return visitDefault(obj, param);
  }

  @Override
  protected R visitFunctionPrototype(FunctionPrototype obj, P param) {
    return visitDefault(obj, param);
  }

  @Override
  protected R visitVarDefStmt(VarDefStmt obj, P param) {
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
  protected R visitCaseStmt(CaseStmt obj, P param) {
    return visitDefault(obj, param);
  }

  @Override
  protected R visitWhile(WhileStmt obj, P param) {
    return visitDefault(obj, param);
  }

  @Override
  protected R visitIf(IfStmt obj, P param) {
    return visitDefault(obj, param);
  }

  @Override
  protected R visitBlock(Block obj, P param) {
    return visitDefault(obj, param);
  }

  @Override
  protected R visitEnumType(EnumType obj, P param) {
    return visitDefault(obj, param);
  }

  @Override
  protected R visitStructType(StructType obj, P param) {
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
  protected R visitArrayType(ArrayType obj, P param) {
    return visitDefault(obj, param);
  }

  @Override
  protected R visitSIntType(SIntType obj, P param) {
    return visitDefault(obj, param);
  }

  @Override
  protected R visitUIntType(UIntType obj, P param) {
    return visitDefault(obj, param);
  }

  @Override
  protected R visitProgram(Program obj, P param) {
    return visitDefault(obj, param);
  }

  @Override
  protected R visitBooleanType(BooleanType obj, P param) {
    return visitDefault(obj, param);
  }

  @Override
  protected R visitBoolValue(BoolValue obj, P param) {
    return visitDefault(obj, param);
  }

}
