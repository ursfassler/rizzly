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

package ast.pass.linker;

import java.util.ArrayList;
import java.util.List;

import ast.Designator;
import ast.data.Named;
import ast.data.reference.LinkedAnchor;
import ast.data.reference.RefItem;
import ast.data.reference.RefName;
import ast.data.reference.Reference;
import ast.data.reference.ReferenceOffset;
import ast.data.reference.UnlinkedAnchor;
import ast.repository.query.ChildByName;

public class SubLinker {
  final private ChildByName childByName;

  public SubLinker(ChildByName childByName) {
    super();
    this.childByName = childByName;
  }

  public void link(Reference ref, Named root) {
    if (ref.getAnchor() instanceof UnlinkedAnchor) {
      List<String> targetName = new ArrayList<String>();

      String rootName = ((UnlinkedAnchor) ref.getAnchor()).getLinkName();

      if (!rootName.equals("self")) {
        targetName.add(rootName);
      }

      if (ref instanceof ReferenceOffset) {
        ReferenceOffset referenceOffset = (ReferenceOffset) ref;
        for (RefItem itr : referenceOffset.getOffset()) {
          String name = ((RefName) itr).name;
          targetName.add(name);
        }
        referenceOffset.getOffset().clear();
      }

      Named target = childByName.get(root, new Designator(targetName), ref.metadata());

      ref.setAnchor(new LinkedAnchor(target));
    }

  }
}
