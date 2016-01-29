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

package ast.repository.query;

import ast.data.Ast;
import ast.data.AstList;
import ast.specification.Specification;
import ast.visitor.Visitor;

public class CollectorVisitor implements Visitor {
  final private AstList<Ast> matched = new AstList<Ast>();
  final private Specification spec;

  public CollectorVisitor(Specification spec) {
    this.spec = spec;
  }

  public void visit(Ast ast) {
    if (spec.isSatisfiedBy(ast)) {
      matched.add(ast);
    }
  }

  public AstList<Ast> getMatched() {
    return matched;
  }

}
