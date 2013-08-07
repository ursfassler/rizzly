package evl.variable;

import common.ElementInfo;

import evl.type.Type;

public class SsaVariable extends Variable {

  public SsaVariable(Variable name, int version) {
    super(name.getInfo(), name.getName() + "_" + version, name.getType());
  }

  public SsaVariable(ElementInfo info, String name, Type type) {
    super(info, name, type);
  }


}
