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

package ast.pass.others;

import ast.data.Ast;
import ast.data.AstList;
import ast.data.Namespace;
import ast.data.type.Type;
import ast.knowledge.KnowledgeBase;
import ast.pass.AstPass;
import ast.repository.manipulator.Manipulate;
import ast.repository.manipulator.PathPrefixer;
import ast.repository.query.Collector;
import ast.specification.IsClass;
import ast.specification.Specification;

/**
 * Moves all types to the top level
 *
 * @author urs
 *
 */
public class TypeUplift extends AstPass {

  @Override
  public void process(Namespace ast, KnowledgeBase kb) {
    Specification type = new IsClass(Type.class);
    PathPrefixer.prefix(ast.children, type);

    AstList<? extends Ast> types = Collector.select(ast, type);
    Manipulate.remove(ast, type);
    ast.children.addAll(types);
  }
}
