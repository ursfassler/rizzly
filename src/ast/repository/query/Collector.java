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
import ast.visitor.DefaultHandler;
import ast.visitor.EveryVisitor;
import ast.visitor.VisitorAcceptor;

public class Collector extends EveryVisitor {
  private final CollectorVisitor collector;

  public Collector(Specification spec) {
    super();
    collector = new CollectorVisitor(spec);
    addDefaultHandler(collector);
  }

  public AstList<Ast> getMatched() {
    return collector.getMatched();
  }

  @Deprecated
  public static AstList<Ast> select(VisitorAcceptor ast, Specification spec) {
    Collector visitor = new Collector(spec);
    ast.accept(visitor);
    return visitor.getMatched();
  }
}

class CollectorVisitor implements DefaultHandler {
  final private AstList<Ast> matched = new AstList<Ast>();
  final private Specification spec;

  public CollectorVisitor(Specification spec) {
    super();
    this.spec = spec;
  }

  @Override
  public void visit(Ast ast) {
    if (spec.isSatisfiedBy(ast)) {
      matched.add(ast);
    }
  }

  public AstList<Ast> getMatched() {
    return matched;
  }

}
