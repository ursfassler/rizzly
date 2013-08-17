package pir.function;

import java.util.List;

import pir.other.FuncVariable;
import pir.type.Type;

final public class FuncProto extends Function {
  public FuncProto(String name, List<FuncVariable> argument, Type retType) {
    super(name, argument, retType);
  }
}
