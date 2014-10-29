package evl.traverser;

import java.util.Set;

import common.Designator;

import evl.DefTraverser;
import evl.Evl;
import evl.other.Named;

//TODO how to ensure that names are unique?
//TODO use blacklist with keywords
//TODO use better names for public stuff
/**
 * @see pir.traverser.Renamer
 * 
 * @author urs
 * 
 */
public class Renamer extends DefTraverser<Void, Void> {

  final private Set<String> blacklist;

  public Renamer(Set<String> blacklist) {
    super();
    this.blacklist = blacklist;
  }

  public static void process(Evl cprog, Set<String> blacklist) {
    Renamer cVarDeclToTop = new Renamer(blacklist);
    cVarDeclToTop.traverse(cprog, null);
  }

  private String cleanName(String name) {
    String ret = cir.traverser.Renamer.cleanName(name);

    while (blacklist.contains(ret.toLowerCase())) {
      ret += Designator.NAME_SEP;
    }

    return ret;
  }

  @Override
  protected Void visit(Evl obj, Void param) {
    if (obj instanceof Named) {
      Named item = (Named) obj;
      String name = cleanName(item.getName());
      item.setName(name);
    }
    return super.visit(obj, param);
  }

}
