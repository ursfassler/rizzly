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

import org.junit.Test;

import ast.data.reference.Anchor;
import ast.data.reference.LinkedAnchor;
import ast.data.reference.UnlinkedAnchor;
import ast.data.statement.Block;
import ast.meta.MetaList;
import ast.pass.input.xml.scanner.ExpectionParser;
import error.ErrorType;
import error.RizzlyError;

public class ParserDispatcher_Test {
  final private ExpectionParser stream = mock(ExpectionParser.class);
  final private XmlParser parser = mock(XmlParser.class);
  final private RizzlyError error = mock(RizzlyError.class);
  final private ParserDispatcher testee = new ParserDispatcher(Anchor.class, stream, parser, error);

  @Test
  public void name_is_empty_when_no_parser_is_added() {
    assertEquals(0, testee.names().size());
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
  }
  // @Test
  // public void parse_global_constant() {
  // when(stream.attribute(eq("name"))).thenReturn("the variable name");
  // when(parser.itemOf(Reference.class)).thenReturn(reference);
  // when(parser.itemOf(Expression.class)).thenReturn(expression);
  //
  // GlobalConstant globalConstant = testee.parse();
  //
  // assertEquals("the variable name", globalConstant.getName());
  // assertEquals(reference, globalConstant.type);
  // assertEquals(expression, globalConstant.def);
  //
  // order.verify(stream).elementStart(eq("GlobalConstant"));
  // order.verify(stream).attribute(eq("name"));
  // order.verify(parser).itemOf(eq(Reference.class));
  // order.verify(parser).itemOf(eq(Expression.class));
  // order.verify(stream).elementEnd();
  // }

}
