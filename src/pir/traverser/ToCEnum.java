package pir.traverser;

import pir.DefTraverser;
import pir.expression.reference.RefHead;
import pir.expression.reference.RefItem;
import pir.expression.reference.RefName;
import pir.expression.reference.Referencable;
import pir.other.Program;
import pir.type.EnumElement;
import pir.type.EnumType;

import common.Designator;

public class ToCEnum {

  public static void process(Program prg) {
    Reduce reduce = new Reduce();
    reduce.traverse(prg, null);
    Rename rename = new Rename();
    rename.traverse(prg, null);
  }

}

class Reduce extends RefReplacer<Void> {

  @Override
  protected RefItem visitRefName(RefName obj, Void param) {
    RefItem prev = visit(obj.getPrevious(), param);
    if (prev instanceof RefHead) {
      Referencable named = ((RefHead) prev).getRef();
      if (named instanceof EnumType) {
        EnumElement element = ((EnumType) named).find(obj.getName());
        assert (element != null);
        return new RefHead(element);
      }
    }
    return obj;
  }

}

class Rename extends DefTraverser<Void, Void> {
  @Override
  protected Void visitEnumType(EnumType obj, Void param) {
    for (EnumElement itr : obj.getElements()) {
      String name = obj.getName() + Designator.NAME_SEP + itr.getName();
      itr.setName(name);
    }
    return null;
  }

}
