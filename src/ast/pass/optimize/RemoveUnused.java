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

import java.util.Set;

import ast.data.Ast;
import ast.data.AstList;
import ast.data.Namespace;
import ast.dispatcher.other.DepGraph;
import ast.doc.SimpleGraph;
import ast.knowledge.KnowledgeBase;
import ast.pass.AstPass;
import ast.repository.query.Collector;
import ast.specification.ExternalFunction;
import ast.specification.PublicFunction;

public class RemoveUnused implements AstPass {

  @Override
  public void process(Namespace ast, KnowledgeBase kb) {
    AstList<? extends Ast> roots = Collector.select(ast, new PublicFunction().or(new ExternalFunction()));

    SimpleGraph<Ast> g = DepGraph.build(roots);

    Set<Ast> keep = g.vertexSet();

    ast.children.retainAll(keep);

    kb.clear();
  }
}
