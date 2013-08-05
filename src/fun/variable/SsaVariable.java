package fun.variable;

public class SsaVariable extends Variable {

  public SsaVariable(Variable name, int version) {
    super(name.getInfo(), name.getName() + "_" + version, name.getType());
  }

}
