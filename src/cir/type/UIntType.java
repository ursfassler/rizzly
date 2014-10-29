package cir.type;

public class UIntType extends IntType {

  public UIntType(String name, int bytes) {
    super(name, bytes);
  }

  static public String makeName(int bytes) {
    return "U" + Integer.toString(8 * bytes);
  }

}
