package fun.type.genfunc;

import common.ElementInfo;

import fun.other.ListOfNamed;
import fun.other.Named;
import fun.type.base.BaseType;
import fun.variable.CompfuncParameter;

final public class Unsigned extends BaseType implements Named {
  final private int bits;
  private String name;

  public Unsigned(ElementInfo info, int bits) {
    super(info);
    this.bits = bits;
    name = "U_" + makeBitString(bits);
  }

  private static String makeBitString(int bits) {
    if (bits >= 0) {
      return Integer.toString(bits);
    } else if (bits == -1) {
      return "*";
    } else {
      throw new RuntimeException("Wrong number of bits: " + bits);
    }
  }

  public int getBits() {
    return bits;
  }

  @Override
  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  @Override
  public ListOfNamed<CompfuncParameter> getParamList() {
    return new ListOfNamed<CompfuncParameter>();
  }
}
