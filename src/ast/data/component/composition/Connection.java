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

abstract public class Connection extends AstBase {
  private Endpoint src;
  private Endpoint dst;

  public Connection(Endpoint src, Endpoint dst) {
    this.src = src;
    this.dst = dst;
  }

  public Endpoint getSrc() {
    return src;
  }

  public void setSrc(Endpoint value) {
    src = value;
  }

  public Endpoint getDst() {
    return dst;
  }

  public void setDst(Endpoint value) {
    dst = value;
  }

}
