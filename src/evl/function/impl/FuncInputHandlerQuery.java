package evl.function.impl;

import common.ElementInfo;

import evl.cfg.BasicBlockList;
import evl.function.FuncWithBody;
import evl.function.FuncWithReturn;
import evl.function.FunctionBase;
import evl.other.ListOfNamed;
import evl.type.TypeRef;
import evl.variable.Variable;

/**
 * 
 * @author urs
 */
public class FuncInputHandlerQuery extends FunctionBase implements FuncWithReturn, FuncWithBody {

  private TypeRef ret = null;
  private BasicBlockList body = null;

  public FuncInputHandlerQuery(ElementInfo info, String name, ListOfNamed<Variable> param) {
    super(info, name, param);
  }

  @Override
  public TypeRef getRet() {
    assert (ret != null);
    return ret;
  }

  @Override
  public void setRet(TypeRef ret) {
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
