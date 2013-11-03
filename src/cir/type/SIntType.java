package cir.type;

public class SIntType extends IntType {

  public SIntType(int bytes) {
    super("S" + Integer.toString(8 * bytes), bytes);
  }

}
