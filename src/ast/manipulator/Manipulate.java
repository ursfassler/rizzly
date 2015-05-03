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

package ast.manipulator;

import java.util.Collection;

import ast.data.Ast;
import ast.specification.List;
import ast.specification.Specification;
import ast.traverser.DefTraverser;

public class Manipulate {
  static public void remove(Ast root, Specification spec) {
    Remover traverser = new Remover(spec);
    traverser.traverse(root, null);
  }

}

class Remover extends DefTraverser<Void, Void> {
  final private Specification spec;

  public Remover(Specification spec) {
    super();
    this.spec = spec;
  }

  @Override
  protected Void visitList(Collection<? extends Ast> list, Void param) {
    list.removeAll(List.select(list, spec));
    return super.visitList(list, param);
  }

}
