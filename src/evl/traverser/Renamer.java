package evl.traverser;

import evl.DefTraverser;
import evl.Evl;
import evl.other.Named;

//TODO how to ensure that names are unique?
//TODO use blacklist with C keywords
/**
 * @see pir.traverser.Renamer
 * 
 * @author urs
 * 
 */
public class Renamer extends DefTraverser<Void, Void> {

  public static void process(Evl cprog) {
    Renamer cVarDeclToTop = new Renamer();
    cVarDeclToTop.traverse(cprog, null);
  }

  private void rename(Named item) {
    String name = pir.traverser.Renamer.cleanName(item.getName());
    item.setName(name);
  }

  @Override
  protected Void visit(Evl obj, Void param) {
    if (obj instanceof Named) {
      rename((Named) obj);
    }
    return super.visit(obj, param);
  }
}
