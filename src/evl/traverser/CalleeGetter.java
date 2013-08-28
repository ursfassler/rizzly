package evl.traverser;

import java.util.Set;

import evl.DefTraverser;
import evl.expression.reference.Reference;
import evl.function.FunctionHeader;

/**
 * Adds all called functions to the parameter
 * 
 * @author urs
 * 
 */
public class CalleeGetter<R> extends DefTraverser<R, Set<FunctionHeader>> {

  @Override
  protected R visitReference(Reference obj, Set<FunctionHeader> param) {
    if (obj.getLink() instanceof FunctionHeader) {
      param.add((FunctionHeader) obj.getLink());
    }
    return super.visitReference(obj, param);
  }
}
