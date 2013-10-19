package fun.type.base;

import java.util.List;

import common.ElementInfo;

import fun.expression.reference.Reference;
import fun.type.Type;

public class FunctionType extends Type {
  private List<Reference> arg;
  final private Reference ret;

  public FunctionType(ElementInfo info, String name, List<Reference> arg, Reference ret) {
    super(info,name);
    this.ret = ret;
    this.arg = arg;
  }

  public Reference getRet() {
    return ret;
  }

  public List<Reference> getArg() {
    return arg;
  }

  @Override
  public String toString() {
    return "Function:" + arg + ":" + ret;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((arg == null) ? 0 : arg.hashCode());
    result = prime * result + ((ret == null) ? 0 : ret.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    FunctionType other = (FunctionType) obj;
    if (arg == null) {
      if (other.arg != null)
        return false;
    } else if (!arg.equals(other.arg))
      return false;
    if (ret == null) {
      if (other.ret != null)
        return false;
    } else if (!ret.equals(other.ret))
      return false;
    return true;
  }

}
