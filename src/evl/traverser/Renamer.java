package evl.traverser;

import common.Designator;

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
    String name = cleanName(item.getName());
    item.setName(name);
  }

  public static String cleanName(String name) {
    String ret = "";
    for (int i = 0; i < name.length(); i++) {
      char sym = name.charAt(i);
      switch (sym) {
      case '{':
        if (name.charAt(i + 1) != '}') {
          ret += Designator.NAME_SEP;
        }
        break;
      case '}':
        break;
      case ',':
        ret += Designator.NAME_SEP;
        break;
      case '-':
        ret += Designator.NAME_SEP;
        break;
      case '.':
        ret += Designator.NAME_SEP;
        break;
      default:
        assert ((sym >= 'a' && sym <= 'z') || (sym >= 'A' && sym <= 'Z') || (sym >= '0' && sym <= '9') || (sym == '_'));
        ret += sym;
        break;
      }
    }
    return ret;
  }

  @Override
  protected Void visit(Evl obj, Void param) {
    if (obj instanceof Named) {
      rename((Named) obj);
    }
    return super.visit(obj, param);
  }

}
