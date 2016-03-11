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

package ast.pass.input.xml.parser.meta;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Test;
import org.mockito.InOrder;
import org.mockito.Mockito;

import ast.meta.SourcePosition;
import ast.pass.input.xml.infrastructure.XmlParseError;
import ast.pass.input.xml.scanner.ExpectionParser;
import error.ErrorType;
import error.RizzlyError;

public class SourcePositionParser_Test {
  final private ExpectionParser stream = mock(ExpectionParser.class);
  final private RizzlyError error = mock(RizzlyError.class);
  final private SourcePositionParser testee = new SourcePositionParser(stream, error);
  final private InOrder order = Mockito.inOrder(stream);

  @Test
  public void returns_itself_for_correct_name() {
    assertEquals("SourcePosition", testee.getElementName());
  }

  @Test
  public void parse_SourcePositionParser() {
    when(stream.attribute(eq("filename"))).thenReturn("the file name");
    when(stream.attribute(eq("line"))).thenReturn("3");
    when(stream.attribute(eq("row"))).thenReturn("5");

    SourcePosition position = testee.parse();

    assertEquals("the file name", position.filename);
    assertEquals(3, position.line);
    assertEquals(5, position.row);

    order.verify(stream).elementStart(eq("SourcePosition"));
    order.verify(stream).attribute(eq("filename"));
    order.verify(stream).attribute(eq("line"));
    order.verify(stream).attribute(eq("row"));
    order.verify(stream).elementEnd();
  }

  @Test
  public void reports_an_error_when_the_expected_number_is_not_a_number() {
    when(stream.attribute(eq("line"))).thenReturn("lala");
    when(stream.attribute(eq("row"))).thenReturn("0");

    try {
      testee.parse();
    } catch (Error e) {
    }

    verify(error).err(eq(ErrorType.Error), eq("can not convert attribute to int: \"lala\""), any());
  }

  @Test(expected = XmlParseError.class)
  public void throws_an_error_when_the_expected_number_is_not_a_number() {
    when(stream.attribute(eq("line"))).thenReturn("0");
    when(stream.attribute(eq("row"))).thenReturn("lala");

    testee.parse();
  }
}
