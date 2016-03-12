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

package ast.pass.output.xml;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class NamespaceCollector_Test {
  private final NamespaceCollector testee = new NamespaceCollector();

  @Test
  public void has_initially_no_namespaces() {
    assertEquals(0, testee.getNamespaces().size());
  }

  @Test
  public void records_namespace() {
    testee.beginNode("the namespace", null);

    assertEquals(1, testee.getNamespaces().size());
    assertTrue(testee.getNamespaces().contains("the namespace"));
  }

  @Test
  public void records_multiple_namespaces() {
    testee.beginNode("1", null);
    testee.beginNode("2", null);
    testee.beginNode("3", null);
    testee.beginNode("4", null);
    testee.beginNode("5", null);

    assertEquals(5, testee.getNamespaces().size());
    assertTrue(testee.getNamespaces().contains("1"));
    assertTrue(testee.getNamespaces().contains("2"));
    assertTrue(testee.getNamespaces().contains("3"));
    assertTrue(testee.getNamespaces().contains("4"));
    assertTrue(testee.getNamespaces().contains("5"));
  }

  @Test
  public void does_not_record_duplicates() {
    testee.beginNode("1", null);
    testee.beginNode("2", null);
    testee.beginNode("1", null);
    testee.beginNode("2", null);
    testee.beginNode("1", null);

    assertEquals(2, testee.getNamespaces().size());
    assertTrue(testee.getNamespaces().contains("1"));
    assertTrue(testee.getNamespaces().contains("2"));
  }
}
