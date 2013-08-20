package pir.function;

import java.util.List;

import pir.other.SsaVariable;
import pir.type.Type;

final public class FuncProto extends Function {
  public FuncProto(String name, List<SsaVariable> argument, Type retType) {
    super(name, argument, retType);
  }
}
