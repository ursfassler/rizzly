package pir.other;

import pir.Pir;
import pir.type.TypeRef;

public interface PirValue extends Pir {
  public TypeRef getType();
}
