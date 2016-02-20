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
import ast.data.Named;

public class UnlinkedAnchor extends AstBase implements Anchor {
  private final String linkName;

  public UnlinkedAnchor(String linkName) {
    this.linkName = linkName;
  }

  public String getLinkName() {
    return linkName;
  }

  @Override
  public String targetName() {
    return linkName;
  }

  @Override
  @Deprecated
  public Named getTarget() {
    throw new RuntimeException("never implemented");
  }

}
