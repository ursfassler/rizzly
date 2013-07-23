package pir.type;

import java.util.ArrayList;
import java.util.List;

public class EnumType extends Type {
  final private List<EnumElement> elements = new ArrayList<EnumElement>();

  public EnumType(String name) {
    super(name);
  }

  public List<EnumElement> getElements() {
    return elements;
  }

  public EnumElement find(String name) {
    for (EnumElement itr : elements) {
      if (itr.getName().equals(name)) {
        return itr;
      }
    }
    return null;
  }
}
