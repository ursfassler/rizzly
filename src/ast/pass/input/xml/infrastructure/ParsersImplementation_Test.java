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

package ast.pass.input.xml.infrastructure;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Test;

import ast.meta.MetaList;
import error.ErrorType;
import error.RizzlyError;

public class ParsersImplementation_Test {
  final private RizzlyError error = mock(RizzlyError.class);
  final private ParsersImplementation testee = new ParsersImplementation(error);

  @Test(expected = XmlParseError.class)
  public void throws_exception_for_unknown_element() {
    testee.parserFor("quixli quaxli");
  }

  @Test
  public void log_error_for_unknown_element() {
    try {
      testee.parserFor("quixli quaxli");
    } catch (XmlParseError e) {
    }

    verify(error).err(eq(ErrorType.Error), eq("unknown element \"quixli quaxli\""), any(MetaList.class));
  }

  @Test
  public void return_parser_with_matching_name() {
    Parser parser = mock(Parser.class);
    when(parser.name()).thenReturn("the parser name");
    testee.add(parser);

    Parser found = testee.parserFor("the parser name");

    assertEquals(parser, found);
  }

  @Test
  public void can_not_add_parser_with_same_name_twice() {
    Parser parser = mock(Parser.class);
    when(parser.name()).thenReturn("the parser name");
    testee.add(parser);

    testee.add(parser);

    verify(error).err(eq(ErrorType.Fatal), eq("parser with name \"the parser name\" already registered"), any(MetaList.class));
  }

}
