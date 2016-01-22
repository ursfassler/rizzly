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

import ast.data.function.header.FuncFunction;
import ast.data.function.header.Response;
import ast.data.function.header.FuncSubHandlerQuery;

public class PureFunction extends OrSpec {

  public PureFunction() {
    super(pureFuncSpec());
  }

  static private Collection<Specification> pureFuncSpec() {
    Collection<Specification> spec = new HashSet<Specification>();
    spec.add(new IsClass(FuncFunction.class));
    spec.add(new IsClass(FuncSubHandlerQuery.class));
    spec.add(new IsClass(Response.class));
    return spec;
  }

}
