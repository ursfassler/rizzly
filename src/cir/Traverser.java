package cir;

import java.util.Collection;

import cir.expression.ArrayValue;
import cir.expression.BinaryOp;
import cir.expression.BoolValue;
import cir.expression.Expression;
import cir.expression.Number;
import cir.expression.StringValue;
import cir.expression.TypeCast;
import cir.expression.UnaryOp;
import cir.expression.reference.RefCall;
import cir.expression.reference.RefIndex;
import cir.expression.reference.RefItem;
import cir.expression.reference.RefName;
import cir.expression.reference.Reference;
import cir.function.Function;
import cir.function.FunctionImpl;
import cir.function.FunctionPrototype;
import cir.other.Constant;
import cir.other.FuncVariable;
import cir.other.Program;
import cir.other.StateVariable;
import cir.other.Variable;
import cir.statement.Assignment;
import cir.statement.Block;
import cir.statement.CallStmt;
import cir.statement.CaseEntry;
import cir.statement.CaseStmt;
import cir.statement.IfStmt;
import cir.statement.Return;
import cir.statement.ReturnExpr;
import cir.statement.ReturnVoid;
import cir.statement.Statement;
import cir.statement.VarDefStmt;
import cir.statement.WhileStmt;
import cir.type.ArrayType;
import cir.type.BooleanType;
import cir.type.EnumElement;
import cir.type.EnumType;
import cir.type.IntType;
import cir.type.NamedElemType;
import cir.type.NamedElement;
import cir.type.PointerType;
import cir.type.RangeType;
import cir.type.SIntType;
import cir.type.StringType;
import cir.type.StructType;
import cir.type.Type;
import cir.type.TypeAlias;
import cir.type.TypeRef;
import cir.type.UIntType;
import cir.type.UnionType;
import cir.type.VoidType;

public abstract class Traverser<R, P> {
  public R traverse(Cir obj, P param) {
    return visit(obj, param);
  }

  protected void visitList(Collection<? extends CirBase> list, P param) {
    for (CirBase itr : list) {
      visit(itr, param);
    }
  }

  protected R visit(Cir obj, P param) {
    if (obj == null)
      throw new RuntimeException("object is null");
    else if (obj instanceof Program)
      return visitProgram((Program) obj, param);
    else if (obj instanceof Statement)
      return visitStatement((Statement) obj, param);
    else if (obj instanceof Expression)
      return visitExpression((Expression) obj, param);
    else if (obj instanceof Type)
      return visitType((Type) obj, param);
    else if (obj instanceof Function)
      return visitFunction((Function) obj, param);
    else if (obj instanceof NamedElement)
      return visitNamedElement((NamedElement) obj, param);
    else if (obj instanceof RefItem)
      return visitRefItem((RefItem) obj, param);
    else if (obj instanceof Variable)
      return visitVariable((Variable) obj, param);
    else if (obj instanceof EnumElement)
      return visitEnumElement((EnumElement) obj, param);
    else if (obj instanceof CaseEntry)
      return visitCaseEntry((CaseEntry) obj, param);
    else if (obj instanceof TypeRef)
      return visitTypeRef((TypeRef) obj, param);
    else
      throw new RuntimeException("Unknow object: " + obj.getClass().getSimpleName());
  }

  protected R visitFunction(Function obj, P param) {
    if (obj instanceof FunctionImpl)
      return visitFunctionImpl((FunctionImpl) obj, param);
    else if (obj instanceof FunctionPrototype)
      return visitFunctionPrototype((FunctionPrototype) obj, param);
    else
      throw new RuntimeException("Unknow object: " + obj.getClass().getSimpleName());
  }

  protected R visitRefItem(RefItem obj, P param) {
    if (obj instanceof RefName)
      return visitRefName((RefName) obj, param);
    else if (obj instanceof RefCall)
      return visitRefCall((RefCall) obj, param);
    else if (obj instanceof RefIndex)
      return visitRefIndex((RefIndex) obj, param);
    else
      throw new RuntimeException("Unknow object: " + obj.getClass().getSimpleName());
  }

  protected R visitExpression(Expression obj, P param) {
    if (obj instanceof Reference)
      return visitReference((Reference) obj, param);
    else if (obj instanceof BinaryOp)
      return visitBinaryOp((BinaryOp) obj, param);
    else if (obj instanceof UnaryOp)
      return visitUnaryOp((UnaryOp) obj, param);
    else if (obj instanceof Number)
      return visitNumber((Number) obj, param);
    else if (obj instanceof StringValue)
      return visitStringValue((StringValue) obj, param);
    else if (obj instanceof ArrayValue)
      return visitArrayValue((ArrayValue) obj, param);
    else if (obj instanceof BoolValue)
      return visitBoolValue((BoolValue) obj, param);
    else if (obj instanceof TypeCast)
      return visitTypeCast((TypeCast) obj, param);
    else
      throw new RuntimeException("Unknow object: " + obj.getClass().getSimpleName());
  }

  protected R visitStatement(Statement obj, P param) {
    if (obj instanceof Block)
      return visitBlock((Block) obj, param);
    else if (obj instanceof Assignment)
      return visitAssignment((Assignment) obj, param);
    else if (obj instanceof CallStmt)
      return visitCallStmt((CallStmt) obj, param);
    else if (obj instanceof VarDefStmt)
      return visitVarDefStmt((VarDefStmt) obj, param);
    else if (obj instanceof Return)
      return visitReturn((Return) obj, param);
    else if (obj instanceof IfStmt)
      return visitIf((IfStmt) obj, param);
    else if (obj instanceof WhileStmt)
      return visitWhile((WhileStmt) obj, param);
    else if (obj instanceof CaseStmt)
      return visitCaseStmt((CaseStmt) obj, param);
    else
      throw new RuntimeException("Unknow object: " + obj.getClass().getSimpleName());
  }

  protected R visitVariable(Variable obj, P param) {
    if (obj instanceof StateVariable)
      return visitStateVariable((StateVariable) obj, param);
    else if (obj instanceof Constant)
      return visitConstant((Constant) obj, param);
    else if (obj instanceof FuncVariable)
      return visitFuncVariable((FuncVariable) obj, param);
    else
      throw new RuntimeException("Unknow object: " + obj.getClass().getSimpleName());
  }

  protected R visitReturn(Return obj, P param) {
    if (obj instanceof ReturnExpr)
      return visitReturnValue((ReturnExpr) obj, param);
    else if (obj instanceof ReturnVoid)
      return visitReturnVoid((ReturnVoid) obj, param);
    else
      throw new RuntimeException("Unknow object: " + obj.getClass().getSimpleName());
  }

  protected R visitType(Type obj, P param) {
    if (obj instanceof IntType)
      return visitIntType((IntType) obj, param);
    else if (obj instanceof RangeType)
      return visitRangeType((RangeType) obj, param);
    else if (obj instanceof NamedElemType)
      return visitNamedElemType((NamedElemType) obj, param);
    else if (obj instanceof EnumType)
      return visitEnumType((EnumType) obj, param);
    else if (obj instanceof VoidType)
      return visitVoidType((VoidType) obj, param);
    else if (obj instanceof TypeAlias)
      return visitTypeAlias((TypeAlias) obj, param);
    else if (obj instanceof ArrayType)
      return visitArrayType((ArrayType) obj, param);
    else if (obj instanceof StringType)
      return visitStringType((StringType) obj, param);
    else if (obj instanceof BooleanType)
      return visitBooleanType((BooleanType) obj, param);
    else if (obj instanceof PointerType)
      return visitPointerType((PointerType) obj, param);
    else
      throw new RuntimeException("Unknow object: " + obj.getClass().getSimpleName());
  }

  protected R visitIntType(IntType obj, P param) {
    if (obj instanceof UIntType)
      return visitUIntType((UIntType) obj, param);
    else if (obj instanceof SIntType)
      return visitSIntType((SIntType) obj, param);
    else
      throw new RuntimeException("Unknow object: " + obj.getClass().getSimpleName());
  }

  protected R visitNamedElemType(NamedElemType obj, P param) {
    if (obj instanceof StructType)
      return visitStructType((StructType) obj, param);
    else if (obj instanceof UnionType)
      return visitUnionType((UnionType) obj, param);
    else
      throw new RuntimeException("Unknow object: " + obj.getClass().getSimpleName());
  }

  protected abstract R visitRangeType(RangeType obj, P param);

  protected abstract R visitSIntType(SIntType obj, P param);

  protected abstract R visitUIntType(UIntType obj, P param);

  protected abstract R visitTypeRef(TypeRef obj, P param);

  protected abstract R visitTypeCast(TypeCast obj, P param);

  protected abstract R visitRefCall(RefCall obj, P param);

  protected abstract R visitRefIndex(RefIndex obj, P param);

  protected abstract R visitRefName(RefName obj, P param);

  protected abstract R visitReturnVoid(ReturnVoid obj, P param);

  protected abstract R visitReturnValue(ReturnExpr obj, P param);

  protected abstract R visitEnumElement(EnumElement obj, P param);

  protected abstract R visitCaseEntry(CaseEntry obj, P param);

  protected abstract R visitFuncVariable(FuncVariable obj, P param);

  protected abstract R visitConstant(Constant obj, P param);

  protected abstract R visitStateVariable(StateVariable obj, P param);

  protected abstract R visitNamedElement(NamedElement obj, P param);

  protected abstract R visitTypeAlias(TypeAlias obj, P param);

  protected abstract R visitVoidType(VoidType obj, P param);

  protected abstract R visitBoolValue(BoolValue obj, P param);

  protected abstract R visitArrayValue(ArrayValue obj, P param);

  protected abstract R visitStringValue(StringValue obj, P param);

  protected abstract R visitNumber(Number obj, P param);

  protected abstract R visitBinaryOp(BinaryOp obj, P param);

  protected abstract R visitUnaryOp(UnaryOp obj, P param);

  protected abstract R visitReference(Reference obj, P param);

  protected abstract R visitFunctionImpl(FunctionImpl obj, P param);

  protected abstract R visitFunctionPrototype(FunctionPrototype obj, P param);

  protected abstract R visitVarDefStmt(VarDefStmt obj, P param);

  protected abstract R visitCallStmt(CallStmt obj, P param);

  protected abstract R visitAssignment(Assignment obj, P param);

  protected abstract R visitCaseStmt(CaseStmt obj, P param);

  protected abstract R visitWhile(WhileStmt obj, P param);

  protected abstract R visitIf(IfStmt obj, P param);

  protected abstract R visitBlock(Block obj, P param);

  protected abstract R visitEnumType(EnumType obj, P param);

  protected abstract R visitStructType(StructType obj, P param);

  protected abstract R visitUnionType(UnionType obj, P param);

  protected abstract R visitPointerType(PointerType obj, P param);

  protected abstract R visitBooleanType(BooleanType obj, P param);

  protected abstract R visitStringType(StringType obj, P param);

  protected abstract R visitArrayType(ArrayType obj, P param);

  protected abstract R visitProgram(Program obj, P param);

}
