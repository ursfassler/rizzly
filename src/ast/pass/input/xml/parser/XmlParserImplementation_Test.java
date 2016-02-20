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

package ast.pass.input.xml.parser;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.Test;
import org.mockito.InOrder;
import org.mockito.Mockito;

import ast.data.Ast;
import ast.pass.input.xml.infrastructure.Parser;
import ast.pass.input.xml.infrastructure.Parsers;
import ast.pass.input.xml.scanner.ExpectionParser;

public class XmlParserImplementation_Test {
  final private ExpectionParser stream = mock(ExpectionParser.class);
  final private Parsers parsers = mock(Parsers.class);
  final private Parser parser = mock(Parser.class);
  final private Ast ast = mock(Ast.class);
  final private XmlParserImplementation testee = new XmlParserImplementation(stream, parsers);
  final private InOrder order = Mockito.inOrder(stream, parsers, parser);

  @Test
  public void parse_ast_item_dispatches_to_the_correct_parser() {
    when(stream.peekElement()).thenReturn("the next element");
    when(parsers.parserFor(eq("the next element"))).thenReturn(parser);
    when(parser.parse()).thenReturn(ast);

    Ast item = testee.astItem();

    assertEquals(ast, item);

    order.verify(stream).peekElement();
    order.verify(parsers).parserFor(eq("the next element"));
    order.verify(parser).parse();
  }

  @Test
  public void parsing_ast_items_return_zero_if_there_are_no_more_elements() {
    when(stream.hasElement()).thenReturn(false);

    assertEquals(0, testee.astItems().size());
  }

  @Test
  public void parsing_ast_items_returns_all_items() {
    when(stream.hasElement()).thenReturn(true).thenReturn(true).thenReturn(false);
    when(parsers.parserFor(anyString())).thenReturn(parser);
    when(parser.parse()).thenReturn(ast);

    assertEquals(2, testee.astItems().size());
  }

}