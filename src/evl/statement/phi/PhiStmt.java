package evl.statement.phi;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import common.ElementInfo;

import evl.cfg.BasicBlock;
import evl.expression.Expression;
import evl.statement.normal.SsaGenerator;
import evl.statement.Statement;
import evl.variable.SsaVariable;

/**
 *
 * @author urs
 */
public class PhiStmt extends Statement implements SsaGenerator {

  private SsaVariable vardef;
  private Map<BasicBlock, Expression> arg = new HashMap<BasicBlock, Expression>();

  public PhiStmt(ElementInfo info, SsaVariable vardef) {
    super(info);
    this.vardef = vardef;
  }

  @Override
  public SsaVariable getVariable() {
    return vardef;
  }

  public void addArg(BasicBlock bb, Expression var) {
    arg.put(bb, var);
  }

  public Expression getArg(BasicBlock bb) {
    return arg.get(bb);
  }

  public Set<BasicBlock> getInBB() {
    return new HashSet<BasicBlock>(arg.keySet());
  }
}
