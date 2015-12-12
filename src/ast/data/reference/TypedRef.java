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

package ast.data.reference;

import ast.ElementInfo;
import ast.data.Ast;
import ast.data.AstBase;

public abstract class TypedRef<T extends Ast> extends AstBase {
  public Reference ref;

  public TypedRef(ElementInfo info, Reference ref) {
    super(info);
    this.ref = ref;
  }

  public T getTarget() {
    return (T) ref.getTarget();
  }

  @Override
  public String toString() {
    return "->" + ref;
  }

}