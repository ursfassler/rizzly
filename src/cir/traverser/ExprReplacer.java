package cir.traverser;

import java.util.List;

import cir.CirBase;
import cir.NullTraverser;
import cir.expression.ArrayValue;
import cir.expression.BinaryOp;
import cir.expression.BoolValue;
import cir.expression.ElementValue;
import cir.expression.Expression;
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
import cir.function.FunctionImpl;
import cir.function.FunctionPrototype;
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
import cir.type.EnumType;
import cir.type.IntType;
import cir.type.PointerType;
import cir.type.StringType;
import cir.type.StructType;
import cir.type.TypeAlias;
import cir.type.TypeRef;
import cir.type.UnionType;
import cir.type.VoidType;
import cir.variable.Constant;
import cir.variable.FuncVariable;
import cir.variable.StateVariable;

public class ExprReplacer<T> extends NullTraverser<Expression, T> {

  @Override
  protected Expression visitDefault(CirBase obj, T param) {
    throw new RuntimeException("not yet implemented: " + obj.getClass().getCanonicalName());
  }

  protected void visitExprList(List<Expression> parameter, T param) {
    for (int i = 0; i < parameter.size(); i++) {
      parameter.set(i, visit(parameter.get(i), param));
    }
  }

  @Override
  protected Expression visitRefCall(RefCall obj, T param) {
    visitExprList(obj.getParameter(), param);
    return null;
  }

  @Override
  protected Expression visitRefIndex(RefIndex obj, T param) {
    obj.setIndex(visit(obj.getIndex(), param));
    return null;
  }

  @Override
  protected Expression visitRefName(RefName obj, T param) {
    return null;
  }

  @Override
  protected Expression visitReturnVoid(ReturnVoid obj, T param) {
    return null;
  }

  @Override
  protected Expression visitReturnValue(ReturnExpr obj, T param) {
    obj.setValue(visit(obj.getValue(), param));
    return null;
  }

  @Override
  protected Expression visitCaseEntry(CaseEntry obj, T param) {
    visit(obj.getCode(), param);
    return null;
  }

  @Override
  protected Expression visitConstant(Constant obj, T param) {
    obj.setDef(visit(obj.getDef(), param));
    return null;
  }

  @Override
  protected Expression visitStateVariable(StateVariable obj, T param) {
    return null;
  }

  @Override
  protected Expression visitFuncVariable(FuncVariable obj, T param) {
    return null;
  }

  @Override
  protected Expression visitTypeAlias(TypeAlias obj, T param) {
    return null;
  }

  @Override
  protected Expression visitVoidType(VoidType obj, T param) {
    return null;
  }

  @Override
  protected Expression visitFunctionImpl(FunctionImpl obj, T param) {
    visitList(obj.getArgument(), param);
    visit(obj.getBody(), param);
    return null;
  }

  @Override
  protected Expression visitFunctionPrototype(FunctionPrototype obj, T param) {
    visitList(obj.getArgument(), param);
    return null;
  }

  @Override
  protected Expression visitVarDefStmt(VarDefStmt obj, T param) {
    return null;
  }

  @Override
  protected Expression visitCallStmt(CallStmt obj, T param) {
    visit(obj.getRef(), param);
    return null;
  }

  @Override
  protected Expression visitAssignment(Assignment obj, T param) {
    visit(obj.getDst(), param);
    obj.setSrc(visit(obj.getSrc(), param));
    return null;
  }

  @Override
  protected Expression visitCaseStmt(CaseStmt obj, T param) {
    obj.setCondition(visit(obj.getCondition(), param));
    visitList(obj.getEntries(), param);
    visit(obj.getOtherwise(), param);
    return null;
  }

  @Override
  protected Expression visitWhile(WhileStmt obj, T param) {
    obj.setCond(visit(obj.getCond(), param));
    visit(obj.getBlock(), param);
    return null;
  }

  @Override
  protected Expression visitIf(IfStmt obj, T param) {
    obj.setCondition(visit(obj.getCondition(), param));
    visit(obj.getThenBlock(), param);
    visit(obj.getElseBlock(), param);
    return null;
  }

  @Override
  protected Expression visitBlock(Block obj, T param) {
    visitList(obj.getStatement(), param);
    return null;
  }

  @Override
  protected Expression visitEnumType(EnumType obj, T param) {
    return null;
  }

  @Override
  protected Expression visitStructType(StructType obj, T param) {
    return null;
  }

  @Override
  protected Expression visitUnionType(UnionType obj, T param) {
    return null;
  }

  @Override
  protected Expression visitBooleanType(BooleanType obj, T param) {
    return null;
  }

  @Override
  protected Expression visitArrayType(ArrayType obj, T param) {
    return null;
  }

  @Override
  protected Expression visitStringType(StringType obj, T param) {
    return null;
  }

  @Override
  protected Expression visitIntType(IntType obj, T param) {
    return null;
  }

  @Override
  protected Expression visitProgram(Program obj, T param) {
    // visitList(obj.getConstant(), param);
    visitList(obj.getFunction(), param);
    visitList(obj.getType(), param);
    visitList(obj.getVariable(), param);
    return null;
  }

  @Override
  protected Expression visitBoolValue(BoolValue obj, T param) {
    return obj;
  }

  @Override
  protected Expression visitArrayValue(ArrayValue obj, T param) {
    visitExprList(obj.getValue(), param);
    return obj;
  }

  @Override
  protected Expression visitElementValue(ElementValue obj, T param) {
    throw new RuntimeException("not yet implemented");
  }

  @Override
  protected Expression visitStructValue(StructValue obj, T param) {
    throw new RuntimeException("not yet implemented");
  }

  @Override
  protected Expression visitUnsafeUnionValue(UnsafeUnionValue obj, T param) {
    throw new RuntimeException("not yet implemented");
  }

  @Override
  protected Expression visitUnionValue(UnionValue obj, T param) {
    throw new RuntimeException("not yet implemented");
  }

  @Override
  protected Expression visitStringValue(StringValue obj, T param) {
    return obj;
  }

  @Override
  protected Expression visitNumber(Number obj, T param) {
    return obj;
  }

  @Override
  protected Expression visitReference(Reference obj, T param) {
    visitList(obj.getOffset(), param);
    return obj;
  }

  @Override
  protected Expression visitBinaryOp(BinaryOp obj, T param) {
    obj.setLeft(visit(obj.getLeft(), param));
    obj.setRight(visit(obj.getRight(), param));
    return obj;
  }

  @Override
  protected Expression visitUnaryOp(UnaryOp obj, T param) {
    obj.setExpr(visit(obj.getExpr(), param));
    return obj;
  }

  @Override
  protected Expression visitTypeCast(TypeCast obj, T param) {
    visit(obj.getCast(), param);
    obj.setValue(visit(obj.getValue(), param));
    return obj;
  }

  @Override
  protected Expression visitTypeRef(TypeRef obj, T param) {
    return null;
  }

  @Override
  protected Expression visitPointerType(PointerType obj, T param) {
    visit(obj.getType(), param);
    return null;
  }

}
