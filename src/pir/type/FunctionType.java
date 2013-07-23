package pir.type;

import java.util.List;

public class FunctionType extends Type {
  final private List<Type> arg;
  final private Type ret;

  public FunctionType(List<Type> arg, Type ret) {
    super(makeName(arg,ret));
    this.arg = arg;
    this.ret = ret;
  }

  private static String makeName(List<Type> arg, Type ret) {
    String name = "Function" + arg + ret;
    return name;
  }

  public List<Type> getArg() {
    return arg;
  }

  public Type getRet() {
    return ret;
  }

}
