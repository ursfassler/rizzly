package evl.other;

import common.ElementInfo;

import evl.EvlBase;
import evl.expression.reference.SimpleRef;
import evl.function.Function;

public class SubCallbacks extends EvlBase {
  final private EvlList<Function> func = new EvlList<Function>();
  final private SimpleRef<CompUse> compUse;

  public SubCallbacks(ElementInfo info, SimpleRef<CompUse> compUse) {
    super(info);
    this.compUse = compUse;
  }

  public EvlList<Function> getFunc() {
    return func;
  }

  public SimpleRef<CompUse> getCompUse() {
    return compUse;
  }

}
