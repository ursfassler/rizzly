package evl.copy;

import evl.Evl;
import evl.NullTraverser;
import evl.variable.ConstGlobal;
import evl.variable.ConstPrivate;
import evl.variable.FuncVariable;
import evl.variable.SsaVariable;
import evl.variable.StateVariable;
import evl.variable.Variable;

public class CopyVariable extends NullTraverser<Variable, Void> {
  private CopyEvl cast;

  public CopyVariable(CopyEvl cast) {
    super();
    this.cast = cast;
  }

  @Override
  protected Variable visitDefault(Evl obj, Void param) {
    throw new RuntimeException("not yet implemented: " + obj.getClass().getCanonicalName());
  }

  @Override
  protected Variable visitFuncVariable(FuncVariable obj, Void param) {
    return new FuncVariable(obj.getInfo(), obj.getName(), cast.copy(obj.getType()));
  }

  @Override
  protected Variable visitStateVariable(StateVariable obj, Void param) {
    return new StateVariable(obj.getInfo(), obj.getName(), cast.copy(obj.getType()));
  }

  @Override
  protected Variable visitConstPrivate(ConstPrivate obj, Void param) {
    return new ConstPrivate(obj.getInfo(), obj.getName(), cast.copy(obj.getType()),cast.copy(obj.getDef()));
  }

  @Override
  protected Variable visitConstGlobal(ConstGlobal obj, Void param) {
    return new ConstGlobal(obj.getInfo(), obj.getName(), cast.copy(obj.getType()),cast.copy(obj.getDef()));
  }

  @Override
  protected Variable visitSsaVariable(SsaVariable obj, Void param) {
    return new SsaVariable(obj.getInfo(), obj.getName(), cast.copy(obj.getType()));
  }

}
