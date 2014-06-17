package evl.expression;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import common.ElementInfo;

import evl.type.TypeRef;

public class RecordValue extends Expression {
  final private List<NamedElementValue> value = new ArrayList<NamedElementValue>();
  private TypeRef type;

  public RecordValue(ElementInfo info, Collection<NamedElementValue> value, TypeRef type) {
    super(info);
    this.value.addAll(value);
    this.type = type;
  }

  public List<NamedElementValue> getValue() {
    return value;
  }

  public TypeRef getType() {
    return type;
  }

  public void setType(TypeRef type) {
    this.type = type;
  }

  @Override
  public String toString() {
    return value + ": " + type;
  }

}
