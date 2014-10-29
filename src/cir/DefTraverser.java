package cir;

import cir.expression.ArrayValue;
import cir.expression.BinaryOp;
import cir.expression.BoolValue;
import cir.expression.ElementValue;
import cir.expression.NoValue;
import cir.expression.Number;
import cir.expression.StringValue;
import cir.expression.StructValue;
import cir.expression.TypeCast;
import cir.expression.UnaryOp;
import cir.expression.UnionValue;
import cir.expression.UnsafeUnionValue;
import cir.expression.reference.RefCall;
import cir.expression.reference.RefIndex;
import cir.expression.reference.RefName;
import cir.expression.reference.Reference;
import cir.function.FunctionPrivate;
import cir.function.FunctionPrototype;
import cir.function.FunctionPublic;
import cir.other.Program;
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
import cir.variable.Constant;
import cir.variable.FuncVariable;
import cir.variable.StateVariable;

public class DefTraverser<R, P> extends Traverser<R, P> {

  @Override
  protected R visitProgram(Program obj, P param) {
    visitList(obj.getType(), param);
    visitList(obj.getVariable(), param);
    visitList(obj.getFunction(), param);
    return null;
  }

  @Override
  protected R visitCallStmt(CallStmt obj, P param) {
    visit(obj.getRef(), param);
    return null;
  }

  @Override
  protected R visitAssignment(Assignment obj, P param) {
    visit(obj.getDst(), param);
    visit(obj.getSrc(), param);
    return null;
  }

  @Override
  protected R visitBlock(Block obj, P param) {
    visitList(obj.getStatement(), param);
    return null;
  }

  @Override
  protected R visitFunctionPrivate(FunctionPrivate obj, P param) {
    visitList(obj.getArgument(), param);
    visit(obj.getRetType(), param);
    visit(obj.getBody(), param);
    return null;
  }

  @Override
  protected R visitFunctionPublic(FunctionPublic obj, P param) {
    visitList(obj.getArgument(), param);
    visit(obj.getRetType(), param);
    visit(obj.getBody(), param);
    return null;
  }

  @Override
  protected R visitFunctionPrototype(FunctionPrototype obj, P param) {
    visitList(obj.getArgument(), param);
    visit(obj.getRetType(), param);
    return null;
  }

  @Override
  protected R visitNumber(Number obj, P param) {
    return null;
  }

  @Override
  protected R visitBinaryOp(BinaryOp obj, P param) {
    visit(obj.getLeft(), param);
    visit(obj.getRight(), param);
    return null;
  }

  @Override
  protected R visitUnaryOp(UnaryOp obj, P param) {
    visit(obj.getExpr(), param);
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
  protected R visitReturnValue(ReturnExpr obj, P param) {
    visit(obj.getValue(), param);
    return null;
  }

  @Override
  protected R visitReference(Reference obj, P param) {
    visitList(obj.getOffset(), param);
    return null;
  }

  @Override
  protected R visitRefCall(RefCall obj, P param) {
    visitList(obj.getParameter(), param);
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
  protected R visitPointerType(PointerType obj, P param) {
    visit(obj.getType(), param);
    return null;
  }

  @Override
  protected R visitStructType(StructType obj, P param) {
    visitList(obj.getElements(), param);
    return null;
  }

  @Override
  protected R visitUnionType(UnionType obj, P param) {
    visit(obj.getTag(), param);
    visitList(obj.getElements(), param);
    return null;
  }

  @Override
  protected R visitUnsafeUnionType(UnsafeUnionType obj, P param) {
    visitList(obj.getElements(), param);
    return null;
  }

  @Override
  protected R visitIf(IfStmt obj, P param) {
    visit(obj.getCondition(), param);
    visit(obj.getThenBlock(), param);
    visit(obj.getElseBlock(), param);
    return null;
  }

  @Override
  protected R visitTypeAlias(TypeAlias obj, P param) {
    visit(obj.getRef(), param);
    return null;
  }

  @Override
  protected R visitConstant(Constant obj, P param) {
    visit(obj.getDef(), param);
    visit(obj.getType(), param);
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
    return null;
  }

  @Override
  protected R visitVarDefStmt(VarDefStmt obj, P param) {
    visit(obj.getVariable(), param);
    return null;
  }

  @Override
  protected R visitRangeType(RangeType obj, P param) {
    return null;
  }

  @Override
  protected R visitArrayType(ArrayType obj, P param) {
    visit(obj.getType(), param);
    return null;
  }

  @Override
  protected R visitWhile(WhileStmt obj, P param) {
    visit(obj.getCondition(), param);
    visit(obj.getBlock(), param);
    return null;
  }

  @Override
  protected R visitCaseEntry(CaseEntry obj, P param) {
    visit(obj.getCode(), param);
    return null;
  }

  @Override
  protected R visitCaseStmt(CaseStmt obj, P param) {
    visit(obj.getCondition(), param);
    visitList(obj.getEntries(), param);
    visit(obj.getOtherwise(), param);
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
  protected R visitStructValue(StructValue obj, P param) {
    visitList(obj.getValue(), param);
    return null;
  }

  @Override
  protected R visitBooleanType(BooleanType obj, P param) {
    return null;
  }

  @Override
  protected R visitBoolValue(BoolValue obj, P param) {
    return null;
  }

  @Override
  protected R visitTypeCast(TypeCast obj, P param) {
    visit(obj.getCast(), param);
    visit(obj.getValue(), param);
    return null;
  }

  @Override
  protected R visitTypeRef(TypeRef obj, P param) {
    return null;
  }

  @Override
  protected R visitSIntType(SIntType obj, P param) {
    return null;
  }

  @Override
  protected R visitUIntType(UIntType obj, P param) {
    return null;
  }

  @Override
  protected R visitNoValue(NoValue obj, P param) {
    return null;
  }

  @Override
  protected R visitUnsafeUnionValue(UnsafeUnionValue obj, P param) {
    visit(obj.getContentValue(), param);
    return null;
  }

  @Override
  protected R visitUnionValue(UnionValue obj, P param) {
    visit(obj.getTagValue(), param);
    visit(obj.getContentValue(), param);
    return null;
  }

  @Override
  protected R visitElementValue(ElementValue obj, P param) {
    visit(obj.getValue(), param);
    return null;
  }

}
