package cir.type;

public class SIntType extends IntType {

  public SIntType(String name, int bytes) {
    super(name, bytes);
  }

  static public String makeName(int bytes) {
    return "S" + Integer.toString(8 * bytes);
  }

}
