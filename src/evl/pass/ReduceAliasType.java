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
import evl.DefTraverser;
import evl.expression.reference.BaseRef;
import evl.knowledge.KnowledgeBase;
import evl.other.Named;
import evl.other.Namespace;
import evl.type.out.AliasType;

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
    Named link = obj.getLink();
    List<Named> checked = new ArrayList<Named>();
    while (link instanceof AliasType) {
      checked.add(link);
      link = ((AliasType) link).getRef().getLink();
      if (checked.contains(link)) {
        for (Named itr : checked) {
          RError.err(ErrorType.Hint, itr.getInfo(), "part of recursive type alias: " + itr.getName());
        }
        RError.err(ErrorType.Error, obj.getInfo(), "recursive type alias found: " + obj.getLink().getName());
      }
    }
    obj.setLink(link);
    return null;
  }
}
