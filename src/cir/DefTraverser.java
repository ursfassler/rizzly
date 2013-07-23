package cir;

import cir.expression.ArrayValue;
import cir.expression.BinaryOp;
import cir.expression.BoolValue;
import cir.expression.Number;
import cir.expression.Reference;
import cir.expression.StringValue;
import cir.expression.UnaryOp;
import cir.expression.reference.RefCall;
import cir.expression.reference.RefHead;
import cir.expression.reference.RefIndex;
import cir.expression.reference.RefName;
import cir.function.FunctionImpl;
import cir.function.FunctionPrototype;
import cir.function.LibFunction;
import cir.library.CLibrary;
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
import cir.statement.ReturnValue;
import cir.statement.ReturnVoid;
import cir.statement.VarDefStmt;
import cir.statement.WhileStmt;
import cir.type.ArrayType;
import cir.type.BooleanType;
import cir.type.EnumElement;
import cir.type.EnumType;
import cir.type.IntType;
import cir.type.NamedElement;
import cir.type.StringType;
import cir.type.StructType;
import cir.type.TypeAlias;
import cir.type.UnionType;
import cir.type.VoidType;

public class DefTraverser<R, P> extends Traverser<R, P> {

  @Override
  protected R visitProgram(Program obj, P param) {
    visitList(obj.getLibrary(), param);
    visitList(obj.getType(), param);
    visitList(obj.getVariable(), param);
    visitList(obj.getFunction(), param);
    return null;
  }

  @Override
  protected R visitCLibrary(CLibrary obj, P param) {
    visitList(obj.getFunction(), param);
    return null;
  }

  @Override
  protected R visitIntType(IntType obj, P param) {
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
  protected R visitFunctionImpl(FunctionImpl obj, P param) {
    visitList(obj.getArgument(), param);
    visit(obj.getBody(), param);
    return null;
  }

  @Override
  protected R visitFunctionPrototype(FunctionPrototype obj, P param) {
    visitList(obj.getArgument(), param);
    return null;
  }

  @Override
  protected R visitLibFunction(LibFunction obj, P param) {
    visitList(obj.getArgument(), param);
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
    return null;
  }

  @Override
  protected R visitReturnVoid(ReturnVoid obj, P param) {
    return null;
  }

  @Override
  protected R visitReturnValue(ReturnValue obj, P param) {
    visit(obj.getValue(), param);
    return null;
  }

  @Override
  protected R visitReference(Reference obj, P param) {
    visit(obj.getRef(), param);
    return null;
  }

  @Override
  protected R visitRefCall(RefCall obj, P param) {
    visit(obj.getPrevious(), param);
    visitList(obj.getParameter(), param);
    return null;
  }

  @Override
  protected R visitRefName(RefName obj, P param) {
    visit(obj.getPrevious(), param);
    return null;
  }

  @Override
  protected R visitRefIndex(RefIndex obj, P param) {
    visit(obj.getPrevious(), param);
    visit(obj.getIndex(), param);
    return null;
  }

  @Override
  protected R visitRefHead(RefHead obj, P param) {
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
    return null;
  }

  @Override
  protected R visitFuncVariable(FuncVariable obj, P param) {
    return null;
  }

  @Override
  protected R visitStateVariable(StateVariable obj, P param) {
    return null;
  }

  @Override
  protected R visitVarDefStmt(VarDefStmt obj, P param) {
    visit(obj.getVariable(), param);
    return null;
  }

  @Override
  protected R visitArrayType(ArrayType obj, P param) {
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
  protected R visitBooleanType(BooleanType obj, P param) {
    return null;
  }

  @Override
  protected R visitBoolValue(BoolValue obj, P param) {
    return null;
  }

}
