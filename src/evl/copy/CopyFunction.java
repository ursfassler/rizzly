package evl.copy;

import evl.Evl;
import evl.NullTraverser;
import evl.function.Function;
import evl.function.FunctionFactory;
import evl.other.EvlList;
import evl.variable.FuncVariable;

public class CopyFunction extends NullTraverser<Evl, Void> {
  private CopyEvl cast;

  public CopyFunction(CopyEvl cast) {
    super();
    this.cast = cast;
  }

  @Override
  protected Function visitDefault(Evl obj, Void param) {
    throw new RuntimeException("not yet implemented: " + obj.getClass().getCanonicalName());
  }

  @Override
  protected Function visitFunctionImpl(Function obj, Void param) {
    EvlList<FuncVariable> arg = cast.copy(obj.getParam());
    Function ret = FunctionFactory.create(obj.getClass(), obj.getInfo(), obj.getName(), arg, cast.copy(obj.getRet()), cast.copy(obj.getBody()));
    cast.getCopied().put(obj, ret);
    return ret;
  }

}
