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

import org.junit.Assert;
import org.junit.Test;

public class NamespacePrefix_Test {

  @Test
  public void returns_the_prefix_of_namepsaces() {
    Assert.assertEquals("meta", NamespacePrefix.get("http://www.bitzgi.ch/2016/rizzly/test/meta"));
    Assert.assertEquals("world", NamespacePrefix.get("hello/world"));
    Assert.assertEquals("hello", NamespacePrefix.get("hello"));
  }
}
