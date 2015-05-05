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

import java.util.Collection;

import ast.data.Ast;
import ast.data.AstList;
import ast.specification.IsClass;

public class TypeFilter {

  static public <T extends Ast> AstList<T> select(Collection<? extends Ast> list, Class<T> kind) {
    return List.select(list, new IsClass(kind)).castTo(kind);
  }
}
