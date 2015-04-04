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

import java.util.ArrayList;
import java.util.List;

import pass.EvlPass;
import error.ErrorType;
import error.RError;
import evl.data.Named;
import evl.data.Namespace;
import evl.data.expression.reference.BaseRef;
import evl.data.type.out.AliasType;
import evl.knowledge.KnowledgeBase;
import evl.traverser.DefTraverser;

public class ReduceAliasType extends EvlPass {

  @Override
  public void process(Namespace evl, KnowledgeBase kb) {
    ReduceAliasTypeWorker worker = new ReduceAliasTypeWorker();
    worker.traverse(evl, null);
  }

}

class ReduceAliasTypeWorker extends DefTraverser<Void, Void> {

  @Override
  protected Void visitBaseRef(BaseRef obj, Void param) {
    super.visitBaseRef(obj, param);
    Named link = obj.link;
    List<Named> checked = new ArrayList<Named>();
    while (link instanceof AliasType) {
      checked.add(link);
      link = ((AliasType) link).ref.link;
      if (checked.contains(link)) {
        for (Named itr : checked) {
          RError.err(ErrorType.Hint, itr.getInfo(), "part of recursive type alias: " + itr.name);
        }
        RError.err(ErrorType.Error, obj.getInfo(), "recursive type alias found: " + obj.link.name);
      }
    }
    obj.link = link;
    return null;
  }
}
