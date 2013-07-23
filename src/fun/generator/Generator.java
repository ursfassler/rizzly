package fun.generator;

import java.util.Collection;

import common.ElementInfo;

import fun.FunBase;
import fun.expression.Expression;
import fun.other.ListOfNamed;
import fun.other.Named;
import fun.variable.CompfuncParameter;

/**
 *
 * @author urs
 */
abstract public class Generator<T extends FunBase> extends Expression implements Named {
  private String name;
  final private ListOfNamed<CompfuncParameter> param;
  final private T item;

  public Generator(ElementInfo info, String name, Collection<CompfuncParameter> param, T item) {
    super(info);
    this.name = name;
    this.param = new ListOfNamed<CompfuncParameter>(param);
    this.item = item;
  }

  public T getItem() {
    return item;
  }

  public ListOfNamed<CompfuncParameter> getParam() {
    return param;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  @Override
  public String toString() {
    return name.toString() + param;
  }

}
