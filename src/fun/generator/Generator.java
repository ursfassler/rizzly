package fun.generator;

import java.util.Collection;

import common.ElementInfo;

import fun.FunBase;
import fun.expression.Expression;
import fun.other.ListOfNamed;
import fun.other.Named;
import fun.variable.TemplateParameter;

/**
 *
 * @author urs
 */
abstract public class Generator<T extends FunBase> extends Expression implements Named {
  private String name;
  final private ListOfNamed<TemplateParameter> param;
  final private T item;

  public Generator(ElementInfo info, String name, Collection<TemplateParameter> param, T item) {
    super(info);
    this.name = name;
    this.param = new ListOfNamed<TemplateParameter>(param);
    this.item = item;
  }

  public T getItem() {
    return item;
  }

  public ListOfNamed<TemplateParameter> getParam() {
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
