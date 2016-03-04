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

package ast.repository.query.Referencees;

import ast.data.Named;
import ast.data.reference.Anchor;
import ast.data.reference.LinkedAnchor;
import ast.data.reference.Reference;
import ast.data.reference.ReferenceOffset;

public class TargetResolver {

  public <T extends Named> T targetOf(Reference reference, Class<T> kind) {
    return staticTargetOf(reference, kind);
  }

  @Deprecated
  static public <T extends Named> T staticTargetOf(Reference reference, Class<T> kind) {
    Named target = staticTargetOf(reference);
    return (T) target;
  }

  static public Named staticTargetOf(Reference reference) {
    if (reference instanceof ReferenceOffset) {
      ReferenceOffset ofsr = (ReferenceOffset) reference;
      assert (ofsr.getOffset().isEmpty()); // FIXME make it correct or remove function
    }
    return staticTargetOf(reference.getAnchor());
  }

  static public Named staticTargetOf(Anchor anchor) {
    LinkedAnchor linked = (LinkedAnchor) anchor;
    return linked.getLink();
  }
}
