package evl.type.special;

import common.ElementInfo;

import evl.other.EvlList;
import evl.type.Type;
import evl.type.composed.NamedElement;

public class ComponentType extends Type {
  final private EvlList<NamedElement> input = new EvlList<NamedElement>();
  final private EvlList<NamedElement> output = new EvlList<NamedElement>();

  public ComponentType(ElementInfo info, String name) {
    super(info, name);
  }

  public EvlList<NamedElement> getInput() {
    return input;
  }

  public EvlList<NamedElement> getOutput() {
    return output;
  }

}
