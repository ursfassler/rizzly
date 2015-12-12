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

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;

import org.junit.Assert;
import org.junit.Test;

import ast.Designator;
import ast.ElementInfo;
import ast.data.AstList;
import ast.data.Named;
import ast.data.Namespace;
import ast.data.reference.DummyLinkTarget;
import ast.data.reference.RefFactory;
import ast.data.reference.RefItem;
import ast.data.reference.RefName;
import ast.data.reference.Reference;
import ast.repository.query.ChildByName;

public class SubLinker_Test {

  final static private ElementInfo info = ElementInfo.NO;
  final private ChildByName childByName = mock(ChildByName.class);
  final private SubLinker testee = new SubLinker(childByName);

  @Test
  public void start_search_from_enclosing_object() {
    Namespace root = new Namespace(info, "");
    Reference ref = RefFactory.create(info, "a");

    testee.link(ref, root);

    verify(childByName).get(eq(root), any(Designator.class), any(ElementInfo.class));
  }

  @Test
  public void uses_reference_for_child_search() {
    Namespace root = new Namespace(info, "");
    Reference ref = reference(info, new Designator("a", "b"));

    testee.link(ref, root);

    verify(childByName).get(any(Named.class), eq(new Designator("a", "b")), any(ElementInfo.class));
  }

  @Test
  public void use_info_from_reference_for_child_search() {
    Namespace root = new Namespace(info, "");

    ElementInfo info = new ElementInfo("", 42, 57);
    Reference ref = RefFactory.create(info, "a");

    testee.link(ref, root);

    verify(childByName).get(any(Named.class), any(Designator.class), eq(info));
  }

  @Test
  public void uses_target_from_search() {
    Namespace root = new Namespace(info, "");
    Reference ref = RefFactory.create(info, "a");
    Named result = mock(Named.class);
    when(childByName.get(any(Named.class), any(Designator.class), any(ElementInfo.class))).thenReturn(result);

    testee.link(ref, root);

    Assert.assertEquals(result, ref.link);
  }

  @Test
  public void removes_reference_offset() {
    Namespace root = new Namespace(info, "");
    Reference ref = reference(info, new Designator("a", "b"));

    testee.link(ref, root);

    Assert.assertEquals(0, ref.offset.size());
  }

  @Test
  public void removes_self_bevore_searching_for_children() {
    Namespace root = new Namespace(info, "me");
    Reference ref = RefFactory.create(info, "self");

    testee.link(ref, root);

    verify(childByName).get(any(Named.class), eq(new Designator()), any(ElementInfo.class));
  }

  @Test
  public void searches_for_child_when_taget_starts_with_self_and_has_more_elements() {
    Namespace root = new Namespace(info, "me");
    Reference ref = reference(info, new Designator("self", "a"));

    testee.link(ref, root);

    verify(childByName).get(any(Named.class), eq(new Designator("a")), any(ElementInfo.class));
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
