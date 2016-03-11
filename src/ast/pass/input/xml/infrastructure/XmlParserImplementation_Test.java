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
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Test;
import org.mockito.InOrder;
import org.mockito.Mockito;

import ast.data.Ast;
import ast.data.reference.Reference;
import ast.data.statement.Block;
import ast.pass.input.xml.scanner.ExpectionParser;
import error.ErrorType;
import error.RizzlyError;

public class XmlParserImplementation_Test {
  final private ExpectionParser stream = mock(ExpectionParser.class);
  final private Parsers parsers = mock(Parsers.class);
  final private RizzlyError error = mock(RizzlyError.class);
  final private Parser parser = mock(Parser.class);
  final private Ast ast = mock(Ast.class);
  final private XmlParserImplementation testee = new XmlParserImplementation(stream, parsers, error);
  final private InOrder order = Mockito.inOrder(stream, parsers, parser);

  @Test
  public void can_add_parser() {
    Parser parser = mock(Parser.class);

    testee.add(parser);

    verify(parsers).add(eq(parser));
  }

  @Test
  public void parse_ast_item_dispatches_to_the_correct_parser() {
    when(stream.peekElement()).thenReturn("the next element");
    when(parsers.parserFor(eq("the next element"))).thenReturn(parser);
    when(parser.parse()).thenReturn(ast);

    Ast item = testee.anyItem();

    assertEquals(ast, item);

    order.verify(stream).peekElement();
    order.verify(parsers).parserFor(eq("the next element"));
    order.verify(parser).parse();
  }

  @Test
  public void reports_an_error_if_the_parser_for_the_next_item_is_missing() {
    when(stream.peekElement()).thenReturn("the next element");
    when(parsers.parserFor(eq("the next element"))).thenReturn(null);

    try {
      testee.anyItem();
    } catch (XmlParseError e) {
    }

    verify(error).err(eq(ErrorType.Fatal), eq("parser not found for: the next element"), any());
  }

  @Test(expected = XmlParseError.class)
  public void throw_an_error_if_the_parser_for_the_next_item_is_missing() {
    when(stream.peekElement()).thenReturn("the next element");
    when(parsers.parserFor(eq("the next element"))).thenReturn(null);

    testee.anyItem();
  }

  @Test
  public void parsing_ast_items_return_zero_if_there_are_no_more_elements() {
    when(stream.hasElement()).thenReturn(false);

    assertEquals(0, testee.anyItems().size());
  }

  @Test
  public void parsing_ast_items_returns_all_items() {
    when(stream.hasElement()).thenReturn(true).thenReturn(true).thenReturn(false);
    when(parsers.parserFor(anyString())).thenReturn(parser);
    when(parser.parse()).thenReturn(ast);

    assertEquals(2, testee.anyItems().size());
  }

  @Test
  public void parse_for_a_specific_type() {
    Reference reference = mock(Reference.class);
    when(parsers.parserFor(Reference.class)).thenReturn(parser);
    when(parser.parse()).thenReturn(reference);

    Reference item = testee.itemOf(Reference.class);

    assertEquals(reference, item);
  }

  @Test
  public void reports_an_error_if_a_parser_is_missing() {
    when(parsers.parserFor(any(Class.class))).thenReturn(null);

    try {
      testee.itemOf(Reference.class);
    } catch (XmlParseError e) {
    }

    verify(error).err(eq(ErrorType.Fatal), eq("parser not found for: Reference"), any());
  }

  @Test(expected = XmlParseError.class)
  public void throw_an_error_if_a_parser_is_missing() {
    when(parsers.parserFor(any(Class.class))).thenReturn(null);

    testee.itemOf(Reference.class);
  }

  @Test
  public void reports_an_error_if_a_parser_is_missing_for_multiple_items() {
    when(parsers.parserFor(any(Class.class))).thenReturn(null);

    try {
      testee.itemsOf(Reference.class);
    } catch (XmlParseError e) {
    }

    verify(error).err(eq(ErrorType.Fatal), eq("parser not found for: Reference"), any());
  }

  @Test(expected = XmlParseError.class)
  public void throw_an_error_if_a_parser_is_missing_for_multiple_items() {
    when(parsers.parserFor(any(Class.class))).thenReturn(null);

    testee.itemsOf(Reference.class);
  }

  @Test
  public void parsing_for_specific_items_return_zero_if_there_are_no_more_elements() {
    when(parsers.parserFor(Block.class)).thenReturn(parser);
    when(stream.hasElement()).thenReturn(false);

    assertEquals(0, testee.itemsOf(Block.class).size());
  }

  @Test
  public void parsing_for_specific_items_returns_all_until_the_first_non_matching_element() {
    Block block = mock(Block.class);
    when(stream.hasElement()).thenReturn(true);
    when(stream.peekElement()).thenReturn("next element").thenReturn("quixli");
    when(parsers.parserFor(Block.class)).thenReturn(parser);
    when(parser.parserFor("next element")).thenReturn(parser);
    when(parser.parse()).thenReturn(block);

    assertEquals(1, testee.itemsOf(Block.class).size());
  }

  @Test
  public void parsing_for_specific_items_returns_all_elements() {
    Block block = mock(Block.class);
    when(stream.hasElement()).thenReturn(true).thenReturn(true).thenReturn(false);
    when(stream.peekElement()).thenReturn("next element");
    when(parsers.parserFor(Block.class)).thenReturn(parser);
    when(parser.parserFor("next element")).thenReturn(parser);
    when(parser.parse()).thenReturn(block);

    assertEquals(2, testee.itemsOf(Block.class).size());
  }

  @Test
  public void id_returns_the_id_attribute_when_available() {
    when(stream.attribute(eq("id"), any())).thenReturn("the id");

    assertEquals("the id", testee.id());
  }

  @Test
  public void id_returns_an_empty_string_when_id_is_not_available() {
    testee.id();

    verify(stream).attribute(eq("id"), eq(""));
  }
}
