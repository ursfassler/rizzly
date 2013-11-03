package cir.expression.reference;

import cir.Cir;

public interface Referencable extends Cir {
  public String getName();

  public void setName(String name);
}
