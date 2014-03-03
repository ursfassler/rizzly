package fun.traverser;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import error.ErrorType;
import error.RError;
import fun.DefTraverser;
import fun.Fun;
import fun.other.Named;

public class CheckNames extends DefTraverser<Void, Collection<String>> {
  private static final CheckNames INSTANCE = new CheckNames();

  static public void check(Fun fun, Collection<String> blacklist) {
    INSTANCE.traverse(fun, blacklist);
  }

  public static void check(Collection<? extends Fun> fun, List<? extends Named> blacklist) {
    Set<String> set = new HashSet<String>();
    for (Named itr : blacklist) {
      set.add(itr.getName());
    }
    for (Fun itr : fun) {
      INSTANCE.traverse(itr, set);
    }
  }

  @Override
  protected Void visit(Fun obj, Collection<String> param) {
    if (obj instanceof Named) {
      String name = ((Named) obj).getName();
      if (param.contains(name)) {
        RError.err(ErrorType.Error, obj.getInfo(), "Expected name, got keyword " + name);
      }
    }
    return super.visit(obj, param);
  }

}
