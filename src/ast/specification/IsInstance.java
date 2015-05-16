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

package ast.specification;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import ast.data.Ast;

public class IsInstance extends Specification {
  final private Set<Ast> instances;

  public IsInstance(Collection<Ast> instances) {
    this.instances = new HashSet<Ast>(instances);
  }

  @Override
  public boolean isSatisfiedBy(Ast candidate) {
    return instances.contains(candidate);
  }

  @Override
  public String toString() {
    return "is in " + instances.toString();
  }

}
