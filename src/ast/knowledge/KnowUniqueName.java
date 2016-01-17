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

package ast.knowledge;

import ast.Designator;
import ast.data.Ast;
import ast.data.Named;
import ast.dispatcher.DfsTraverser;

public class KnowUniqueName extends KnowledgeEntry {
  static final String START = Designator.NAME_SEP;
  static final String SEP = ":";
  private int nr = -1;

  @Override
  public void init(KnowledgeBase base) {
    KnowUniqueNameGetter getter = new KnowUniqueNameGetter();
    getter.traverse(base.getRoot(), null);
    nr = getter.last;
  }

  public String get(String name) {
    nr++;
    return START + name + SEP + nr;
  }

}

class KnowUniqueNameGetter extends DfsTraverser<Void, Void> {
  int last = -1;

  @Override
  protected Void visit(Ast obj, Void param) {
    if (obj instanceof Named) {
      String name = ((Named) obj).getName();
      if (name.startsWith(KnowUniqueName.START) && name.contains(KnowUniqueName.SEP)) {
        String numstr = name.substring(name.indexOf(KnowUniqueName.SEP) + KnowUniqueName.SEP.length());
        int num = Integer.parseInt(numstr);
        last = Math.max(last, num);
      }
    }
    return super.visit(obj, param);
  }
}
