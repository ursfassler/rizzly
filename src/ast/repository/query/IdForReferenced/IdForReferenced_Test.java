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

package ast.repository.query.IdForReferenced;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.HashSet;
import java.util.Set;

import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

import ast.data.Ast;
import ast.data.AstList;
import ast.data.reference.LinkTarget;
import ast.data.reference.RefItem;
import ast.data.reference.LinkedReferenceWithOffset_Implementation;
import ast.repository.query.Referencees.ReferenceesReader;

public class IdForReferenced_Test {
  final private ReferenceesReader referenceesReader = mock(ReferenceesReader.class);
  final private IdGenerator idGenerator = mock(IdGenerator.class);
  final private IdForReferenced testee = new IdForReferenced(referenceesReader, idGenerator);
  final private Ast item = Mockito.mock(Ast.class);
  final private static Set<LinkedReferenceWithOffset_Implementation> SomeReference = new HashSet<LinkedReferenceWithOffset_Implementation>();

  {
    SomeReference.add(new LinkedReferenceWithOffset_Implementation(new LinkTarget(""), new AstList<RefItem>()));
  }

  @Test
  public void does_not_set_id_when_not_referenced() {
    testee.visit(item);

    Assert.assertEquals(false, testee.hasId(item));
  }

  @Test
  public void does_set_id_when_referenced() {
    Mockito.when(referenceesReader.getReferencees(item)).thenReturn(SomeReference);

    testee.visit(item);

    Assert.assertEquals(true, testee.hasId(item));
  }

  @Test
  public void get_id_from_IdGenerator() {
    when(referenceesReader.getReferencees(item)).thenReturn(SomeReference);
    when(idGenerator.newId(item)).thenReturn("the new id");

    testee.visit(item);

    Assert.assertEquals("the new id", testee.getId(item));
  }
}
