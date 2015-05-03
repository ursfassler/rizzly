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

package ast.repository;

import ast.data.Ast;
import ast.data.AstList;
import ast.specification.Specification;
import ast.traverser.DefTraverser;

public class Collector {
  static public AstList<? extends Ast> select(Ast root, Specification spec) {
    CollectorTraverser traverser = new CollectorTraverser(spec);
    traverser.traverse(root, null);
    return traverser.getMatched();
  }

  static public AstList<? extends Ast> select(AstList<? extends Ast> roots, Specification spec) {
    CollectorTraverser traverser = new CollectorTraverser(spec);
    traverser.traverse(roots, null);
    return traverser.getMatched();
  }
}

class CollectorTraverser extends DefTraverser<Void, Void> {
  final private AstList<Ast> matched = new AstList<Ast>();
  final private Specification spec;

  public CollectorTraverser(Specification spec) {
    super();
    this.spec = spec;
  }

  @Override
  protected Void visit(Ast obj, Void param) {
    if (spec.isSatisfiedBy(obj)) {
      matched.add(obj);
    }
    return super.visit(obj, param);
  }

  public AstList<Ast> getMatched() {
    return matched;
  }
}
