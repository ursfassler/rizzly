package evl.traverser.typecheck;

import java.util.HashSet;
import java.util.Set;

//TODO for what use is this class?
public enum InterfaceElements {
  iface_in, iface_out, type, function, variable_state, constant;

  public static Set<InterfaceElements> full() {
    HashSet<InterfaceElements> ret = new HashSet<InterfaceElements>();
    for (InterfaceElements elem : InterfaceElements.values()) {
      ret.add(elem);
    }
    return ret;
  }

  public static Set<InterfaceElements> fullClass() {
    HashSet<InterfaceElements> ret = new HashSet<InterfaceElements>();
    ret.add(type);
    ret.add(constant);
    return ret;
  }

  public static Set<InterfaceElements> fullHeader() {
    HashSet<InterfaceElements> ret = new HashSet<InterfaceElements>();
    ret.add(iface_in);
    ret.add(iface_out);
    return ret;
  }

  public Set<InterfaceElements> set(){
    Set<InterfaceElements> ret = new HashSet<InterfaceElements>();
    ret.add(this);
    return ret;
  }
}
