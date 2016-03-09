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
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Test;

import ast.data.reference.Anchor;
import ast.data.statement.Block;
import ast.pass.input.xml.scanner.ExpectionParser;
import error.RizzlyError;

public class ParserDispatcher_Test {
  final private Parsers parsers = mock(Parsers.class);
  final private ExpectionParser stream = mock(ExpectionParser.class);
  final private XmlParser parser = mock(XmlParser.class);
  final private RizzlyError error = mock(RizzlyError.class);
  final private ParserDispatcher testee = new ParserDispatcher(Anchor.class, parsers, stream, parser, error);

  @Test
  public void forwards_request_for_parser_by_name_to_parsers() {
    Parser parser = mock(Parser.class);
    when(parsers.parserFor("another name")).thenReturn(parser);

    assertEquals(parser, testee.parserFor("another name"));
  }

  @Test
  public void forwards_request_for_parser_by_type_to_parsers() {
    Parser parser = mock(Parser.class);
    when(parsers.parserFor(Block.class)).thenReturn(parser);

    assertEquals(parser, testee.parserFor(Block.class));
  }

  @Test
  public void returns_itself_if_request_for_parser_with_type_that_handles_this_class() {
    assertEquals(testee, testee.parserFor(Anchor.class));
  }

  @Test
  public void uses_parsers_to_parse_next_element() {
    Parser parser = mock(Parser.class);
    Anchor item = mock(Anchor.class);
    when(stream.peekElement()).thenReturn("next element");
    when(parsers.parserFor(anyString())).thenReturn(parser);
    when(parser.parse()).thenReturn(item);

    assertEquals(item, testee.parse());

    verify(parsers).parserFor(eq("next element"));
    verify(parser).parse();
  }

}
