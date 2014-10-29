package evl.type.special;

import common.ElementInfo;

import evl.type.base.BaseType;

/**
 * Use this type only for type checking and not for code production.
 * 
 * @author urs
 * 
 */
final public class NaturalType extends BaseType {
  public final static String NAME = "Natural";

  public NaturalType() {
    super(ElementInfo.NO, NAME);
  }

}
