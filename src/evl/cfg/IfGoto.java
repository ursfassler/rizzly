package evl.cfg;

import java.util.HashSet;
import java.util.Set;

import common.ElementInfo;

import evl.EvlBase;
import evl.expression.Expression;

/**
 *
 * @author urs
 */
public class IfGoto extends EvlBase implements BasicBlockEnd {
  private Expression condition;
  private BasicBlock thenBlock;
  private BasicBlock elseBlock;

  public IfGoto(ElementInfo info) {
    super(info);
  }

  public Expression getCondition() {
    return condition;
  }

  public void setCondition(Expression condition) {
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
