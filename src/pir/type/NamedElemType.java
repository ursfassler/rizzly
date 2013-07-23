package pir.type;

import java.util.ArrayList;
import java.util.List;

abstract public class NamedElemType extends Type {
  final private List<NamedElement> elements = new ArrayList<NamedElement>();

  public NamedElemType(String name) {
    super(name);
  }

  public List<NamedElement> getElements() {
    return elements;
  }

  public NamedElement find(String name) {
    for (NamedElement elem : elements) {
      if (elem.getName().equals(name)) {
        return elem;
      }
    }
    return null;
  }

}
