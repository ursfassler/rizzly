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

package evl.pass.infrastructure;

import java.util.HashSet;
import java.util.Set;

import pass.EvlPass;
import error.ErrorType;
import error.RError;
import evl.DefTraverser;
import evl.Evl;
import evl.expression.reference.BaseRef;
import evl.knowledge.KnowledgeBase;
import evl.other.Namespace;

public class LinkTargetExists extends EvlPass {

  @Override
  public void process(Namespace evl, KnowledgeBase kb) {
    LinkOkWorker worker = new LinkOkWorker();
    worker.traverse(evl, null);
    Set<Evl> problem = worker.getLonelyTargets();
    if (!problem.isEmpty()) {
      for (Evl itr : problem) {
        RError.err(ErrorType.Hint, itr.getInfo(), "missing " + itr);
      }
      RError.err(ErrorType.Fatal, "missing link targets found");
    }
  }

}

class LinkOkWorker extends DefTraverser<Void, Void> {
  final private Set<Evl> target = new HashSet<Evl>();
  final private Set<Evl> found = new HashSet<Evl>();

  Set<Evl> getLonelyTargets() {
    Set<Evl> ret = new HashSet<Evl>(target);
    ret.removeAll(found);
    return ret;
  }

  @Override
  protected Void visit(Evl obj, Void param) {
    found.add(obj);
    return super.visit(obj, param);
  }

  @Override
  protected Void visitBaseRef(BaseRef obj, Void param) {
    target.add(obj.link);
    return super.visitBaseRef(obj, param);
  }

}
