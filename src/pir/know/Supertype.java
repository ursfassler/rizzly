package pir.know;

import java.util.ArrayList;
import java.util.LinkedList;

import pir.NullTraverser;
import pir.PirObject;
import pir.type.Type;

//TODO for what is this class used?
public class Supertype extends NullTraverser<Type, Void> {

  static public ArrayList<Type> getHirarchy(Type type) {
    Supertype supertype = new Supertype();
    LinkedList<Type> ret = new LinkedList<Type>();

    while (type != null) {
      ret.push(type);
      type = supertype.traverse(type, null);
    }

    return new ArrayList<Type>(ret);
  }

  @Override
  protected Type doDefault(PirObject obj, Void param) {
    throw new RuntimeException("not yet implemented");
  }

}
