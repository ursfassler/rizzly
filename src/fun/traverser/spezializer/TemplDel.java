package fun.traverser.spezializer;

import java.util.HashSet;
import java.util.Set;

import error.ErrorType;
import error.RError;
import fun.DefTraverser;
import fun.Fun;
import fun.other.FunList;
import fun.other.Template;

/**
 * Tries to remove all templates. Detects missed ones.
 */
public class TemplDel extends DefTraverser<Void, Void> {
  static private final TemplDel INSTANCE = new TemplDel();

  public static void process(Fun fun) {
    INSTANCE.traverse(fun, null);
  }

  @Override
  protected Void visitList(FunList<? extends Fun> list, Void param) {
    Set<Template> remove = new HashSet<Template>();
    for (Fun ast : list) {
      if (ast instanceof Template) {
        remove.add((Template) ast);
      } else {
        visit(ast, param);
      }
    }
    list.removeAll(remove);
    return null;
  }

  @Override
  protected Void visitDeclaration(Template obj, Void param) {
    RError.err(ErrorType.Fatal, obj.getInfo(), "missed template");
    return null;
  }

}
