package evl.variable;

import common.ElementInfo;

import evl.type.TypeRef;

public class SsaVariable extends Variable {

  public SsaVariable(Variable name, int version) {
    super(name.getInfo(), name.getName() + "_" + version, new TypeRef(name.getType().getInfo(), name.getType().getRef()));
  }

  public SsaVariable(ElementInfo info, String name, TypeRef type) {
    super(info, name, type);
  }

}
