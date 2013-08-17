package evl.function.impl;

import common.ElementInfo;

import evl.cfg.BasicBlockList;
import evl.function.FuncWithBody;
import evl.function.FunctionBase;
import evl.other.ListOfNamed;
import evl.variable.FuncVariable;

/**
 *
 * @author urs
 */
public class FuncSubHandlerEvent extends FunctionBase implements FuncWithBody {

  private BasicBlockList body = null;

  public FuncSubHandlerEvent(ElementInfo info, String name, ListOfNamed<FuncVariable> param) {
    super(info, name, param);
  }

  @Override
  public BasicBlockList getBody() {
    assert (body != null);
    return body;
  }

  @Override
  public void setBody(BasicBlockList body) {
    assert (body != null);
    this.body = body;
  }

}