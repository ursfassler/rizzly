package fun.other;

import java.util.EnumMap;

import common.Direction;
import common.ElementInfo;

import fun.FunBase;
import fun.function.FunctionHeader;
import fun.variable.TemplateParameter;

abstract public class Component extends FunBase implements Generator, Named {
  final private ListOfNamed<TemplateParameter> param = new ListOfNamed<TemplateParameter>();
  final private EnumMap<Direction, ListOfNamed<FunctionHeader>> iface;
  private String name;

  public Component(ElementInfo info, String name) {
    super(info);
    this.name = name;
    iface = new EnumMap<Direction, ListOfNamed<FunctionHeader>>(Direction.class);
    iface.put(Direction.in, new ListOfNamed<FunctionHeader>());
    iface.put(Direction.out, new ListOfNamed<FunctionHeader>());
  }

  public ListOfNamed<FunctionHeader> getIface(Direction dir) {
    return iface.get(dir);
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  @Override
  public ListOfNamed<TemplateParameter> getTemplateParam() {
    return param;
  }
}
