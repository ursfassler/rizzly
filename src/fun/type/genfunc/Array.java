package fun.type.genfunc;

import java.math.BigInteger;

import common.Designator;
import common.ElementInfo;

import fun.expression.reference.Reference;
import fun.other.ListOfNamed;
import fun.other.Named;
import fun.type.base.BaseType;
import fun.variable.CompfuncParameter;

public class Array extends BaseType implements Named {
  private Reference type;
  private BigInteger size;
  private String name;

  public Array(ElementInfo info, BigInteger size, Reference type) {
    super(info);
    this.type = type;
    this.size = size;
    name = GenericArray.NAME + Designator.NAME_SEP + size + Designator.NAME_SEP + type.toString();
  }

  public Reference getType() {
    return type;
  }

  public void setType(Reference type) {
    this.type = type;
  }

  public void setSize(BigInteger size) {
    this.size = size;
  }

  public BigInteger getSize() {
    return size;
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
