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

import ast.data.AstBase;
import ast.data.component.hfsm.State;

public class SimpleReference extends AstBase implements Reference {
  private Anchor anchor;

  public SimpleReference(Anchor anchor) {
    this.anchor = anchor;
  }

  @Override
  public Anchor getAnchor() {
    return anchor;
  }

  @Override
  public void setAnchor(Anchor anchor) {
    this.anchor = anchor;
  }

  @Override
  public String toString() {
    return ">" + anchor;
  }

  @Override
  @Deprecated
  public State getTarget() {
    return (State) anchor.getTarget();
  }

}