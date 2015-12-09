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

import org.junit.Assert;
import org.junit.Test;

import ast.Designator;
import ast.ElementInfo;
import ast.data.AstList;
import ast.data.Named;
import ast.data.Namespace;
import ast.data.component.hfsm.StateContent;
import ast.data.reference.DummyLinkTarget;
import ast.data.reference.RefItem;
import ast.data.reference.RefName;
import ast.data.reference.Reference;

public class SubLinker_Test {

  private class Content extends Named implements StateContent {
    public Content(ElementInfo info, String name) {
      super(info, name);
    }
  }

  @Test
  public void use_info_from_reference_if_not_found() {
    Namespace root = new Namespace(null, "");

    ElementInfo info = new ElementInfo("", 42, 57);
    Reference ref = reference(info, new Designator("a"));

    SubLinker.link(ref, root);

    Assert.assertEquals(0, ref.offset.size());
  }

  @Test
  public void link_only_on_same_level() {
    Namespace root = new Namespace(null, "");

    Content content = new Content(null, "a");
    root.children.add(content);

    Reference ref = reference(null, new Designator("a"));

    SubLinker.link(ref, root);

    Assert.assertEquals(content, ref.link);
    Assert.assertEquals(0, ref.offset.size());
  }

  @Test
  public void link_target_on_same_level() {
    Namespace root = new Namespace(null, "");

    root.children.add(new Content(null, "a"));
    Content content = new Content(null, "b");
    root.children.add(content);
    root.children.add(new Content(null, "c"));

    Reference ref = reference(null, new Designator("b"));

    SubLinker.link(ref, root);

    Assert.assertEquals(content, ref.link);
    Assert.assertEquals(0, ref.offset.size());
  }

  @Test
  public void link_target_on_second_level() {
    Namespace root = new Namespace(null, "");

    Namespace second = new Namespace(null, "a");
    root.children.add(second);

    Content content = new Content(null, "b");
    second.children.add(content);

    Reference ref = reference(null, new Designator("a", "b"));

    SubLinker.link(ref, root);

    Assert.assertEquals(content, ref.link);
    Assert.assertEquals(0, ref.offset.size());
  }

  private Reference reference(ElementInfo info, Designator name) {
    ArrayList<String> list = name.toList();

    DummyLinkTarget target = new DummyLinkTarget(null, list.get(0));
    list.remove(0);

    AstList<RefItem> offset = new AstList<RefItem>();
    for (String itr : list) {
      offset.add(new RefName(null, itr));
    }

    return new Reference(info, target, offset);
  }
}
