package fun.other;

import java.util.List;

import common.Designator;
import common.ElementInfo;

import fun.Fun;
import fun.FunBase;

/**
 * 
 * @author urs
 */
final public class RizzlyFile extends FunBase implements Named {
  private String name;
  final private List<Designator> imports;
  final private FunList<Fun> objects = new FunList<Fun>();

  public RizzlyFile(ElementInfo info, String name, List<Designator> imports) {
    super(info);
    this.name = name;
    this.imports = imports;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public List<Designator> getImports() {
    return imports;
  }

  public FunList<Fun> getObjects() {
    return objects;
  }

  @Override
  public String toString() {
    return name;
  }

}
