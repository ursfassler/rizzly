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
import ast.data.reference.DummyLinkTarget;
import ast.data.reference.RefItem;
import ast.data.reference.RefName;
import ast.data.reference.Reference;
import ast.repository.query.ChildByName;

public class SubLinker {
  final private ChildByName childByName;

  public SubLinker(ChildByName childByName) {
    super();
    this.childByName = childByName;
  }

  public void link(Reference ref, Named root) {

    if (ref.link instanceof DummyLinkTarget) {
      List<String> targetName = new ArrayList<String>();

      String rootName = ((DummyLinkTarget) ref.link).name;

      if (!rootName.equals("self")) {
        targetName.add(rootName);
      }

      for (RefItem itr : ref.offset) {
        String name = ((RefName) itr).name;
        targetName.add(name);
      }

      Named target = childByName.get(root, new Designator(targetName), ref.getInfo());

      ref.link = target;
      ref.offset.clear();
    }

  }
}
