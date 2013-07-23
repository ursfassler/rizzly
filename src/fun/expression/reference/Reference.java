package fun.expression.reference;

import java.util.LinkedList;

import common.ElementInfo;

import fun.expression.Expression;

abstract public class Reference extends Expression {
  final private LinkedList<RefItem> offset = new LinkedList<RefItem>();

  public Reference(ElementInfo info) {
    super(info);
  }

  public LinkedList<RefItem> getOffset() {
    return offset;
  }

  @Override
  public String toString() {
    String ret = "";
    for (RefItem item : offset) {
      ret += item.toString();
    }
    return ret;
  }

}
