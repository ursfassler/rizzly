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
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collection;

import org.junit.Test;

import ast.data.reference.Anchor;
import ast.data.reference.LinkedAnchor;
import ast.data.reference.UnlinkedAnchor;
import ast.data.statement.Block;
import ast.meta.MetaList;
import ast.pass.input.xml.parser.Names;
import ast.pass.input.xml.scanner.ExpectionParser;
import error.ErrorType;
import error.RizzlyError;

public class ParserDispatcher_Test {
  final private Parsers parsers = mock(Parsers.class);
  final private ExpectionParser stream = mock(ExpectionParser.class);
  final private XmlParser parser = mock(XmlParser.class);
  final private RizzlyError error = mock(RizzlyError.class);
  final private ParserDispatcher testee = new ParserDispatcher(Anchor.class, parsers, stream, parser, error);

  @Test
  public void forwards_name_request_to_parsers() {
    Collection<String> names = Names.list("one name", "another one");
    when(parsers.names()).thenReturn(names);

    assertEquals(names, testee.names());
  }

  @Test
  public void returns_the_type_as_specified() {
    assertEquals(Anchor.class, testee.type());
  }

  @Test
  public void can_not_add_parsers_with_incompatible_type() {
    Parser incompatibleParser = mock(Parser.class);
    when(incompatibleParser.type()).thenReturn((Class) Block.class);

    testee.add(incompatibleParser);

    verify(error).err(eq(ErrorType.Fatal), eq("Can not add parser (type Block is not a subtype of Anchor)"), any(MetaList.class));
    verify(parsers, never()).add(any(Parser.class));
  }

  @Test
  public void can_add_parsers_when_types_are_subtypes() {
    Parser unlinkedParser = mock(Parser.class);
    Parser linkedParser = mock(Parser.class);
    when(unlinkedParser.type()).thenReturn((Class) UnlinkedAnchor.class);
    when(linkedParser.type()).thenReturn((Class) LinkedAnchor.class);

    testee.add(unlinkedParser);
    testee.add(linkedParser);

    verify(error, never()).err(any(ErrorType.class), anyString(), any(MetaList.class));
    verify(parsers).add(eq(unlinkedParser));
    verify(parsers).add(eq(linkedParser));
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
