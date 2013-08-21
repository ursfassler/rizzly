package pir.function;

import java.util.List;

import pir.other.SsaVariable;
import pir.type.TypeRef;

final public class FuncProto extends Function {
  public FuncProto(String name, List<SsaVariable> argument, TypeRef retType) {
    super(name, argument, retType);
  }
}
