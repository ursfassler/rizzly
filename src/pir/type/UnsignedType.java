package pir.type;

public class UnsignedType extends IntType {

  public UnsignedType(int bits) {
    super(makeName(bits), bits);
  }

  public static String makeName(int bits) {
    return "U" + "{" + bits + "}";
  }

}
