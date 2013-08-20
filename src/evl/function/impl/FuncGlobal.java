package evl.function.impl;

import common.ElementInfo;

import evl.cfg.BasicBlockList;
import evl.expression.reference.Reference;
import evl.function.FuncWithBody;
import evl.function.FuncWithReturn;
import evl.function.FunctionBase;
import evl.other.ListOfNamed;
import evl.variable.Variable;

/**
 *
 * @author urs
 */
public class FuncGlobal extends FunctionBase implements FuncWithReturn, FuncWithBody {
  private Reference ret = null;
  private BasicBlockList body = null;

  public FuncGlobal(ElementInfo info, String name, ListOfNamed<Variable> param) {
    super(info, name, param);
  }

  @Override
  public Reference getRet() {
    assert (ret != null);
    return ret;
  }

  @Override
  public void setRet(Reference ret) {
    assert (ret != null);
    this.ret = ret;
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
