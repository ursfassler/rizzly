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

import java.util.HashSet;
import java.util.Set;

import org.junit.Assert;
import org.junit.Test;

import ast.data.Ast;
import ast.data.AstList;
import ast.data.Named;
import ast.data.reference.RefItem;
import ast.data.reference.LinkedReferenceWithOffset_Implementation;

public class Referencees_Test {
  final private Referencees testee = new Referencees();
  private static final Set<Ast> EmptySet = new HashSet<Ast>();

  @Test
  public void returns_nothing_if_object_is_not_in_container() {
    Named target = mock(Named.class);

    Assert.assertEquals(null, testee.getReferencees(target));
  }

  @Test
  public void returns_empty_when_no_referencees_are_added() {
    Named target = mock(Named.class);

    testee.addTarget(target);

    Assert.assertEquals(EmptySet, testee.getReferencees(target));
  }

  @Test
  public void returns_added_reference() {
    Named target = mock(Named.class);
    LinkedReferenceWithOffset_Implementation referencee = new LinkedReferenceWithOffset_Implementation(target, new AstList<RefItem>());

    testee.addReferencee(referencee);

    Assert.assertEquals(set(referencee), testee.getReferencees(target));
  }

  @Test
  public void returns_multiple_added_reference() {
    Named target = mock(Named.class);
    LinkedReferenceWithOffset_Implementation referencee1 = new LinkedReferenceWithOffset_Implementation(target, new AstList<RefItem>());
    LinkedReferenceWithOffset_Implementation referencee2 = new LinkedReferenceWithOffset_Implementation(target, new AstList<RefItem>());

    testee.addReferencee(referencee1);
    testee.addReferencee(referencee2);

    Assert.assertEquals(set(referencee1, referencee2), testee.getReferencees(target));
  }

  @Test
  public void adding_a_target_does_not_clear_refrencees_for_this_target() {
    Named target = mock(Named.class);
    LinkedReferenceWithOffset_Implementation referencee = new LinkedReferenceWithOffset_Implementation(target, new AstList<RefItem>());
    testee.addReferencee(referencee);

    testee.addTarget(target);

    Assert.assertEquals(set(referencee), testee.getReferencees(target));
  }

  private Set<LinkedReferenceWithOffset_Implementation> set(LinkedReferenceWithOffset_Implementation referencee1, LinkedReferenceWithOffset_Implementation referencee2) {
    Set<LinkedReferenceWithOffset_Implementation> ret = set(referencee1);
    ret.add(referencee2);
    return ret;
  }

  private Set<LinkedReferenceWithOffset_Implementation> set(LinkedReferenceWithOffset_Implementation reference) {
    Set<LinkedReferenceWithOffset_Implementation> ret = new HashSet<LinkedReferenceWithOffset_Implementation>();
    ret.add(reference);
    return ret;
  }
}
