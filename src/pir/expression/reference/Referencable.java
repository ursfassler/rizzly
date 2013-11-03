package pir.expression.reference;

import pir.Pir;

public interface Referencable extends Pir {
  public String getName();

  public void setName(String name);
}
