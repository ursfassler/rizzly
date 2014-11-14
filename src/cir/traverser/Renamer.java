/**
 *  This file is part of Rizzly.
 *
 *  Rizzly is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Rizzly is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with Rizzly.  If not, see <http://www.gnu.org/licenses/>.
 */

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
