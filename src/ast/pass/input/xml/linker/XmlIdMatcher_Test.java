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

package ast.pass.input.xml.linker;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Map;

import org.junit.Test;

import ast.data.Named;
import ast.meta.MetaList;
import error.ErrorType;
import error.RizzlyError;

public class XmlIdMatcher_Test {
  final private RizzlyError error = mock(RizzlyError.class);
  final private XmlIdMatcher testee = new XmlIdMatcher(error);

  @Test
  public void collects_the_targets() {
    LinkDummy link0 = mock(LinkDummy.class);
    LinkDummy link1 = mock(LinkDummy.class);
    LinkDummy link2 = mock(LinkDummy.class);

    testee.add(link0);
    testee.add(link1);
    testee.add(link2);

    assertEquals(3, testee.getTargets().size());
    assertTrue(testee.getTargets().contains(link0));
    assertTrue(testee.getTargets().contains(link1));
    assertTrue(testee.getTargets().contains(link2));
  }

  @Test
  public void collects_the_id_with_objects() {
    Named object = mock(Named.class);
    testee.register("the id", object);

    assertEquals(object, testee.getIdToObject().get("the id"));
  }

  @Test
  public void ignores_empty_ids() {
    Named object = mock(Named.class);

    testee.register("", object);

    assertEquals(0, testee.getIdToObject().size());
  }

  @Test
  public void can_not_add_the_same_id_twice() {
    Named object0 = mock(Named.class);
    MetaList meta0 = mock(MetaList.class);
    when(object0.metadata()).thenReturn(meta0);
    Named object1 = mock(Named.class);
    MetaList meta1 = mock(MetaList.class);
    when(object1.metadata()).thenReturn(meta1);
    testee.register("the id", object0);

    testee.register("the id", object1);

    verify(error).err(eq(ErrorType.Hint), eq("First defined here"), eq(meta0));
    verify(error).err(eq(ErrorType.Error), eq("Object with the id \"the id\" already registered"), eq(meta1));
    assertEquals(object0, testee.getIdToObject().get("the id"));
  }

  @Test
  public void matches_the_targets_with_the_objects() {
    Named object0 = mock(Named.class);
    Named object1 = mock(Named.class);

    LinkDummy link0 = mock(LinkDummy.class);
    LinkDummy link1 = mock(LinkDummy.class);
    when(link0.getName()).thenReturn("the id");
    when(link1.getName()).thenReturn("the id");

    testee.add(link0);
    testee.add(link1);
    testee.register("the id", object0);
    testee.register("the other id", object1);

    Map<LinkDummy, Named> mapping = testee.getMapping();

    assertEquals(2, mapping.size());
    assertEquals(object0, mapping.get(link0));
    assertEquals(object0, mapping.get(link1));
  }

  @Test
  public void need_to_have_an_object_for_every_target_in_the_end() {
    LinkDummy link = mock(LinkDummy.class);
    when(link.getName()).thenReturn("the other id");
    MetaList meta = mock(MetaList.class);
    when(link.metadata()).thenReturn(meta);
    testee.add(link);

    Map<LinkDummy, Named> mapping = testee.getMapping();

    assertEquals(0, mapping.size());
    verify(error).err(eq(ErrorType.Error), eq("no object with id \"the other id\" defined"), eq(meta));
  }
}
