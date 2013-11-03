package pir.type;

import java.util.List;

public class FunctionType extends Type {
  final private List<TypeRef> arg;
  final private TypeRef ret;

  public FunctionType(List<TypeRef> arg, TypeRef ret) {
    super(makeName(arg, ret));
    this.arg = arg;
    this.ret = ret;
  }

  private static String makeName(List<TypeRef> arg, TypeRef ret) {
    String name = "Function" + arg + ret;
    return name;
  }

  public List<TypeRef> getArg() {
    return arg;
  }

  public TypeRef getRet() {
    return ret;
  }

}
