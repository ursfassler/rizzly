package pir.expression.reference;

import java.util.LinkedList;
import java.util.List;

import pir.expression.Expression;

final public class Reference extends Expression {
  private Referencable ref;
  final private LinkedList<RefItem> offset = new LinkedList<RefItem>();

  public Reference(Referencable ref, List<RefItem> offset) {
    super();
    this.ref = ref;
    this.offset.addAll(offset);
  }

  public Reference(Referencable ref) {
    super();
    this.ref = ref;
  }

  public Referencable getRef() {
    return ref;
  }

  public void setRef(Referencable ref) {
    this.ref = ref;
  }

  public LinkedList<RefItem> getOffset() {
    return offset;
  }

  @Override
  public String toString() {
    return ref.toString();
  }

}
