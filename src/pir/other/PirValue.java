package pir.other;

import pir.Pir;
import pir.type.TypeRef;

/**
 * Describs a value which is, once assigned, not changable. Like a number, constant or SSA value.
 * @author urs
 */
public interface PirValue extends Pir {

  public TypeRef getType();
}
