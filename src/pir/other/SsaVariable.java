package pir.other;

import pir.type.TypeRef;

/**
 * Variable of scalar type and exactly one write point
 * @author urs
 *
 */
final public class SsaVariable extends Variable {

  public SsaVariable(String name, TypeRef type) {
    super(name, type);
  }

}
