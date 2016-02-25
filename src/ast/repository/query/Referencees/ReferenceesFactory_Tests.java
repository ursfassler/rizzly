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

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.HashSet;
import java.util.Set;

import org.junit.Assert;
import org.junit.Test;

import ast.data.Namespace;
import ast.data.reference.LinkedAnchor;
import ast.data.reference.Reference;

public class ReferenceesFactory_Tests {
  private static final Set<Reference> EmptySet = new HashSet<Reference>();

  final private ReferenceesFactory testee = new ReferenceesFactory();

  final private Namespace root = new Namespace("root");
  final private Namespace item1 = new Namespace("item1");
  final private Namespace item2 = new Namespace("item2");
  final private Namespace item3 = new Namespace("item3");
  final private LinkedAnchor anchor1 = mock(LinkedAnchor.class);
  final private LinkedAnchor anchor2 = mock(LinkedAnchor.class);

  {
    when(anchor1.getLink()).thenReturn(item1);
    when(anchor2.getLink()).thenReturn(item3);

    root.children.add(item1);
    root.children.add(anchor1);
    root.children.add(anchor2);
    root.children.add(item2);
    root.children.add(item3);
  }

  @Test
  public void produces_a_ReferenceesReader() {
    ReferenceesReader reader = testee.produce(root);

    Assert.assertNotEquals(null, reader);
  }

  @Test
  public void reader_contains_all_nodes() {
    ReferenceesReader reader = testee.produce(root);

    Assert.assertNotEquals(null, reader.getReferencees(root));
    Assert.assertNotEquals(null, reader.getReferencees(item1));
    Assert.assertNotEquals(null, reader.getReferencees(item2));
    Assert.assertNotEquals(null, reader.getReferencees(item3));
    Assert.assertNotEquals(null, reader.getReferencees(anchor1));
    Assert.assertNotEquals(null, reader.getReferencees(anchor2));
  }

  @Test
  public void has_references() {
    ReferenceesReader reader = testee.produce(root);

    Assert.assertEquals(set(anchor1), reader.getReferencees(item1));
    Assert.assertEquals(set(anchor2), reader.getReferencees(item3));
  }

  @Test
  public void has_no_additional_references() {
    ReferenceesReader reader = testee.produce(root);

    Assert.assertEquals(EmptySet, reader.getReferencees(root));
    Assert.assertEquals(EmptySet, reader.getReferencees(item2));
    Assert.assertEquals(EmptySet, reader.getReferencees(anchor1));
    Assert.assertEquals(EmptySet, reader.getReferencees(anchor2));
  }

  private Set<LinkedAnchor> set(LinkedAnchor reference) {
    Set<LinkedAnchor> ret = new HashSet<LinkedAnchor>();
    ret.add(reference);
    return ret;
  }
}
