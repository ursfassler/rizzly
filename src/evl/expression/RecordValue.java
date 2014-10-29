package evl.expression;

import java.util.Collection;

import common.ElementInfo;

import evl.expression.reference.SimpleRef;
import evl.other.EvlList;
import evl.type.Type;

public class RecordValue extends Expression {
  final private EvlList<NamedElementValue> value = new EvlList<NamedElementValue>();
  private SimpleRef<Type> type;

  public RecordValue(ElementInfo info, Collection<NamedElementValue> value, SimpleRef<Type> type) {
    super(info);
    this.value.addAll(value);
    this.type = type;
  }

  public EvlList<NamedElementValue> getValue() {
    return value;
  }

  public SimpleRef<Type> getType() {
    return type;
  }

  public void setType(SimpleRef<Type> type) {
    this.type = type;
  }

  @Override
  public String toString() {
    return value + ": " + type;
  }

}
