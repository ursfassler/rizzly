package pir.traverser;

import pir.DefTraverser;
import pir.expression.reference.RefIndex;
import pir.expression.reference.RefItem;
import pir.expression.reference.RefName;
import pir.expression.reference.Reference;

public class RefReplacer<P> extends DefTraverser<RefItem, P> {

  @Override
  protected RefItem visitReference(Reference obj, P param) {
    obj.setRef(visit(obj.getRef(), param));
    return null;
  }

  @Override
  protected RefItem visitRefCall(RefCall obj, P param) {
    visitList(obj.getParameter(), param);
    obj.setPrevious(visit(obj.getPrevious(),param));
    return obj;
  }

  @Override
  protected RefItem visitRefName(RefName obj, P param) {
    obj.setPrevious(visit(obj.getPrevious(),param));
    return obj;
  }

  @Override
  protected RefItem visitRefIndex(RefIndex obj, P param) {
    visit(obj.getIndex(),param);
    obj.setPrevious(visit(obj.getPrevious(),param));
    return obj;
  }

  @Override
  protected RefItem visitRefHead(RefHead obj, P param) {
    return obj;
  }

}
