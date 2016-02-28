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

package ast.pass.check.sanity;

import java.util.HashSet;
import java.util.Set;

import ast.data.Ast;
import ast.data.Namespace;
import ast.data.reference.LinkedAnchor;
import ast.dispatcher.DfsTraverser;
import ast.knowledge.KnowledgeBase;
import ast.pass.AstPass;
import error.ErrorType;
import error.RError;

public class LinkTargetExists implements AstPass {

  @Override
  public void process(Namespace ast, KnowledgeBase kb) {
    LinkOkWorker worker = new LinkOkWorker();
    worker.traverse(ast, null);
    Set<Ast> problem = worker.getLonelyTargets();
    if (!problem.isEmpty()) {
      for (Ast itr : problem) {
        RError.err(ErrorType.Hint, "missing " + itr, itr.metadata());
      }
      RError.err(ErrorType.Fatal, "missing link targets found");
    }
  }

}

class LinkOkWorker extends DfsTraverser<Void, Void> {
  final private Set<Ast> target = new HashSet<Ast>();
  final private Set<Ast> found = new HashSet<Ast>();

  Set<Ast> getLonelyTargets() {
    Set<Ast> ret = new HashSet<Ast>(target);
    ret.removeAll(found);
    return ret;
  }

  @Override
  protected Void visit(Ast obj, Void param) {
    found.add(obj);
    return super.visit(obj, param);
  }

  @Override
  protected Void visitLinkedAnchor(LinkedAnchor obj, Void param) {
    target.add(obj.getLink());
    return super.visitLinkedAnchor(obj, param);
  }

}
