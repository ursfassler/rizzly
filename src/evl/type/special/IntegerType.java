package evl.type.special;

import common.ElementInfo;

import evl.type.base.BaseType;


/**
 * Use this type only for type checking and not for code production.
 *
 * @author urs
 *
 */
final public class IntegerType extends BaseType {
  public final static String NAME = "Integer";

  public IntegerType() {
    super(new ElementInfo(), NAME);
  }

}
