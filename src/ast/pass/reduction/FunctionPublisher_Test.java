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

package ast.pass.reduction;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;

import org.junit.Test;

import ast.data.function.Function;
import ast.data.function.FunctionProperty;

public class FunctionPublisher_Test {
  final private FunctionPublisher testee = new FunctionPublisher();

  @Test
  public void changes_the_visibility_to_public() {
    Function function = mock(Function.class);
    function.property = FunctionProperty.Private;

    testee.visit(function);

    assertEquals(FunctionProperty.Public, function.property);
  }

}
