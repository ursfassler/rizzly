package evl.function;

import java.util.HashSet;
import java.util.Set;

import common.ElementInfo;
import common.FuncAttr;

import evl.EvlBase;
import evl.other.ListOfNamed;
import evl.variable.FuncVariable;

/**
 * 
 * @author urs
 */
abstract public class FunctionBase extends EvlBase implements FunctionHeader {
  private String name;
  private ListOfNamed<FuncVariable> param = new ListOfNamed<FuncVariable>();
  private Set<FuncAttr> attributes = new HashSet<FuncAttr>();

  public FunctionBase(ElementInfo info, String name, ListOfNamed<FuncVariable> param) {
    super(info);
    assert (name != null);
    this.name = name;
    this.param = param;
  }

  public void setAttribute(FuncAttr attr) {
    attributes.add(attr);
  }

  public Set<FuncAttr> getAttributes() {
    return attributes;
  }

  @Override
  public ListOfNamed<FuncVariable> getParam() {
    return param;
  }

  @Override
  public String getName() {
    return name;
  }

  @Override
  public void setName(String name) {
    this.name = name;
  }

  @Override
  public String toString() {
    return name + param;
  }

}
