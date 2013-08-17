package pir.expression.reference;

import java.util.LinkedList;
import java.util.List;

import pir.expression.PExpression;
import pir.other.Variable;

final public class VarRef extends PExpression implements Reference<Variable> {
  private Variable ref;
  final private LinkedList<RefItem> offset = new LinkedList<RefItem>();

  public VarRef(Variable ref, List<RefItem> offset) {
    super();
    this.ref = ref;
    this.offset.addAll(offset);
  }

  public VarRef(Variable ref) {
    super();
    this.ref = ref;
  }

  @Override
  public Variable getRef() {
    return ref;
  }

  @Override
  public void setRef(Variable ref) {
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
