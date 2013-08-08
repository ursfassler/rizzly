package pir.cfg;

import java.util.HashSet;
import java.util.Set;

import pir.expression.PExpression;

/**
 *
 * @author urs
 */
public class IfGoto extends BasicBlockEnd {
  private PExpression condition;
  private BasicBlock thenBlock;
  private BasicBlock elseBlock;

  public IfGoto(PExpression condition, BasicBlock thenBlock, BasicBlock elseBlock) {
    super();
    this.condition = condition;
    this.thenBlock = thenBlock;
    this.elseBlock = elseBlock;
  }

  public PExpression getCondition() {
    return condition;
  }

  public void setCondition(PExpression condition) {
    this.condition = condition;
  }

  public BasicBlock getThenBlock() {
    return thenBlock;
  }

  public void setThenBlock(BasicBlock thenBlock) {
    this.thenBlock = thenBlock;
  }

  public BasicBlock getElseBlock() {
    return elseBlock;
  }

  public void setElseBlock(BasicBlock elseBlock) {
    this.elseBlock = elseBlock;
  }

  @Override
  public Set<BasicBlock> getJumpDst() {
    Set<BasicBlock> ret = new HashSet<BasicBlock>();
    ret.add(thenBlock);
    ret.add(elseBlock);
    return ret;
  }

}
