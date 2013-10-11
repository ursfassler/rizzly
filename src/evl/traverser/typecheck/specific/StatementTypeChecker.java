package evl.traverser.typecheck.specific;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import util.NumberSet;
import error.ErrorType;
import error.RError;
import evl.Evl;
import evl.NullTraverser;
import evl.cfg.BasicBlock;
import evl.cfg.BasicBlockList;
import evl.knowledge.KnowBaseItem;
import evl.knowledge.KnowledgeBase;
import evl.statement.Statement;
import evl.statement.bbend.CaseGoto;
import evl.statement.bbend.CaseOptRange;
import evl.statement.bbend.CaseOptValue;
import evl.statement.bbend.Goto;
import evl.statement.bbend.IfGoto;
import evl.statement.bbend.ReturnExpr;
import evl.statement.bbend.ReturnVoid;
import evl.statement.bbend.Unreachable;
import evl.statement.normal.Assignment;
import evl.statement.normal.CallStmt;
import evl.statement.normal.TypeCast;
import evl.statement.normal.VarDefInitStmt;
import evl.statement.normal.VarDefStmt;
import evl.statement.phi.PhiStmt;
import evl.traverser.typecheck.LeftIsContainerOfRightTest;
import evl.type.Type;
import evl.type.base.BooleanType;
import evl.type.base.NumSet;
import evl.variable.Constant;
import evl.variable.FuncVariable;
import evl.variable.SsaVariable;
import evl.variable.StateVariable;
import evl.variable.Variable;

//TODO check return type at a different place
//TODO before this step, replace case statements with a boolean condition with an if statement
public class StatementTypeChecker extends NullTraverser<Void, Map<SsaVariable, NumSet>> {
  private KnowledgeBase kb;
  private KnowBaseItem kbi;
  private Type funcReturn;
  private Map<BasicBlock, Map<SsaVariable, NumSet>> map = new HashMap<BasicBlock, Map<SsaVariable, NumSet>>();
  private Map<SsaVariable, NumSet> ranges;

  public StatementTypeChecker(KnowledgeBase kb, Type funcReturn, Map<SsaVariable, NumSet> ranges) {
    super();
    this.kb = kb;
    kbi = kb.getEntry(KnowBaseItem.class);
    this.funcReturn = funcReturn;
    this.ranges = ranges;
  }

  public static void process(BasicBlockList obj, Type funcReturn, Map<SsaVariable, NumSet> ranges, KnowledgeBase kb) {
    StatementTypeChecker adder = new StatementTypeChecker(kb, funcReturn, ranges);
    adder.traverse(obj, null);
  }

  public static void process(BasicBlockList obj, Type funcReturn, KnowledgeBase kb) {
    StatementTypeChecker adder = new StatementTypeChecker(kb, funcReturn, new HashMap<SsaVariable, NumSet>());
    adder.traverse(obj, null);
  }

  public static void process(Statement obj, Type funcReturn, KnowledgeBase kb) {
    StatementTypeChecker adder = new StatementTypeChecker(kb, funcReturn, new HashMap<SsaVariable, NumSet>());
    adder.traverse(obj, new HashMap<SsaVariable, NumSet>());
  }

  public static void process(Variable obj, KnowledgeBase kb) {
    StatementTypeChecker adder = new StatementTypeChecker(kb, null, new HashMap<SsaVariable, NumSet>());
    adder.traverse(obj, null);
  }

  private void visitList(List<? extends Evl> list, Map<SsaVariable, NumSet> param) {
    for (Evl itr : list) {
      visit(itr, param);
    }
  }

  @Override
  protected Void visitDefault(Evl obj, Map<SsaVariable, NumSet> param) {
    throw new RuntimeException("not yet implemented: " + obj.getClass().getCanonicalName());
  }

  @Override
  protected Void visitVarDef(VarDefStmt obj, Map<SsaVariable, NumSet> param) {
    visit(obj.getVariable(), param);

    return null;
  }

  @Override
  protected Void visitStateVariable(StateVariable obj, Map<SsaVariable, NumSet> param) {
    return null;
  }

  @Override
  protected Void visitConstant(Constant obj, Map<SsaVariable, NumSet> param) {
    Type ret = obj.getType().getRef();
    Type defType = ExpressionTypeChecker.process(obj.getDef(), kb);
    if (!LeftIsContainerOfRightTest.process(ret, defType, kb)) {
      RError.err(ErrorType.Error, obj.getInfo(), "Data type to big or incompatible in assignment: " + ret.getName() + " := " + defType.getName());
    }
    return null;
  }

  @Override
  protected Void visitFuncVariable(FuncVariable obj, Map<SsaVariable, NumSet> param) {
    return null;
  }

  @Override
  protected Void visitSsaVariable(SsaVariable obj, Map<SsaVariable, NumSet> param) {
    return null;
  }

  @Override
  protected Void visitGoto(Goto obj, Map<SsaVariable, NumSet> param) {
    return null;
  }

  @Override
  protected Void visitIfGoto(IfGoto obj, Map<SsaVariable, NumSet> param) {
    Type cond = ExpressionTypeChecker.process(obj.getCondition(), kb);
    if (!(cond instanceof BooleanType)) {
      RError.err(ErrorType.Error, obj.getInfo(), "Need boolean type, got: " + cond.getName());
    }
    return null;
  }

  @Override
  protected Void visitCaseGoto(CaseGoto obj, Map<SsaVariable, NumSet> param) {
    Type cond = ExpressionTypeChecker.process(obj.getCondition(), kb);
    // TODO enumerator and boolean should also be allowed
    if (!LeftIsContainerOfRightTest.process(kbi.getIntegerType(), cond, kb)) {
      RError.err(ErrorType.Error, obj.getInfo(), "Condition variable has to be an integer, got: " + cond.getName());
    }
    // TODO check somewhere if case values are disjunct
    return null;
  }

  @Override
  protected Void visitCaseOptRange(CaseOptRange obj, Map<SsaVariable, NumSet> param) {
    Type start = ExpressionTypeChecker.process(obj.getStart(), kb);
    Type end = ExpressionTypeChecker.process(obj.getEnd(), kb);
    if (!LeftIsContainerOfRightTest.process(kbi.getIntegerType(), start, kb)) {
      RError.err(ErrorType.Error, obj.getInfo(), "Case value has to be an integer (start), got: " + start.getName());
    }
    if (!LeftIsContainerOfRightTest.process(kbi.getIntegerType(), end, kb)) {
      RError.err(ErrorType.Error, obj.getInfo(), "Case value has to be an integer (end), got: " + end.getName());
    }
    return null;
  }

  @Override
  protected Void visitCaseOptValue(CaseOptValue obj, Map<SsaVariable, NumSet> param) {
    Type value = ExpressionTypeChecker.process(obj.getValue(), kb);
    if (!LeftIsContainerOfRightTest.process(kbi.getIntegerType(), value, kb)) {
      RError.err(ErrorType.Error, obj.getInfo(), "Case value has to be an integer, got: " + value.getName());
    }
    return null;
  }

  @Override
  protected Void visitCallStmt(CallStmt obj, Map<SsaVariable, NumSet> param) {
    ExpressionTypeChecker.process(obj.getCall(), kb);
    return null;
  }

  @Override
  protected Void visitUnreachable(Unreachable obj, Map<SsaVariable, NumSet> param) {
    return null;
  }

  @Override
  protected Void visitPhiStmt(PhiStmt obj, Map<SsaVariable, NumSet> param) {
    Type lhs = obj.getVariable().getType().getRef();
    for (BasicBlock in : obj.getInBB()) {
      Type rhs = ExpressionTypeChecker.process(obj.getArg(in), kb); // TODO use map to get smaller range
      if (!LeftIsContainerOfRightTest.process(lhs, rhs, kb)) {
        RError.err(ErrorType.Error, obj.getInfo(), "Data type to big or incompatible in assignment: " + lhs.getName() + " := " + rhs.getName());
      }
    }
    return null;
  }

  @Override
  protected Void visitVarDefInitStmt(VarDefInitStmt obj, Map<SsaVariable, NumSet> param) {
    assert (param != null);
    Type lhs = obj.getVariable().getType().getRef();
    Type rhs = ExpressionTypeChecker.process(obj.getInit(), param, kb);
    if (!LeftIsContainerOfRightTest.process(lhs, rhs, kb)) {
      RError.err(ErrorType.Error, obj.getInfo(), "Data type to big or incompatible in assignment: " + lhs.getName() + " := " + rhs.getName());
    }
    return null;
  }

  @Override
  protected Void visitAssignment(Assignment obj, Map<SsaVariable, NumSet> param) {
    Type lhs = RefTypeChecker.process(obj.getLeft(), kb);
    Type rhs = ExpressionTypeChecker.process(obj.getRight(), param, kb);
    if (!LeftIsContainerOfRightTest.process(lhs, rhs, kb)) {
      RError.err(ErrorType.Error, obj.getInfo(), "Data type to big or incompatible in assignment: " + lhs.getName() + " := " + rhs.getName());
    }
    return null;
  }

  @Override
  protected Void visitBasicBlockList(BasicBlockList obj, Map<SsaVariable, NumSet> param) {
    for (BasicBlock bb : obj.getAllBbs()) {
      map.put(bb, new HashMap<SsaVariable, NumSet>());
    }
    visitItr(obj.getAllBbs(), null);
    return null;
  }

  @Override
  protected Void visitBasicBlock(BasicBlock obj, Map<SsaVariable, NumSet> param) {
    assert (param == null);
    param = narrowAll(map.get(obj), ranges);
    visitList(obj.getPhi(), param);
    visitList(obj.getCode(), param);
    visit(obj.getEnd(), param);
    return null;
  }

  private Map<SsaVariable, NumSet> narrowAll(Map<SsaVariable, NumSet> a, Map<SsaVariable, NumSet> b) {
    Map<SsaVariable, NumSet> ret = new HashMap<SsaVariable, NumSet>();
    ret.putAll(a);
    ret.putAll(b);
    Set<SsaVariable> vars = new HashSet<SsaVariable>(a.keySet());
    vars.retainAll(b.keySet());
    for (SsaVariable var : vars) {
      //TODO really needed here? not done in range narrower?
      NumberSet rs = NumberSet.intersection(a.get(var).getNumbers(), b.get(var).getNumbers());
      NumSet rt = kbi.getNumsetType(rs.getRanges());
      ret.put(var, rt);
    }
    return ret;
  }

  @Override
  protected Void visitReturnExpr(ReturnExpr obj, Map<SsaVariable, NumSet> param) {
    Type ret = ExpressionTypeChecker.process(obj.getExpr(), kb);
    if (!LeftIsContainerOfRightTest.process(funcReturn, ret, kb)) {
      RError.err(ErrorType.Error, obj.getInfo(), "Data type to big or incompatible to return: " + funcReturn.getName() + " := " + ret.getName());
    }
    return null;
  }

  @Override
  protected Void visitReturnVoid(ReturnVoid obj, Map<SsaVariable, NumSet> param) {
    return null;
  }

  @Override
  protected Void visitTypeCast(TypeCast obj, Map<SsaVariable, NumSet> param) {
    // we trust the cast
    //TODO can we trust the cast?
    return null;
  }

}
