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

package ast.pass;

import java.util.HashSet;
import java.util.Set;

import pass.AstPass;
import util.SimpleGraph;
import ast.data.Ast;
import ast.data.Namespace;
import ast.data.function.Function;
import ast.data.function.FunctionProperty;
import ast.knowledge.KnowledgeBase;
import ast.traverser.other.ClassGetter;
import ast.traverser.other.DepGraph;

public class RemoveUnused extends AstPass {

  @Override
  public void process(Namespace ast, KnowledgeBase kb) {
    Set<Function> roots = new HashSet<Function>();

    for (Function func : ClassGetter.getRecursive(Function.class, ast)) {
      if ((func.property == FunctionProperty.Public) || (func.property == FunctionProperty.External)) {
        roots.add(func);
      }
    }

    SimpleGraph<Ast> g = DepGraph.build(roots);

    Set<Ast> keep = g.vertexSet();

    ast.children.retainAll(keep);

    kb.clear();
  }
}
