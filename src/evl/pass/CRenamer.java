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

package evl.pass;

import pass.EvlPass;

import common.Designator;

import evl.DefTraverser;
import evl.Evl;
import evl.expression.reference.RefName;
import evl.knowledge.KnowledgeBase;
import evl.other.Named;
import evl.other.Namespace;

//TODO how to ensure that names are unique?
//TODO merge with Renamer?
/**
 * Replaces "{" and "}" from names.
 *
 * @author urs
 *
 */
public class CRenamer extends EvlPass {

  @Override
  public void process(Namespace evl, KnowledgeBase kb) {
    RenamerWorker cVarDeclToTop = new RenamerWorker();
    cVarDeclToTop.traverse(evl, null);
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
        case '-':
        case '.':
        case ':':
          ret += Designator.NAME_SEP;
          break;
        case '!':
          // ret += Designator.NAME_SEP;
          break;
        default:
          assert ((sym >= 'a' && sym <= 'z') || (sym >= 'A' && sym <= 'Z') || (sym >= '0' && sym <= '9') || (sym == '_'));
          ret += sym;
          break;
      }
    }
    return ret;
  }

}

class RenamerWorker extends DefTraverser<Void, Void> {

  @Override
  protected Void visit(Evl obj, Void param) {
    if (obj instanceof Named) {
      String name = CRenamer.cleanName(((Named) obj).getName());
      ((Named) obj).setName(name);
    }
    return super.visit(obj, param);
  }

  @Override
  protected Void visitRefName(RefName obj, Void param) {
    String name = CRenamer.cleanName(obj.name);
    obj.name = name;
    return null;
  }

}
