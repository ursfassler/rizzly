package evl.copy;

import java.util.Collection;

import evl.Evl;
import evl.NullTraverser;
import evl.function.FuncWithBody;
import evl.function.FuncWithReturn;
import evl.function.FunctionBase;
import evl.function.FunctionFactory;
import evl.other.ListOfNamed;
import evl.variable.FuncVariable;

public class CopyFunction extends NullTraverser<FunctionBase, Void> {
  private CopyEvl cast;

  public CopyFunction(CopyEvl cast) {
    super();
    this.cast = cast;
  }

  @Override
  protected FunctionBase visitDefault(Evl obj, Void param) {
    throw new RuntimeException("not yet implemented: " + obj.getClass().getCanonicalName());
  }

  @Override
  protected FunctionBase visitFunctionBase(FunctionBase obj, Void param) {
    Collection<FuncVariable> arg = cast.copy(obj.getParam().getList());
    FunctionBase ret = FunctionFactory.create(obj.getClass(), obj.getInfo(), obj.getName(), new ListOfNamed<FuncVariable>(arg));
    cast.getCopied().put(obj, ret);
    if (obj instanceof FuncWithReturn) {
      ((FuncWithReturn) ret).setRet(cast.copy(((FuncWithReturn) obj).getRet()));
    }
    if (obj instanceof FuncWithBody) {
      ((FuncWithBody) ret).setBody(cast.copy(((FuncWithBody) obj).getBody()));
    }
    ret.getAttributes().addAll(obj.getAttributes());
    return ret;
  }

}
