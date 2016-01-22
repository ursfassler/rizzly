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

import java.util.HashSet;
import java.util.Set;

import org.junit.Assert;
import org.junit.Test;

import ast.data.AstList;
import ast.data.Namespace;
import ast.data.reference.RefItem;
import ast.data.reference.LinkedReferenceWithOffset_Implementation;

public class ReferenceesFactory_Tests {
  private static final Set<LinkedReferenceWithOffset_Implementation> EmptySet = new HashSet<LinkedReferenceWithOffset_Implementation>();

  final private ReferenceesFactory testee = new ReferenceesFactory();

  final private Namespace root = new Namespace("root");
  final private Namespace item1 = new Namespace("item1");
  final private Namespace item2 = new Namespace("item2");
  final private Namespace item3 = new Namespace("item3");
  final private LinkedReferenceWithOffset_Implementation reference1 = new LinkedReferenceWithOffset_Implementation(item1, new AstList<RefItem>());
  final private LinkedReferenceWithOffset_Implementation reference2 = new LinkedReferenceWithOffset_Implementation(item3, new AstList<RefItem>());

  {
    root.children.add(item1);
    root.children.add(reference1);
    root.children.add(reference2);
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
    Assert.assertNotEquals(null, reader.getReferencees(reference1));
    Assert.assertNotEquals(null, reader.getReferencees(reference2));
  }

  @Test
  public void has_references() {
    ReferenceesReader reader = testee.produce(root);

    Assert.assertEquals(set(reference1), reader.getReferencees(item1));
    Assert.assertEquals(set(reference2), reader.getReferencees(item3));
  }

  @Test
  public void has_no_additional_references() {
    ReferenceesReader reader = testee.produce(root);

    Assert.assertEquals(EmptySet, reader.getReferencees(root));
    Assert.assertEquals(EmptySet, reader.getReferencees(item2));
    Assert.assertEquals(EmptySet, reader.getReferencees(reference1));
    Assert.assertEquals(EmptySet, reader.getReferencees(reference2));
  }

  private Set<LinkedReferenceWithOffset_Implementation> set(LinkedReferenceWithOffset_Implementation reference) {
    Set<LinkedReferenceWithOffset_Implementation> ret = new HashSet<LinkedReferenceWithOffset_Implementation>();
    ret.add(reference);
    return ret;
  }
}
