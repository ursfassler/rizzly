package pir;

import java.util.Collection;

import pir.expression.ArithmeticOp;
import pir.expression.ArrayValue;
import pir.expression.BoolValue;
import pir.expression.Number;
import pir.expression.PExpression;
import pir.expression.Reference;
import pir.expression.Relation;
import pir.expression.StringValue;
import pir.expression.UnaryExpr;
import pir.expression.reference.RefCall;
import pir.expression.reference.RefHead;
import pir.expression.reference.RefIndex;
import pir.expression.reference.RefItem;
import pir.expression.reference.RefMiddle;
import pir.expression.reference.RefName;
import pir.function.Function;
import pir.function.impl.FuncImplRet;
import pir.function.impl.FuncImplVoid;
import pir.function.impl.FuncProtoRet;
import pir.function.impl.FuncProtoVoid;
import pir.other.Constant;
import pir.other.FuncVariable;
import pir.other.Program;
import pir.other.StateVariable;
import pir.other.Variable;
import pir.statement.Assignment;
import pir.statement.Block;
import pir.statement.CallStmt;
import pir.statement.CaseEntry;
import pir.statement.CaseOptEntry;
import pir.statement.CaseOptRange;
import pir.statement.CaseOptValue;
import pir.statement.CaseStmt;
import pir.statement.IfStmt;
import pir.statement.IfStmtEntry;
import pir.statement.Return;
import pir.statement.ReturnValue;
import pir.statement.ReturnVoid;
import pir.statement.Statement;
import pir.statement.VarDefStmt;
import pir.statement.WhileStmt;
import pir.type.Array;
import pir.type.BooleanType;
import pir.type.EnumElement;
import pir.type.EnumType;
import pir.type.NamedElemType;
import pir.type.NamedElement;
import pir.type.StringType;
import pir.type.StructType;
import pir.type.Type;
import pir.type.TypeAlias;
import pir.type.UnionType;
import pir.type.UnsignedType;
import pir.type.VoidType;

public abstract class Traverser<R, P> {
  public R traverse(PirObject obj, P param) {
    return visit(obj, param);
  }

  protected void visitList(Collection<? extends PirObject> list, P param) {
    for (PirObject itr : list) {
      visit(itr, param);
    }
  }

  protected R visit(PirObject obj, P param) {
    if (obj == null)
      throw new RuntimeException("object is null");
    else if (obj instanceof Program)
      return visitProgram((Program) obj, param);
    else if (obj instanceof Statement)
      return visitStatement((Statement) obj, param);
    else if (obj instanceof PExpression)
      return visitPExpression((PExpression) obj, param);
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
    else if (obj instanceof CaseOptEntry)
      return visitOptEntry((CaseOptEntry) obj, param);
    else if (obj instanceof CaseEntry)
      return visitCaseEntry((CaseEntry) obj, param);
    else if (obj instanceof IfStmtEntry)
      return visitIfStmtEntry((IfStmtEntry) obj, param);
    else
      throw new RuntimeException("Unknow object: " + obj.getClass().getSimpleName());
  }

  private R visitOptEntry(CaseOptEntry obj, P param) {
    if (obj instanceof CaseOptValue)
      return visitCaseOptValue((CaseOptValue) obj, param);
    else if (obj instanceof CaseOptRange)
      return visitCaseOptRange((CaseOptRange) obj, param);
    else
      throw new RuntimeException("Unknow object: " + obj.getClass().getSimpleName());
  }

  protected R visitFunction(Function obj, P param) {
    if (obj instanceof FuncProtoRet)
      return visitFuncProtoRet((FuncProtoRet) obj, param);
    else if (obj instanceof FuncProtoVoid)
      return visitFuncProtoVoid((FuncProtoVoid) obj, param);
    else if (obj instanceof FuncImplRet)
      return visitFuncImplRet((FuncImplRet) obj, param);
    else if (obj instanceof FuncImplVoid)
      return visitFuncImplVoid((FuncImplVoid) obj, param);
    else
      throw new RuntimeException("Unknow object: " + obj.getClass().getSimpleName());
  }

  protected R visitRefItem(RefItem obj, P param) {
    if (obj instanceof RefMiddle)
      return visitRefMiddle((RefMiddle) obj, param);
    else if (obj instanceof RefHead)
      return visitRefHead((RefHead) obj, param);
    else
      throw new RuntimeException("Unknow object: " + obj.getClass().getSimpleName());
  }

  protected R visitRefMiddle(RefMiddle obj, P param) {
    if (obj instanceof RefName)
      return visitRefName((RefName) obj, param);
    else if (obj instanceof RefCall)
      return visitRefCall((RefCall) obj, param);
    else if (obj instanceof RefIndex)
      return visitRefIndex((RefIndex) obj, param);
    else
      throw new RuntimeException("Unknow object: " + obj.getClass().getSimpleName());
  }

  protected R visitPExpression(PExpression obj, P param) {
    if (obj instanceof Reference)
      return visitReference((Reference) obj, param);
    else if (obj instanceof ArithmeticOp)
      return visitArithmeticOp((ArithmeticOp) obj, param);
    else if (obj instanceof UnaryExpr)
      return visitUnaryExpr((UnaryExpr) obj, param);
    else if (obj instanceof Relation)
      return visitRelation((Relation) obj, param);
    else if (obj instanceof Number)
      return visitNumber((Number) obj, param);
    else if (obj instanceof StringValue)
      return visitStringValue((StringValue) obj, param);
    else if (obj instanceof ArrayValue)
      return visitArrayValue((ArrayValue) obj, param);
    else if (obj instanceof BoolValue)
      return visitBoolValue((BoolValue) obj, param);
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
      return visitIfStmt((IfStmt) obj, param);
    else if (obj instanceof WhileStmt)
      return visitWhile((WhileStmt) obj, param);
    else if (obj instanceof CaseStmt)
      return visitCaseStmt((CaseStmt) obj, param);
    else
      throw new RuntimeException("Unknow object: " + obj.getClass().getSimpleName());
  }

  protected R visitVariable(Variable obj, P param) {
    if (obj instanceof FuncVariable)
      return visitFuncVariable((FuncVariable) obj, param);
    else if (obj instanceof StateVariable)
      return visitStateVariable((StateVariable) obj, param);
    else if (obj instanceof Constant)
      return visitConstant((Constant) obj, param);
    else
      throw new RuntimeException("Unknow object: " + obj.getClass().getSimpleName());
  }

  protected R visitReturn(Return obj, P param) {
    if (obj instanceof ReturnValue)
      return visitReturnValue((ReturnValue) obj, param);
    else if (obj instanceof ReturnVoid)
      return visitReturnVoid((ReturnVoid) obj, param);
    else
      throw new RuntimeException("Unknow object: " + obj.getClass().getSimpleName());
  }

  protected R visitType(Type obj, P param) {
    if (obj instanceof UnsignedType)
      return visitUnsignedType((UnsignedType) obj, param);
    else if (obj instanceof BooleanType)
      return visitBooleanType((BooleanType) obj, param);
    else if (obj instanceof NamedElemType)
      return visitNamedElemType((NamedElemType) obj, param);
    else if (obj instanceof EnumType)
      return visitEnumType((EnumType) obj, param);
    else if (obj instanceof VoidType)
      return visitVoidType((VoidType) obj, param);
    else if (obj instanceof TypeAlias)
      return visitTypeAlias((TypeAlias) obj, param);
    else if (obj instanceof Array)
      return visitArray((Array) obj, param);
    else if (obj instanceof StringType)
      return visitStringType((StringType) obj, param);
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

  protected abstract R visitRefHead(RefHead obj, P param);

  protected abstract R visitRefCall(RefCall obj, P param);

  protected abstract R visitRefIndex(RefIndex obj, P param);

  protected abstract R visitRefName(RefName obj, P param);

  protected abstract R visitReturnVoid(ReturnVoid obj, P param);

  protected abstract R visitReturnValue(ReturnValue obj, P param);

  protected abstract R visitEnumElement(EnumElement obj, P param);

  protected abstract R visitIfStmtEntry(IfStmtEntry obj, P param);

  protected abstract R visitCaseEntry(CaseEntry obj, P param);

  protected abstract R visitCaseOptValue(CaseOptValue obj, P param);

  protected abstract R visitCaseOptRange(CaseOptRange obj, P param);

  protected abstract R visitConstant(Constant obj, P param);

  protected abstract R visitStateVariable(StateVariable obj, P param);

  protected abstract R visitFuncVariable(FuncVariable obj, P param);

  protected abstract R visitNamedElement(NamedElement obj, P param);

  protected abstract R visitTypeAlias(TypeAlias obj, P param);

  protected abstract R visitVoidType(VoidType obj, P param);

  protected abstract R visitBoolValue(BoolValue obj, P param);

  protected abstract R visitNumber(Number obj, P param);

  protected abstract R visitArrayValue(ArrayValue obj, P param);

  protected abstract R visitStringValue(StringValue obj, P param);

  protected abstract R visitRelation(Relation obj, P param);

  protected abstract R visitArithmeticOp(ArithmeticOp obj, P param);

  protected abstract R visitUnaryExpr(UnaryExpr obj, P param);

  protected abstract R visitReference(Reference obj, P param);

  protected abstract R visitFuncImplVoid(FuncImplVoid obj, P param);

  protected abstract R visitFuncImplRet(FuncImplRet obj, P param);

  protected abstract R visitFuncProtoVoid(FuncProtoVoid obj, P param);

  protected abstract R visitFuncProtoRet(FuncProtoRet obj, P param);

  protected abstract R visitVarDefStmt(VarDefStmt obj, P param);

  protected abstract R visitCallStmt(CallStmt obj, P param);

  protected abstract R visitAssignment(Assignment obj, P param);

  protected abstract R visitCaseStmt(CaseStmt obj, P param);

  protected abstract R visitWhile(WhileStmt obj, P param);

  protected abstract R visitIfStmt(IfStmt obj, P param);

  protected abstract R visitBlock(Block obj, P param);

  protected abstract R visitEnumType(EnumType obj, P param);

  protected abstract R visitStructType(StructType obj, P param);

  protected abstract R visitUnionType(UnionType obj, P param);

  protected abstract R visitBooleanType(BooleanType obj, P param);

  protected abstract R visitStringType(StringType obj, P param);

  protected abstract R visitArray(Array obj, P param);

  protected abstract R visitUnsignedType(UnsignedType obj, P param);

  protected abstract R visitProgram(Program obj, P param);

}
