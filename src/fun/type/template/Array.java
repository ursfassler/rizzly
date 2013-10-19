package fun.type.template;

import java.math.BigInteger;

import common.ElementInfo;

import fun.expression.reference.Reference;
import fun.other.ListOfNamed;
import fun.other.Named;
import fun.type.base.BaseType;
import fun.variable.TemplateParameter;

public class Array extends BaseType implements Named {
  private Reference type;
  private BigInteger size;

  public Array(ElementInfo info, BigInteger size, Reference type) {
    super(info, makeName(size, type));
    this.type = type;
    this.size = size;
  }

  private static String makeName(BigInteger size, Reference type) {
    return ArrayTemplate.NAME + "{" + size + "," + type.toString() + "}";
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
  public ListOfNamed<TemplateParameter> getParamList() {
    return new ListOfNamed<TemplateParameter>();
  }
}
