package cir.type;

public class UIntType extends IntType {

  public UIntType(int bytes) {
    super("U" + Integer.toString(8*bytes), bytes);
  }

}
