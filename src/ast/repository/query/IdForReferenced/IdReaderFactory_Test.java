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

import org.junit.Assert;
import org.junit.Test;

import ast.data.AstList;
import ast.data.Namespace;
import ast.data.reference.RefItem;
import ast.data.reference.LinkedReferenceWithOffset_Implementation;
import ast.pass.output.xml.IdReader;

public class IdReaderFactory_Test {
  final private IdReaderFactory testee = new IdReaderFactory();

  final private Namespace root = new Namespace("root");
  final private Namespace item1 = new Namespace("item1");
  final private Namespace item2 = new Namespace("item2");
  final private Namespace item3 = new Namespace("item3");
  final private LinkedReferenceWithOffset_Implementation reference1 = new LinkedReferenceWithOffset_Implementation(item1, new AstList<RefItem>());
  final private LinkedReferenceWithOffset_Implementation reference2 = new LinkedReferenceWithOffset_Implementation(item3, new AstList<RefItem>());
  final private LinkedReferenceWithOffset_Implementation reference3 = new LinkedReferenceWithOffset_Implementation(root, new AstList<RefItem>());

  {
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

    Assert.assertNotEquals(null, reader);
  }

  @Test
  public void has_ids_for_referenced() {
    IdReader reader = testee.produce(root);

    Assert.assertTrue(reader.hasId(root));
    Assert.assertTrue(reader.hasId(item1));
    Assert.assertTrue(reader.hasId(item3));
  }

  @Test
  public void has_ids_are_in_increasing_order() {
    IdReader reader = testee.produce(root);

    Assert.assertEquals("0", reader.getId(root));
    Assert.assertEquals("1", reader.getId(item1));
    Assert.assertEquals("2", reader.getId(item3));
  }

  @Test
  public void has_no_additional_references() {
    IdReader reader = testee.produce(root);

    Assert.assertFalse(reader.hasId(item2));
    Assert.assertFalse(reader.hasId(reference1));
    Assert.assertFalse(reader.hasId(reference2));
    Assert.assertFalse(reader.hasId(reference3));
  }

}
