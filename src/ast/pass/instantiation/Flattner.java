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

package ast.pass.instantiation;

import java.util.Collection;
import java.util.HashSet;

import ast.data.Ast;
import ast.data.AstList;
import ast.data.Namespace;
import ast.data.function.Function;
import ast.data.type.Type;
import ast.data.variable.Constant;
import ast.data.variable.StateVariable;
import ast.knowledge.KnowledgeBase;
import ast.pass.AstPass;
import ast.repository.manipulator.PathPrefixer;
import ast.repository.query.Collector;
import ast.specification.IsClass;
import ast.specification.OrSpec;
import ast.specification.Specification;

public class Flattner extends AstPass {

  @Override
  public void process(Namespace ast, KnowledgeBase kb) {
    Specification spec = extractSpec();

    PathPrefixer.prefix(ast.children, spec);
    AstList<? extends Ast> items = Collector.select(ast, spec);
    ast.children.clear();
    ast.children.addAll(items);
  }

  private Specification extractSpec() {
    Collection<Specification> orcol = new HashSet<Specification>();
    orcol.add(new IsClass(Function.class));
    orcol.add(new IsClass(StateVariable.class));
    orcol.add(new IsClass(Constant.class));
    orcol.add(new IsClass(Type.class));
    Specification spec = new OrSpec(orcol);
    return spec;
  }
}
