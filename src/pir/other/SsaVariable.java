package pir.other;

import pir.type.Type;

/**
 * Variable of scalar type and exactly one write point
 * @author urs
 *
 */
final public class SsaVariable extends Variable {

  public SsaVariable(String name, Type type) {
    super(name, type);
  }

}
