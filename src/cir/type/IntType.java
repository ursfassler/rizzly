package cir.type;

public class IntType extends Type {
  private final boolean signed;
  private final int bytes;

  public IntType(boolean signed, int bytes) {
    super(makeName(signed,bytes));
    this.signed = signed;
    this.bytes = bytes;
  }
  
  static private String makeName( boolean signed, int bytes ){
    return signed?"":"u" + "int" + bytes*8 + "_t";
  }

  public boolean isSigned() {
    return signed;
  }

  public int getBytes() {
    return bytes;
  }

}
