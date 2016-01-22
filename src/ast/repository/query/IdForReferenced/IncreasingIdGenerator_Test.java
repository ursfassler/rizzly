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

import org.junit.Assert;
import org.junit.Test;

import ast.data.Ast;

public class IncreasingIdGenerator_Test {
  final private IncreasingIdGenerator testee = new IncreasingIdGenerator();
  final private Ast item = mock(Ast.class);

  @Test
  public void produces_increasing_ids_from_0() {
    Assert.assertEquals("0", testee.newId(item));
    Assert.assertEquals("1", testee.newId(item));
    Assert.assertEquals("2", testee.newId(item));
    Assert.assertEquals("3", testee.newId(item));
    Assert.assertEquals("4", testee.newId(item));
    Assert.assertEquals("5", testee.newId(item));
    Assert.assertEquals("6", testee.newId(item));
    Assert.assertEquals("7", testee.newId(item));
    Assert.assertEquals("8", testee.newId(item));
    Assert.assertEquals("9", testee.newId(item));
    Assert.assertEquals("10", testee.newId(item));
    Assert.assertEquals("11", testee.newId(item));
    Assert.assertEquals("12", testee.newId(item));
    Assert.assertEquals("13", testee.newId(item));
    Assert.assertEquals("14", testee.newId(item));
    Assert.assertEquals("15", testee.newId(item));
  }
}
