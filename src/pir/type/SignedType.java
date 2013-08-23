package pir.type;

public class SignedType extends IntType {
  public SignedType(int bits) {
    super(makeName(bits), bits);
  }

  public static String makeName(int bits) {
    return "S" + "{" + bits + "}";
  }

}
