package pir;

import pir.cfg.BasicBlock;
import pir.cfg.BasicBlockEnd;
import pir.cfg.BasicBlockList;
import pir.cfg.CaseGoto;
import pir.cfg.CaseGotoOpt;
import pir.cfg.CaseOptEntry;
import pir.cfg.CaseOptRange;
import pir.cfg.CaseOptValue;
import pir.cfg.Goto;
import pir.cfg.IfGoto;
import pir.cfg.PhiStmt;
import pir.cfg.Return;
import pir.cfg.ReturnExpr;
import pir.cfg.ReturnVoid;
import pir.expression.ArrayValue;
import pir.expression.BoolValue;
import pir.expression.Number;
import pir.expression.PExpression;
import pir.expression.StringValue;
import pir.expression.reference.RefIndex;
import pir.expression.reference.RefItem;
import pir.expression.reference.RefName;
import pir.expression.reference.VarRef;
import pir.expression.reference.VarRefSimple;
import pir.function.FuncImpl;
import pir.function.FuncProto;
import pir.function.Function;
import pir.other.Constant;
import pir.other.FuncVariable;
import pir.other.Program;
import pir.other.SsaVariable;
import pir.other.StateVariable;
import pir.other.Variable;
import pir.statement.ArithmeticOp;
import pir.statement.Assignment;
import pir.statement.CallAssignment;
import pir.statement.CallStmt;
import pir.statement.ComplexWriter;
import pir.statement.GetElementPtr;
import pir.statement.LoadStmt;
import pir.statement.Relation;
import pir.statement.Statement;
import pir.statement.StoreStmt;
import pir.statement.UnaryOp;
import pir.statement.VarDefStmt;
import pir.statement.VariableGeneratorStmt;
import pir.statement.convert.ConvertValue;
import pir.statement.convert.SignExtendValue;
import pir.statement.convert.TruncValue;
import pir.statement.convert.TypeCast;
import pir.statement.convert.ZeroExtendValue;
import pir.type.ArrayType;
import pir.type.BooleanType;
import pir.type.EnumElement;
import pir.type.EnumType;
import pir.type.IntType;
import pir.type.NamedElemType;
import pir.type.NamedElement;
import pir.type.NoSignType;
import pir.type.RangeType;
import pir.type.SignedType;
import pir.type.StringType;
import pir.type.StructType;
import pir.type.Type;
import pir.type.TypeAlias;
import pir.type.TypeRef;
import pir.type.UnionType;
import pir.type.UnsignedType;
import pir.type.VoidType;

abstract public class Traverser<R, P> {
  public R traverse(Pir obj, P param) {
    return visit(obj, param);
  }

  protected void visitList(Iterable<? extends Pir> list, P param) {
    for (Pir itr : list) {
      visit(itr, param);
    }
  }

  protected R visit(Pir obj, P param) {
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
    else if (obj instanceof BasicBlockList)
      return visitBasicBlockList((BasicBlockList) obj, param);
    else if (obj instanceof BasicBlock)
      return visitBasicBlock((BasicBlock) obj, param);
    else if (obj instanceof TypeRef)
      return visitTypeRef((TypeRef) obj, param);
    else if (obj instanceof CaseGotoOpt)
      return visitCaseGotoOpt((CaseGotoOpt) obj, param);
    else if (obj instanceof CaseOptEntry)
      return visitCaseOptEntry((CaseOptEntry) obj, param);
    else
      throw new RuntimeException("Unknow object: " + obj.getClass().getSimpleName());
  }

  protected R visitCaseOptEntry(CaseOptEntry obj, P param) {
    if (obj instanceof CaseOptRange)
      return visitCaseOptRange((CaseOptRange) obj, param);
    else if (obj instanceof CaseOptValue)
      return visitCaseOptValue((CaseOptValue) obj, param);
    else
      throw new RuntimeException("Unknow object: " + obj.getClass().getSimpleName());
  }

  protected R visitBasicBlockEnd(BasicBlockEnd obj, P param) {
    if (obj instanceof Return)
      return visitReturn((Return) obj, param);
    else if (obj instanceof IfGoto)
      return visitIfGoto((IfGoto) obj, param);
    else if (obj instanceof CaseGoto)
      return visitCaseGoto((CaseGoto) obj, param);
    else if (obj instanceof Goto)
      return visitGoto((Goto) obj, param);
    else
      throw new RuntimeException("Unknow object: " + obj.getClass().getSimpleName());
  }

  protected R visitFunction(Function obj, P param) {
    if (obj instanceof FuncProto)
      return visitFuncProto((FuncProto) obj, param);
    else if (obj instanceof FuncImpl)
      return visitFuncImpl((FuncImpl) obj, param);
    else
      throw new RuntimeException("Unknow object: " + obj.getClass().getSimpleName());
  }

  protected R visitRefItem(RefItem obj, P param) {
    if (obj instanceof RefName)
      return visitRefName((RefName) obj, param);
    else if (obj instanceof RefIndex)
      return visitRefIndex((RefIndex) obj, param);
    else
      throw new RuntimeException("Unknow object: " + obj.getClass().getSimpleName());
  }

  protected R visitPExpression(PExpression obj, P param) {
    if (obj instanceof Number)
      return visitNumber((Number) obj, param);
    else if (obj instanceof StringValue)
      return visitStringValue((StringValue) obj, param);
    else if (obj instanceof ArrayValue)
      return visitArrayValue((ArrayValue) obj, param);
    else if (obj instanceof BoolValue)
      return visitBoolValue((BoolValue) obj, param);
    else if (obj instanceof VarRef)
      return visitVarRef((VarRef) obj, param);
    else if (obj instanceof VarRefSimple)
      return visitVarRefSimple((VarRefSimple) obj, param);
    else
      throw new RuntimeException("Unknow object: " + obj.getClass().getSimpleName());
  }

  protected R visitStatement(Statement obj, P param) {
    if (obj instanceof CallStmt)
      return visitCallStmt((CallStmt) obj, param);
    else if (obj instanceof VarDefStmt)
      return visitVarDefStmt((VarDefStmt) obj, param);
    else if (obj instanceof VariableGeneratorStmt)
      return visitVariableGeneratorStmt((VariableGeneratorStmt) obj, param);
    else if (obj instanceof StoreStmt)
      return visitStoreStmt((StoreStmt) obj, param);
    else if (obj instanceof ComplexWriter)
      return visitComplexWriter((ComplexWriter) obj, param);
    else if (obj instanceof BasicBlockEnd)
      return visitBasicBlockEnd((BasicBlockEnd) obj, param);
    else
      throw new RuntimeException("Unknow object: " + obj.getClass().getSimpleName());
  }

  protected R visitVariableGeneratorStmt(VariableGeneratorStmt obj, P param) {
    if (obj instanceof Assignment)
      return visitAssignment((Assignment) obj, param);
    else if (obj instanceof ArithmeticOp)
      return visitArithmeticOp((ArithmeticOp) obj, param);
    else if (obj instanceof Relation)
      return visitRelation((Relation) obj, param);
    else if (obj instanceof LoadStmt)
      return visitLoadStmt((LoadStmt) obj, param);
    else if (obj instanceof CallAssignment)
      return visitCallAssignment((CallAssignment) obj, param);
    else if (obj instanceof GetElementPtr)
      return visitGetElementPtr((GetElementPtr) obj, param);
    else if (obj instanceof ConvertValue)
      return visitConvertValue((ConvertValue) obj, param);
    else if (obj instanceof PhiStmt)
      return visitPhiStmt((PhiStmt) obj, param);
    else if (obj instanceof UnaryOp)
      return visitUnaryOp((UnaryOp) obj, param);
    else
      throw new RuntimeException("Unknow object: " + obj.getClass().getSimpleName());
  }

  protected R visitConvertValue(ConvertValue obj, P param) {
    if (obj instanceof SignExtendValue)
      return visitSignExtendValue((SignExtendValue) obj, param);
    if (obj instanceof ZeroExtendValue)
      return visitZeroExtendValue((ZeroExtendValue) obj, param);
    else if (obj instanceof TruncValue)
      return visitTruncValue((TruncValue) obj, param);
    else if (obj instanceof TypeCast)
      return visitTypeCast((TypeCast) obj, param);
    else
      throw new RuntimeException("Unknow object: " + obj.getClass().getSimpleName());
  }

  protected R visitVariable(Variable obj, P param) {
    if (obj instanceof FuncVariable)
      return visitFuncVariable((FuncVariable) obj, param);
    else if (obj instanceof SsaVariable)
      return visitSsaVariable((SsaVariable) obj, param);
    else if (obj instanceof StateVariable)
      return visitStateVariable((StateVariable) obj, param);
    else if (obj instanceof Constant)
      return visitConstant((Constant) obj, param);
    else
      throw new RuntimeException("Unknow object: " + obj.getClass().getSimpleName());
  }

  protected R visitReturn(Return obj, P param) {
    if (obj instanceof ReturnExpr)
      return visitReturnExpr((ReturnExpr) obj, param);
    else if (obj instanceof ReturnVoid)
      return visitReturnVoid((ReturnVoid) obj, param);
    else
      throw new RuntimeException("Unknow object: " + obj.getClass().getSimpleName());
  }

  protected R visitType(Type obj, P param) {
    if (obj instanceof RangeType)
      return visitRangeType((RangeType) obj, param);
    else if (obj instanceof IntType)
      return visitIntType((IntType) obj, param);
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
    else if (obj instanceof ArrayType)
      return visitArray((ArrayType) obj, param);
    else if (obj instanceof StringType)
      return visitStringType((StringType) obj, param);
    else
      throw new RuntimeException("Unknow object: " + obj.getClass().getSimpleName());
  }

  protected R visitIntType(IntType obj, P param) {
    if (obj instanceof UnsignedType)
      return visitUnsignedType((UnsignedType) obj, param);
    else if (obj instanceof SignedType)
      return visitSignedType((SignedType) obj, param);
    else if (obj instanceof NoSignType)
      return visitNoSignType((NoSignType) obj, param);
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

  protected abstract R visitCaseOptValue(CaseOptValue obj, P param);

  protected abstract R visitCaseOptRange(CaseOptRange obj, P param);

  protected abstract R visitCaseGotoOpt(CaseGotoOpt obj, P param);

  protected abstract R visitTypeRef(TypeRef obj, P param);

  protected abstract R visitTypeCast(TypeCast obj, P param);

  protected abstract R visitTruncValue(TruncValue obj, P param);

  protected abstract R visitSignExtendValue(SignExtendValue obj, P param);

  protected abstract R visitZeroExtendValue(ZeroExtendValue obj, P param);

  protected abstract R visitNoSignType(NoSignType obj, P param);

  protected abstract R visitSignedType(SignedType obj, P param);

  protected abstract R visitUnaryOp(UnaryOp obj, P param);

  protected abstract R visitPhiStmt(PhiStmt obj, P param);

  protected abstract R visitGoto(Goto obj, P param);

  protected abstract R visitSsaVariable(SsaVariable obj, P param);

  protected abstract R visitBasicBlock(BasicBlock obj, P param);

  protected abstract R visitBasicBlockList(BasicBlockList obj, P param);

  protected abstract R visitCaseGoto(CaseGoto obj, P param);

  protected abstract R visitRefIndex(RefIndex obj, P param);

  protected abstract R visitRefName(RefName obj, P param);

  protected abstract R visitReturnVoid(ReturnVoid obj, P param);

  protected abstract R visitReturnExpr(ReturnExpr obj, P param);

  protected abstract R visitEnumElement(EnumElement obj, P param);

  protected abstract R visitIfGoto(IfGoto obj, P param);

  protected abstract R visitConstant(Constant obj, P param);

  protected abstract R visitStateVariable(StateVariable obj, P param);

  protected abstract R visitFuncVariable(FuncVariable obj, P param);

  protected abstract R visitNamedElement(NamedElement obj, P param);

  protected abstract R visitTypeAlias(TypeAlias obj, P param);

  protected abstract R visitVoidType(VoidType obj, P param);

  protected abstract R visitVarRefSimple(VarRefSimple obj, P param);

  protected abstract R visitVarRef(VarRef obj, P param);

  protected abstract R visitBoolValue(BoolValue obj, P param);

  protected abstract R visitNumber(Number obj, P param);

  protected abstract R visitArrayValue(ArrayValue obj, P param);

  protected abstract R visitStringValue(StringValue obj, P param);

  protected abstract R visitRelation(Relation obj, P param);

  protected abstract R visitCallAssignment(CallAssignment obj, P param);

  protected abstract R visitGetElementPtr(GetElementPtr obj, P param);

  protected abstract R visitLoadStmt(LoadStmt obj, P param);

  protected abstract R visitArithmeticOp(ArithmeticOp obj, P param);

  protected abstract R visitFuncImpl(FuncImpl obj, P param);

  protected abstract R visitFuncProto(FuncProto obj, P param);

  protected abstract R visitComplexWriter(ComplexWriter obj, P param);

  protected abstract R visitStoreStmt(StoreStmt obj, P param);

  protected abstract R visitVarDefStmt(VarDefStmt obj, P param);

  protected abstract R visitCallStmt(CallStmt obj, P param);

  protected abstract R visitAssignment(Assignment obj, P param);

  protected abstract R visitEnumType(EnumType obj, P param);

  protected abstract R visitStructType(StructType obj, P param);

  protected abstract R visitUnionType(UnionType obj, P param);

  protected abstract R visitBooleanType(BooleanType obj, P param);

  protected abstract R visitStringType(StringType obj, P param);

  protected abstract R visitArray(ArrayType obj, P param);

  protected abstract R visitUnsignedType(UnsignedType obj, P param);

  protected abstract R visitRangeType(RangeType obj, P param);

  protected abstract R visitProgram(Program obj, P param);

}
