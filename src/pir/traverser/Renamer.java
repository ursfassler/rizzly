package pir.traverser;

import pir.DefTraverser;
import pir.Pir;
import pir.PirObject;
import pir.expression.reference.Referencable;

import common.Designator;

//TODO how to ensure that names are unique?
//TODO use blacklist with llvm keywords

/**
 * Replaces "{", "}" and "," in names.
 * 
 * @author urs
 * 
 */
public class Renamer extends DefTraverser<Void, Void> {
  public static void process(PirObject cprog) {
    Renamer cVarDeclToTop = new Renamer();
    cVarDeclToTop.traverse(cprog, null);
  }

  private void rename(Referencable item) {
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
  protected Void visit(Pir obj, Void param) {
    if (obj instanceof Referencable) {
      rename((Referencable) obj);
    }
    return super.visit(obj, param);
  }

}
