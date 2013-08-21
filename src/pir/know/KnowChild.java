package pir.know;

import java.util.ArrayList;
import java.util.List;

import pir.NullTraverser;
import pir.Pir;
import pir.PirObject;
import pir.expression.reference.Referencable;
import pir.other.Program;

public class KnowChild extends KnowledgeEntry {
  private ChildNower nower = new ChildNower();
  private KnowledgeBase kb;

  @Override
  public void init(KnowledgeBase kb) {
    this.kb = kb;
  }

  public Pir find(Pir parent, String childName) {
    return nower.traverse(parent, childName);
  }

}

class ChildNower extends NullTraverser<Pir, String> {

  @Override
  protected Pir doDefault(PirObject obj, String param) {
    throw new RuntimeException("not yet implemented: " + obj.getClass().getCanonicalName());
  }

  private Pir find(List<Referencable> list, String param) {
    for (Referencable itr : list) {
      if (itr.getName().equals(param)) {
        return itr;
      }
    }
    return null;
  }

  @Override
  protected Pir visitProgram(Program obj, String param) {
    List<Referencable> list = new ArrayList<Referencable>();
    list.addAll(obj.getConstant());
    list.addAll(obj.getFunction());
    list.addAll(obj.getType());
    list.addAll(obj.getVariable());
    return find(list, param);
  }

}
