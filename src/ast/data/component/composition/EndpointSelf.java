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

package ast.data.component.composition;

import ast.data.AstBase;
import ast.data.reference.Reference;
import ast.meta.MetaList;

public class EndpointSelf extends AstBase implements Endpoint {
  private final Reference funcRef;

  public EndpointSelf(Reference funcRef) {
    this.funcRef = funcRef;
  }

  @Deprecated
  public EndpointSelf(MetaList info, Reference funcRef) {
    metadata().add(info);
    this.funcRef = funcRef;
  }

  public Reference getFuncRef() {
    return funcRef;
  }

  @Override
  public String toString() {
    return "self." + getFuncRef().getTarget().getName();
  }

}
