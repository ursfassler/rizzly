package cir.traverser;

import cir.Cir;
import cir.CirBase;
import cir.DefTraverser;
import cir.other.Named;

import common.Designator;

//TODO how to ensure that names are unique?

/**
 * Replaces "{" and "}" from names.
 * 
 * @author urs
 * 
 */
public class Renamer extends DefTraverser<Void, Void> {
  public static void process(CirBase cprog) {
    Renamer cVarDeclToTop = new Renamer();
    cVarDeclToTop.traverse(cprog, null);
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
        case '!':
          // ret += Designator.NAME_SEP;
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
  protected Void visit(Cir obj, Void param) {
    if (obj instanceof Named) {
      String name = cleanName(((Named) obj).getName());
      ((Named) obj).setName(name);
    }
    return super.visit(obj, param);
  }

}
