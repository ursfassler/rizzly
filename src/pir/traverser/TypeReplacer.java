package pir.traverser;

import java.util.HashSet;

import pir.Traverser;
import pir.cfg.BasicBlock;
import pir.cfg.BasicBlockList;
import pir.cfg.CaseGoto;
import pir.cfg.Goto;
import pir.cfg.IfGoto;
import pir.cfg.PhiStmt;
import pir.cfg.ReturnExpr;
import pir.cfg.ReturnVoid;
import pir.expression.ArrayValue;
import pir.expression.BoolValue;
import pir.expression.Number;
import pir.expression.StringValue;
import pir.expression.UnaryExpr;
import pir.expression.reference.CallExpr;
import pir.expression.reference.RefIndex;
import pir.expression.reference.RefName;
import pir.expression.reference.VarRef;
import pir.function.FuncImpl;
import pir.function.FuncProto;
import pir.function.FuncWithBody;
import pir.function.Function;
import pir.other.Constant;
import pir.other.FuncVariable;
import pir.other.Program;
import pir.other.SsaVariable;
import pir.other.StateVariable;
import pir.other.Variable;
import pir.statement.ArithmeticOp;
import pir.statement.Assignment;
import pir.statement.CallStmt;
import pir.statement.Relation;
import pir.statement.StoreStmt;
import pir.statement.VarDefStmt;
import pir.statement.VariableGeneratorStmt;
import pir.type.EnumElement;
import pir.type.EnumType;
import pir.type.NamedElement;
import pir.type.StringType;
import pir.type.Type;

abstract public class TypeReplacer<T> extends Traverser<Type, T> {

  @Override
  protected Type visitVariable(Variable obj, T param) {
    obj.setType(visit(obj.getType(), param));
    return null;
  }

  @Override
  protected Type visitFunction(Function obj, T param) {
    obj.setRetType(visit(obj.getRetType(), param));
    visitList(obj.getArgument(), param);
    if (obj instanceof FuncWithBody) {
      visit(((FuncWithBody) obj).getBody(), param);
    }
    return null;
  }

  @Override
  protected Type visitFuncImpl(FuncImpl obj, T param) {
    throw new RuntimeException("not yet implemented");
  }

  @Override
  protected Type visitFuncProto(FuncProto obj, T param) {
    throw new RuntimeException("not yet implemented");
  }

  @Override
  protected Type visitStringType(StringType obj, T param) {
    throw new RuntimeException("not yet implemented");
  }

  @Override
  protected Type visitReturnVoid(ReturnVoid obj, T param) {
    return null;
  }

  @Override
  protected Type visitRefIndex(RefIndex obj, T param) {
    throw new RuntimeException("not yet implemented");
  }

  @Override
  protected Type visitRefName(RefName obj, T param) {
    throw new RuntimeException("not yet implemented");
  }

  @Override
  protected Type visitReturnExpr(ReturnExpr obj, T param) {
    return null;
  }

  @Override
  protected Type visitEnumElement(EnumElement obj, T param) {
    obj.setType((EnumType) visit(obj.getType(), param));
    return null;
  }

  @Override
  protected Type visitConstant(Constant obj, T param) {
    throw new RuntimeException("not yet implemented");
  }

  @Override
  protected Type visitStateVariable(StateVariable obj, T param) {
    throw new RuntimeException("not yet implemented");
  }

  @Override
  protected Type visitFuncVariable(FuncVariable obj, T param) {
    throw new RuntimeException("not yet implemented");
  }

  @Override
  protected Type visitNamedElement(NamedElement obj, T param) {
    obj.setType(visit(obj.getType(), param));
    return null;
  }

  @Override
  protected Type visitBoolValue(BoolValue obj, T param) {
    throw new RuntimeException("not yet implemented");
  }

  @Override
  protected Type visitStringValue(StringValue obj, T param) {
    throw new RuntimeException("not yet implemented");
  }

  @Override
  protected Type visitArrayValue(ArrayValue obj, T param) {
    throw new RuntimeException("not yet implemented");
  }

  @Override
  protected Type visitNumber(Number obj, T param) {
    return null;
  }

  @Override
  protected Type visitVariableGeneratorStmt(VariableGeneratorStmt obj, T param) {
    visit(obj.getVariable(),param);
    super.visitVariableGeneratorStmt(obj, param);
    return null;
  }

  @Override
  protected Type visitArithmeticOp(ArithmeticOp obj, T param) {
    visit(obj.getLeft(), param);
    visit(obj.getRight(), param);
    return null;
  }

  @Override
  protected Type visitUnaryExpr(UnaryExpr obj, T param) {
    throw new RuntimeException("not yet implemented");
  }

  @Override
  protected Type visitRelation(Relation obj, T param) {
    visit(obj.getLeft(), param);
    visit(obj.getRight(), param);
    return null;
  }

  @Override
  protected Type visitVarDefStmt(VarDefStmt obj, T param) {
    visit(obj.getVariable(), param);
    return null;
  }

  @Override
  protected Type visitCallStmt(CallStmt obj, T param) {
    return null;
  }

  @Override
  protected Type visitAssignment(Assignment obj, T param) {
    return null;
  }

  @Override
  protected Type visitProgram(Program obj, T param) {
    visitList(obj.getConstant(), param);
    visitList(obj.getFunction(), param);
    HashSet<Type> ns = new HashSet<Type>();
    for (Type type : obj.getType()) {
      Type nt = visit(type, param);
      assert (nt != null);
      ns.add(nt);
    }
    obj.getType().clear();
    obj.getType().addAll(ns);
    visitList(obj.getVariable(), param);
    return null;
  }

  @Override
  protected Type visitPhiStmt(PhiStmt obj, T param) {
    visit(obj.getVariable(), param);
    return null;
  }

  @Override
  protected Type visitGoto(Goto obj, T param) {
    return null;
  }

  @Override
  protected Type visitSsaVariable(SsaVariable obj, T param) {
    throw new RuntimeException("not yet implemented");
  }

  @Override
  protected Type visitBasicBlock(BasicBlock obj, T param) {
    visitList(obj.getPhi(), param);
    visitList(obj.getCode(), param);
    visit(obj.getEnd(), param);
    return null;
  }

  @Override
  protected Type visitBasicBlockList(BasicBlockList obj, T param) {
    visitList(obj.getBasicBlocks(), param);
    return null;
  }

  @Override
  protected Type visitCaseGoto(CaseGoto obj, T param) {
    throw new RuntimeException("not yet implemented");
  }

  @Override
  protected Type visitIfGoto(IfGoto obj, T param) {
    visit(obj.getCondition(), param);
    return null;
  }

  @Override
  protected Type visitStoreStmt(StoreStmt obj, T param) {
    visit(obj.getSrc(), param);
    visit(obj.getDst(), param);
    return null;
  }

  @Override
  protected Type visitCallExpr(CallExpr obj, T param) {
    return null;
  }

  @Override
  protected Type visitVarRef(VarRef obj, T param) {
    return null;
  }

}
