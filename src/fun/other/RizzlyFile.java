package fun.other;

import java.util.List;

import common.Designator;
import common.ElementInfo;

import fun.FunBase;
import fun.function.FunctionHeader;
import fun.generator.Generator;
import fun.variable.Constant;

/**
 *
 * @author urs
 */
final public class RizzlyFile extends FunBase {
  private Designator name;
  final private List<Designator> imports;
  @SuppressWarnings("rawtypes")
  final private ListOfNamed<Generator> compfunc = new ListOfNamed<Generator>();
  final private ListOfNamed<Constant> constant = new ListOfNamed<Constant>();
  final private ListOfNamed<FunctionHeader> function = new ListOfNamed<FunctionHeader>();

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

  public ListOfNamed<FunctionHeader> getFunction() {
    return function;
  }

  @SuppressWarnings("rawtypes")
  public ListOfNamed<Generator> getCompfunc() {
    return compfunc;
  }

}
