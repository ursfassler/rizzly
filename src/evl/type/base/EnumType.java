package evl.type.base;


import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import common.Designator;
import common.ElementInfo;

import evl.type.Type;


public class EnumType extends Type {
  final private ArrayList<EnumElement> element;

  public EnumType(ElementInfo info, String name, Collection<EnumElement> elements) {
    super(info, name);
    this.element = new ArrayList<EnumElement>(elements);
  }

  public List<EnumElement> getElement() {
    return element;
  }

  public EnumElement find(String name) {
    for (EnumElement itr : element) {
      if (itr.getName().equals(name)) {
        return itr;
      }
    }
    return null;
  }
  
  public boolean isSupertypeOf( EnumType sub ){
    return element.containsAll(sub.element);
  }

  /**
   * returns true if this does not contain all elements of supertype
   */
  public boolean isRealSubtype( EnumType supertype ){
    assert( supertype.isSupertypeOf(this) );
    return this.element.size() < supertype.element.size();
  }

  public String makeSubtypeName(Collection<EnumElement> subElem) {
    assert( element.containsAll(subElem) );
    if( element.size() == subElem.size() ){
      return getName();
    }
    
    String ret = getName() + Designator.NAME_SEP;
    for( int i = 0; i < element.size(); i++ ){
      if( subElem.contains(element.get(i)) ){
        ret += "1";
      } else {
        ret += "0";
      }
    }
    
    return ret;
  }
  
}
