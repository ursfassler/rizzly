package evl.variable;

import common.ElementInfo;

import evl.expression.Expression;
import evl.type.TypeRef;

abstract public class Constant extends DefVariable {

  public Constant(ElementInfo info, String name, TypeRef type, Expression def) {
    super(info, name, type, def);
  }

}
