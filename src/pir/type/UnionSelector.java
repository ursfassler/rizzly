package pir.type;

import pir.PirObject;
import pir.expression.reference.Referencable;

/**
 *
 * @author urs
 */
public class UnionSelector extends PirObject implements Referencable {

  private String name;

  public UnionSelector(String name) {
    this.name = name;
  }

  @Override
  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  @Override
  public String toString() {
    return name;
  }
}
