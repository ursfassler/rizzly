package pir.traverser;

import java.util.List;

import pir.Traverser;
import pir.expression.PExpression;
import pir.expression.reference.RefCall;
import pir.expression.reference.RefHead;
import pir.expression.reference.RefIndex;
import pir.expression.reference.RefName;
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
import pir.type.RangeType;
import pir.type.StringType;
import pir.type.StructType;
import pir.type.TypeAlias;
import pir.type.UnionType;
import pir.type.UnsignedType;
import pir.type.VoidType;

abstract public class ExprReplacer<T> extends Traverser<PExpression, T> {

  protected void visitExprList(List<PExpression> parameter, T param) {
    for (int i = 0; i < parameter.size(); i++) {
      parameter.set(i, visit(parameter.get(i), param));
    }
  }

  @Override
  protected PExpression visitRefHead(RefHead obj, T param) {
    return null;
  }

  @Override
  protected PExpression visitRefCall(RefCall obj, T param) {
    visit(obj.getPrevious(), param);
    visitExprList(obj.getParameter(), param);
    return null;
  }

  @Override
  protected PExpression visitRefIndex(RefIndex obj, T param) {
    visit(obj.getPrevious(), param);
    obj.setIndex(visit(obj.getIndex(), param));
    return null;
  }

  @Override
  protected PExpression visitRefName(RefName obj, T param) {
    visit(obj.getPrevious(), param);
    return null;
  }

  @Override
  protected PExpression visitReturnVoid(ReturnVoid obj, T param) {
    return null;
  }

  @Override
  protected PExpression visitReturnValue(ReturnValue obj, T param) {
    obj.setValue(visit(obj.getValue(), param));
    return null;
  }

  @Override
  protected PExpression visitEnumElement(EnumElement obj, T param) {
    throw new RuntimeException("not yet implemented");
  }

  @Override
  protected PExpression visitIfStmtEntry(IfStmtEntry obj, T param) {
    obj.setCondition(visit(obj.getCondition(), param));
    visit(obj.getCode(), param);
    return null;
  }

  @Override
  protected PExpression visitCaseEntry(CaseEntry obj, T param) {
    visitList(obj.getValues(), param);
    visit(obj.getCode(), param);
    return null;
  }

  @Override
  protected PExpression visitCaseOptValue(CaseOptValue obj, T param) {
    obj.setValue(visit(obj.getValue(), param));
    return null;
  }

  @Override
  protected PExpression visitCaseOptRange(CaseOptRange obj, T param) {
    obj.setStart(visit(obj.getStart(), param));
    obj.setEnd(visit(obj.getEnd(), param));
    return null;
  }

  @Override
  protected PExpression visitConstant(Constant obj, T param) {
    obj.setDef(visit(obj.getDef(), param));
    return null;
  }

  @Override
  protected PExpression visitStateVariable(StateVariable obj, T param) {
    return null;
  }

  @Override
  protected PExpression visitFuncVariable(FuncVariable obj, T param) {
    return null;
  }

  @Override
  protected PExpression visitNamedElement(NamedElement obj, T param) {
    throw new RuntimeException("not yet implemented");
  }

  @Override
  protected PExpression visitTypeAlias(TypeAlias obj, T param) {
    return null;
  }

  @Override
  protected PExpression visitVoidType(VoidType obj, T param) {
    return null;
  }

  @Override
  protected PExpression visitVarDefStmt(VarDefStmt obj, T param) {
    return null;
  }

  @Override
  protected PExpression visitCallStmt(CallStmt obj, T param) {
    visit(obj.getRef(), param);
    return null;
  }

  @Override
  protected PExpression visitAssignment(Assignment obj, T param) {
    visit(obj.getDst(), param);
    obj.setSrc(visit(obj.getSrc(), param));
    return null;
  }

  @Override
  protected PExpression visitCaseStmt(CaseStmt obj, T param) {
    obj.setCondition(visit(obj.getCondition(), param));
    visitList(obj.getEntries(), param);
    visit(obj.getOtherwise(), param);
    return null;
  }

  @Override
  protected PExpression visitWhile(WhileStmt obj, T param) {
    obj.setCond(visit(obj.getCond(), param));
    visit(obj.getBlock(), param);
    return null;
  }

  @Override
  protected PExpression visitIfStmt(IfStmt obj, T param) {
    visitList(obj.getOption(), param);
    visit(obj.getDef(), param);
    return null;
  }

  @Override
  protected PExpression visitBlock(Block obj, T param) {
    visitList(obj.getStatement(), param);
    return null;
  }

  @Override
  protected PExpression visitEnumType(EnumType obj, T param) {
    return null;
  }

  @Override
  protected PExpression visitStructType(StructType obj, T param) {
    return null;
  }

  @Override
  protected PExpression visitUnionType(UnionType obj, T param) {
    return null;
  }

  @Override
  protected PExpression visitBooleanType(BooleanType obj, T param) {
    return null;
  }

  @Override
  protected PExpression visitArray(Array obj, T param) {
    return null;
  }

  @Override
  protected PExpression visitStringType(StringType obj, T param) {
    return null;
  }

  @Override
  protected PExpression visitRangeType(RangeType obj, T param) {
    return null;
  }

  @Override
  protected PExpression visitUnsignedType(UnsignedType obj, T param) {
    return null;
  }

  @Override
  protected PExpression visitProgram(Program obj, T param) {
    visitList(obj.getConstant(), param);
    visitList(obj.getFunction(), param);
    visitList(obj.getType(), param);
    visitList(obj.getVariable(), param);
    return null;
  }

  @Override
  protected PExpression visitFunction(Function obj, T param) {
    visitList(obj.getArgument(), param);
    if (obj instanceof FuncWithBody) {
      visit(((FuncWithBody) obj).getBody(), param);
    }
    return null;
  }

  @Override
  protected PExpression visitFuncImpl(FuncImpl obj, T param) {
    throw new RuntimeException("not yet implemented");
  }

  @Override
  protected PExpression visitFuncImplRet(FuncImplRet obj, T param) {
    throw new RuntimeException("not yet implemented");
  }

  @Override
  protected PExpression visitFuncProto(FuncProto obj, T param) {
    throw new RuntimeException("not yet implemented");
  }

  @Override
  protected PExpression visitFuncProtoRet(FuncProtoRet obj, T param) {
    throw new RuntimeException("not yet implemented");
  }

}
