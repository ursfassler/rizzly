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

import ast.data.statement.Block;
import ast.data.statement.IfStatement;
import ast.data.variable.StateVariable;
import ast.meta.MetaList;
import ast.pass.input.xml.parser.Names;
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
  public void log_error_for_unknown_name() {
    try {
      testee.parserFor("quixli quaxli");
    } catch (XmlParseError e) {
    }

    verify(error).err(eq(ErrorType.Error), eq("unknown element \"quixli quaxli\""), any(MetaList.class));
  }

  @Test(expected = XmlParseError.class)
  public void throws_exception_for_unknown_type() {
    testee.parserFor(IfStatement.class);
  }

  @Test
  public void log_error_for_unknown_type() {
    try {
      testee.parserFor(IfStatement.class);
    } catch (XmlParseError e) {
    }

    verify(error).err(eq(ErrorType.Error), eq("unknown type \"IfStatement\""), any(MetaList.class));
  }

  @Test
  public void return_parser_with_matching_name() {
    Parser parser = mock(Parser.class);
    when(parser.names()).thenReturn(Names.list("the parser name"));
    testee.add(parser);

    Parser found = testee.parserFor("the parser name");

    assertEquals(parser, found);
  }

  @Test
  public void same_parser_is_registered_with_multiple_names() {
    Parser parser = mock(Parser.class);
    when(parser.names()).thenReturn(Names.list("name1", "name2"));
    testee.add(parser);

    Parser name1 = testee.parserFor("name1");
    Parser name2 = testee.parserFor("name2");

    assertEquals(parser, name1);
    assertEquals(parser, name2);
  }

  @Test
  public void return_parser_with_matching_parse_return_type() {
    Parser stateVariableParser = mock(Parser.class);
    when(stateVariableParser.type()).thenReturn((Class) StateVariable.class);
    testee.add(stateVariableParser);

    Parser blockParser = mock(Parser.class);
    when(blockParser.type()).thenReturn((Class) Block.class);
    testee.add(blockParser);

    assertEquals(stateVariableParser, testee.parserFor(StateVariable.class));
    assertEquals(blockParser, testee.parserFor(Block.class));
  }

  @Test
  public void can_not_add_parser_with_same_name_twice() {
    Parser parser1 = mock(Parser.class);
    when(parser1.names()).thenReturn(Names.list("the parser name"));
    when(parser1.type()).thenReturn((Class) Block.class);
    testee.add(parser1);
    Parser parser2 = mock(Parser.class);
    when(parser2.names()).thenReturn(Names.list("the parser name"));
    when(parser2.type()).thenReturn((Class) StateVariable.class);

    testee.add(parser2);

    verify(error).err(eq(ErrorType.Fatal), eq("parser with name \"the parser name\" already registered"), any(MetaList.class));
  }

  @Test
  public void can_not_add_parser_with_same_type_twice() {
    Parser parser1 = mock(Parser.class);
    when(parser1.names()).thenReturn(Names.list("the parser name 1"));
    when(parser1.type()).thenReturn((Class) Block.class);
    testee.add(parser1);
    Parser parser2 = mock(Parser.class);
    when(parser2.names()).thenReturn(Names.list("the parser name 2"));
    when(parser2.type()).thenReturn((Class) Block.class);

    testee.add(parser2);

    verify(error).err(eq(ErrorType.Fatal), eq("parser with type \"Block\" already registered"), any(MetaList.class));
  }
}
