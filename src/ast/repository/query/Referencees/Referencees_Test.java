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

import ast.data.Ast;
import ast.data.Named;
import ast.data.reference.LinkedAnchor;

public class Referencees_Test {
  final private Referencees testee = new Referencees();
  private static final Set<Ast> EmptySet = new HashSet<Ast>();

  final private Named target = mock(Named.class);
  final private LinkedAnchor anchor1 = mock(LinkedAnchor.class);
  final private LinkedAnchor anchor2 = mock(LinkedAnchor.class);

  {
    when(anchor1.getLink()).thenReturn(target);
    when(anchor2.getLink()).thenReturn(target);
  }

  @Test
  public void returns_nothing_if_object_is_not_in_container() {
    Named target = mock(Named.class);

    Assert.assertEquals(null, testee.getReferencees(target));
  }

  @Test
  public void returns_empty_when_no_anchors_are_added() {
    Named target = mock(Named.class);

    testee.addTarget(target);

    Assert.assertEquals(EmptySet, testee.getReferencees(target));
  }

  @Test
  public void returns_added_anchors() {
    testee.addReferencee(anchor1);

    Assert.assertEquals(set(anchor1), testee.getReferencees(target));
  }

  @Test
  public void returns_multiple_added_anchors() {
    testee.addReferencee(anchor1);
    testee.addReferencee(anchor2);

    Assert.assertEquals(set(anchor1, anchor2), testee.getReferencees(target));
  }

  @Test
  public void adding_a_target_does_not_clear_anchors_for_this_target() {
    testee.addReferencee(anchor1);

    testee.addTarget(target);

    Assert.assertEquals(set(anchor1), testee.getReferencees(target));
  }

  private Set<LinkedAnchor> set(LinkedAnchor referencee1, LinkedAnchor referencee2) {
    Set<LinkedAnchor> ret = set(referencee1);
    ret.add(referencee2);
    return ret;
  }

  private Set<LinkedAnchor> set(LinkedAnchor reference) {
    Set<LinkedAnchor> ret = new HashSet<LinkedAnchor>();
    ret.add(reference);
    return ret;
  }
}
