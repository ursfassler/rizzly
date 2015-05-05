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

public class ChildCollector {

  static public AstList<Ast> select(Ast sub, Specification spec) {
    ChildCollectorTraverser collector = new ChildCollectorTraverser(spec);
    collector.traverse(sub, true);
    return collector.getMatched();
  }

}

class ChildCollectorTraverser extends DefTraverser<Void, Boolean> {
  final private AstList<Ast> matched = new AstList<Ast>();
  final private Specification spec;

  public ChildCollectorTraverser(Specification spec) {
    super();
    this.spec = spec;
  }

  @Override
  protected Void visit(Ast obj, Boolean param) {
    if (param) {
      super.visit(obj, false);
    } else {
      if (spec.isSatisfiedBy(obj)) {
        getMatched().add(obj);
      }
    }
    return null;
  }

  public AstList<Ast> getMatched() {
    return matched;
  }

}
