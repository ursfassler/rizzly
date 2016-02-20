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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.Test;

import ast.data.Namespace;
import ast.data.reference.LinkedAnchor;
import ast.data.reference.Reference;
import ast.pass.output.xml.IdReader;

public class IdReaderFactory_Test {
  final private IdReaderFactory testee = new IdReaderFactory();

  final private Namespace root = new Namespace("root");
  final private Namespace item1 = new Namespace("item1");
  final private Namespace item2 = new Namespace("item2");
  final private Namespace item3 = new Namespace("item3");
  final private LinkedAnchor anchor1 = mock(LinkedAnchor.class);
  final private Reference reference1 = mock(Reference.class);
  final private LinkedAnchor anchor2 = mock(LinkedAnchor.class);
  final private Reference reference2 = mock(Reference.class);
  final private LinkedAnchor anchor3 = mock(LinkedAnchor.class);
  final private Reference reference3 = mock(Reference.class);

  {
    when(anchor1.getLink()).thenReturn(item1);
    when(reference1.getAnchor()).thenReturn(anchor1);
    when(anchor2.getLink()).thenReturn(item3);
    when(reference2.getAnchor()).thenReturn(anchor2);
    when(anchor3.getLink()).thenReturn(root);
    when(reference3.getAnchor()).thenReturn(anchor3);

    root.children.add(item1);
    root.children.add(reference1);
    root.children.add(reference2);
    root.children.add(item2);
    root.children.add(item3);
    root.children.add(reference3);
  }

  @Test
  public void produces_an_IdReader() {
    IdReader reader = testee.produce(root);

    assertNotEquals(null, reader);
  }

  @Test
  public void has_ids_for_referenced() {
    IdReader reader = testee.produce(root);

    assertTrue(reader.hasId(root));
    assertTrue(reader.hasId(item1));
    assertTrue(reader.hasId(item3));
  }

  @Test
  public void has_ids_are_in_increasing_order() {
    IdReader reader = testee.produce(root);

    assertEquals("0", reader.getId(root));
    assertEquals("1", reader.getId(item1));
    assertEquals("2", reader.getId(item3));
  }

  @Test
  public void has_no_additional_references() {
    IdReader reader = testee.produce(root);

    assertFalse(reader.hasId(item2));
    assertFalse(reader.hasId(reference1));
    assertFalse(reader.hasId(reference2));
    assertFalse(reader.hasId(reference3));
  }

}
