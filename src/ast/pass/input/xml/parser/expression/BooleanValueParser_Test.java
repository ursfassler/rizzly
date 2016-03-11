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

package ast.pass.input.xml.parser.expression;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Test;
import org.mockito.InOrder;
import org.mockito.Mockito;

import ast.data.expression.value.BooleanValue;
import ast.pass.input.xml.infrastructure.XmlParseError;
import ast.pass.input.xml.infrastructure.XmlParser;
import ast.pass.input.xml.parser.Names;
import ast.pass.input.xml.scanner.ExpectionParser;
import error.ErrorType;
import error.RizzlyError;

public class BooleanValueParser_Test {
  final private ExpectionParser stream = mock(ExpectionParser.class);
  final private XmlParser parser = mock(XmlParser.class);
  final private RizzlyError error = mock(RizzlyError.class);
  final private BooleanValueParser testee = new BooleanValueParser(stream, parser, error);
  final private InOrder order = Mockito.inOrder(stream, parser);

  @Test
  public void has_correct_name() {
    assertEquals(Names.list("BooleanValue"), testee.names());
  }

  @Test
  public void has_correct_type() {
    assertEquals(BooleanValue.class, testee.type());
  }

  @Test
  public void parse_BooleanValue() {
    when(stream.attribute(eq("value"))).thenReturn("True");

    BooleanValue value = testee.parse();

    assertEquals(true, value.value);

    order.verify(stream).elementStart(eq("BooleanValue"));
    order.verify(stream).attribute(eq("value"));
    order.verify(stream).elementEnd();
  }

  @Test
  public void parse_valid_values() {
    when(stream.attribute(eq("value"))).thenReturn("True").thenReturn("False");

    assertEquals(true, testee.parse().value);
    assertEquals(false, testee.parse().value);
  }

  @Test
  public void reports_an_error_when_the_value_is_not_a_boolean_value() {
    when(stream.attribute(eq("value"))).thenReturn("0");

    try {
      testee.parse();
    } catch (XmlParseError e) {
    }

    // TODO emit xml position
    verify(error).err(eq(ErrorType.Error), eq("attribute value does not contain a boolean value: \"0\""), any());
  }

  @Test(expected = XmlParseError.class)
  public void throws_an_exception_when_the_value_is_not_a_number() {
    when(stream.attribute(eq("value"))).thenReturn("yes");

    testee.parse();
  }

}
