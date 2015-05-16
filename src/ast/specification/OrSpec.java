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

public class OrSpec extends Specification {
  final private Set<Specification> spec;

  public OrSpec(Specification leftSpec, Specification rightSpec) {
    super();
    spec = new HashSet<Specification>();
    spec.add(leftSpec);
    spec.add(rightSpec);
  }

  public OrSpec(Collection<Specification> spec) {
    super();
    this.spec = new HashSet<Specification>(spec);
  }

  @Override
  public boolean isSatisfiedBy(Ast candidate) {
    for (Specification itr : spec) {
      if (itr.isSatisfiedBy(candidate)) {
        return true;
      }
    }
    return false;
  }

  @Override
  public String toString() {
    String ret = "";
    boolean printOr = false;
    for (Specification itr : spec) {
      if (printOr) {
        ret += " or ";
      }
      ret += "(" + itr + ")";
      printOr = true;
    }
    return ret;
  }

}
