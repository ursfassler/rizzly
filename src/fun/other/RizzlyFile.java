package fun.other;

import java.util.List;

import common.Designator;
import common.ElementInfo;

import fun.FunBase;
import fun.function.impl.FuncGlobal;
import fun.type.Type;
import fun.variable.Constant;

/**
 *
 * @author urs
 */
final public class RizzlyFile extends FunBase {
  private Designator name;
  final private List<Designator> imports;
  final private ListOfNamed<Generator> compfunc = new ListOfNamed<Generator>();
  final private ListOfNamed<Type> type = new ListOfNamed<Type>();
  final private ListOfNamed<Interface> iface = new ListOfNamed<Interface>();
  final private ListOfNamed<Component> comp = new ListOfNamed<Component>();
  final private ListOfNamed<Constant> constant = new ListOfNamed<Constant>();
  final private ListOfNamed<FuncGlobal> function = new ListOfNamed<FuncGlobal>();

  public RizzlyFile(ElementInfo info, List<Designator> imports) {
    super(info);
    this.imports = imports;
  }

  @Override
  public String toString() {
    return name.toString();
  }

  public Designator getName() {
    return name;
  }

  public void setName(Designator name) {
    this.name = name;
  }

  public List<Designator> getImports() {
    return imports;
  }

  public ListOfNamed<Constant> getConstant() {
    return constant;
  }

  public ListOfNamed<FuncGlobal> getFunction() {
    return function;
  }

  public ListOfNamed<Generator> getCompfunc() {
    return compfunc;
  }

  public ListOfNamed<Type> getType() {
    return type;
  }

  public ListOfNamed<Interface> getIface() {
    return iface;
  }

  public ListOfNamed<Component> getComp() {
    return comp;
  }

}
