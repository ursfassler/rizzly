package evl.function.impl;

import common.ElementInfo;

import evl.function.FuncWithBody;
import evl.function.FunctionBase;
import evl.other.ListOfNamed;
import evl.statement.Block;
import evl.variable.FuncVariable;

/**
 *
 * @author urs
 */
public class FuncPrivateVoid extends FunctionBase implements FuncWithBody {
  private Block body = null;

  public FuncPrivateVoid(ElementInfo info, String name, ListOfNamed<FuncVariable> param) {
    super(info, name, param);
  }

  @Override
  public Block getBody() {
    assert (body != null);
    return body;
  }

  @Override
  public void setBody(Block body) {
    assert (body != null);
    this.body = body;
  }

}
