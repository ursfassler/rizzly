package pir.cfg;

import java.util.HashSet;
import java.util.Set;

import pir.expression.reference.VarRef;

/**
 * 
 * @author urs
 */
public class IfGoto extends BasicBlockEnd {
  private VarRef condition;
  private BasicBlock thenBlock;
  private BasicBlock elseBlock;

  public IfGoto(VarRef condition, BasicBlock thenBlock, BasicBlock elseBlock) {
    super();
    this.condition = condition;
    this.thenBlock = thenBlock;
    this.elseBlock = elseBlock;
  }

  public VarRef getCondition() {
    return condition;
  }

  public void setCondition(VarRef condition) {
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

  @Override
  public String toString() {
    return "if " + condition + " ? goto " + thenBlock.getName() + " : goto " + elseBlock.getName();
  }

}
