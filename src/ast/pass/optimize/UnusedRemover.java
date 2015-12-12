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

package ast.pass.optimize;

import java.util.Collection;
import java.util.Set;

import ast.data.Ast;
import ast.data.AstList;
import ast.data.Namespace;
import ast.data.component.composition.CompUse;
import ast.data.type.base.BaseType;
import ast.dispatcher.DfsTraverser;
import ast.doc.DepGraph;
import ast.doc.SimpleGraph;
import ast.knowledge.KnowledgeBase;
import ast.pass.AstPass;
import ast.repository.query.List;
import ast.repository.query.Single;
import ast.repository.query.TypeFilter;
import ast.specification.IsClass;
import ast.specification.IsInstance;
import ast.specification.Specification;
import error.ErrorType;
import error.RError;

// FIXME if we remove everything unused, we can not typecheck that in EVL
public class UnusedRemover extends AstPass {

  @Override
  public void process(Namespace root, KnowledgeBase kb) {
    CompUse rootcomp = Single.staticForce(TypeFilter.select(root.children, CompUse.class), root.getInfo());
    SimpleGraph<Ast> g = DepGraph.build(rootcomp);

    Set<Ast> keep = g.vertexSet();
    removeUnused(root, keep);
  }

  private static void removeUnused(Namespace ns, Set<Ast> keepset) {
    Specification keep = new IsInstance(keepset);
    keep = keep.or(new IsClass(Namespace.class));
    keep = keep.or(new IsClass(BaseType.class));

    UnusedRemoverWorker worker = new UnusedRemoverWorker(keep);
    worker.traverse(ns, null);
  }

}

class UnusedRemoverWorker extends DfsTraverser<Void, Void> {
  final private Specification keep;

  public UnusedRemoverWorker(Specification keep) {
    super();
    this.keep = keep;
  }

  @Override
  protected Void visitList(Collection<? extends Ast> list, Void param) {
    AstList<? extends Ast> keeplist = List.select(list, keep);
    list.retainAll(keeplist);
    return super.visitList(list, param);
  }

  @Override
  protected Void visit(Ast obj, Void param) {
    // TODO fix it
    if (!keep.isSatisfiedBy(obj)) {
      RError.err(ErrorType.Warning, obj.getInfo(), "Object not removed: " + obj);
    }
    // RError.ass(keep.isSatisfiedBy(obj), obj.getInfo(), "Object not removed: " + obj);
    return super.visit(obj, param);
  }

}
