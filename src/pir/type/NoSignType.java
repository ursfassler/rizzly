package pir.type;

/**
 * Used to generate llvm
 * 
 * @author urs
 * 
 */
public class NoSignType extends IntType {
  public NoSignType(int bits) {
    super(makeName(bits), bits);
  }

  public static String makeName(int bits) {
    return "N" + "{" + bits + "}";
  }

}
