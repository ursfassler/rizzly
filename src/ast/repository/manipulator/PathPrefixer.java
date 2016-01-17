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

package ast.repository.manipulator;

import java.util.Collection;

import ast.Designator;
import ast.data.Ast;
import ast.data.Named;
import ast.dispatcher.DfsTraverser;
import ast.specification.Specification;

public class PathPrefixer {
  static public void prefix(Ast root, Specification spec) {
    PathPrefixerTraverser traverser = new PathPrefixerTraverser(spec);
    traverser.traverse(root, new Designator());
  }

  static public void prefix(Collection<Ast> roots, Specification spec) {
    PathPrefixerTraverser traverser = new PathPrefixerTraverser(spec);
    traverser.traverse(roots, new Designator());
  }
}

class PathPrefixerTraverser extends DfsTraverser<Void, Designator> {
  final private Specification spec;

  public PathPrefixerTraverser(Specification spec) {
    super();
    this.spec = spec;
  }

  @Override
  protected Void visit(Ast obj, Designator param) {
    if (obj instanceof Named) {
      param = new Designator(param, ((Named) obj).getName());
    }
    if (spec.isSatisfiedBy(obj)) {
      assert (obj instanceof Named);
      ((Named) obj).setName(param.toString(Designator.NAME_SEP));
    }
    return super.visit(obj, param);
  }
}
