package fun.type.base;

import java.math.BigInteger;

import common.ElementInfo;

import fun.expression.Number;
import fun.expression.reference.ReferenceLinked;
import fun.variable.Constant;

final public class EnumElement extends Constant {
  public EnumElement(ElementInfo info, String name, EnumType type, BigInteger value) {
    super(info, name, new ReferenceLinked(info, type));
    setDef(new Number(info, value));
  }

}
