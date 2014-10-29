package fun.traverser;

import java.util.Collection;

import error.ErrorType;
import error.RError;
import fun.DefTraverser;
import fun.Fun;
import fun.other.Named;

public class CheckNames extends DefTraverser<Void, Collection<String>> {

  static public void check(Fun fun, Collection<String> blacklist) {
    CheckNames checkNames = new CheckNames();
    checkNames.traverse(fun, blacklist);
  }

  @Override
  protected Void visit(Fun obj, Collection<String> param) {
    if (obj instanceof Named) {
      if (param.contains(((Named) obj).getName())) {
        RError.err(ErrorType.Error, obj.getInfo(), "Expected name, got keyword " + ((Named) obj).getName());
      }
    }
    return super.visit(obj, param);
  }

}
