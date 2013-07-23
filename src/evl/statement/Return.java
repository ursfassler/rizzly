package evl.statement;

import common.ElementInfo;

/**
 * 
 * @author urs
 */
abstract public class Return extends Statement {

  public Return(ElementInfo info) {
    super(info);
  }

  @Override
  public String toString() {
    return "return";
  }
}
