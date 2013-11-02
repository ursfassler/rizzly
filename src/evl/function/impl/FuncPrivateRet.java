package evl.function.impl;

import common.ElementInfo;

import evl.function.FuncWithBody;
import evl.function.FuncWithReturn;
import evl.function.FunctionBase;
import evl.other.ListOfNamed;
import evl.statement.Block;
import evl.type.TypeRef;
import evl.variable.FuncVariable;

/**
 *
 * @author urs
 */
public class FuncPrivateRet extends FunctionBase implements FuncWithReturn, FuncWithBody {
  private TypeRef ret = null;
  private Block body = null;

  public FuncPrivateRet(ElementInfo info, String name, ListOfNamed<FuncVariable> param) {
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
