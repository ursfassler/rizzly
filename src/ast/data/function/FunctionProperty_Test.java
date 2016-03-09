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

package ast.data.function;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class FunctionProperty_Test {

  @Test
  public void convert_to_string() {
    assertEquals("private", FunctionProperty.Private.toString());
    assertEquals("public", FunctionProperty.Public.toString());
    assertEquals("extern", FunctionProperty.External.toString());
  }

  @Test
  public void parse_string() {
    assertEquals(FunctionProperty.Private, FunctionProperty.parse("private"));
    assertEquals(FunctionProperty.Public, FunctionProperty.parse("public"));
    assertEquals(FunctionProperty.External, FunctionProperty.parse("extern"));
  }

  @Test(expected = IllegalArgumentException.class)
  public void throw_exception_if_value_is_not_parsable() {
    FunctionProperty.parse("blabla");
  }

}
