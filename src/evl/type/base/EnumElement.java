package evl.type.base;

import java.math.BigInteger;

import common.ElementInfo;

import evl.expression.Number;
import evl.type.TypeRef;
import evl.variable.Constant;

final public class EnumElement extends Constant {
  public EnumElement(ElementInfo info, String name, TypeRef type, BigInteger value) {
    super(info, name, type, new Number(info, value));
  }
}
