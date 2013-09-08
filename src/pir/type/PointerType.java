package pir.type;

/**
 *
 * @author urs
 */
public class PointerType extends Type {

  private TypeRef type;

  public PointerType(TypeRef type) {
    super(makeName(type));
    this.type = type;
  }

  public static String makeName(TypeRef type) {
    return "Pointer{" + type.getRef().getName() + "}";
  }

  public TypeRef getType() {
    return type;
  }

  public void setType(TypeRef type) {
    this.type = type;
  }
}
