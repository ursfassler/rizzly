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
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.Test;

import ast.data.statement.Block;
import ast.data.statement.IfStatement;
import ast.data.variable.StateVariable;
import error.RizzlyError;

public class ParsersImplementation_Test {
  final private RizzlyError error = mock(RizzlyError.class);
  final private ParsersImplementation testee = new ParsersImplementation(error);

  @Test
  public void returns_nothing_for_unknown_name() {
    assertEquals(null, testee.parserFor("quixli quaxli"));
  }

  @Test
  public void returns_nothing_for_unknown_type() {
    assertEquals(null, testee.parserFor(IfStatement.class));
  }

  @Test
  public void return_parser_with_matching_name() {
    Parser parser = mock(Parser.class);
    when(parser.parserFor("the parser name")).thenReturn(parser);
    testee.add(parser);

    Parser found = testee.parserFor("the parser name");

    assertEquals(parser, found);
  }

  @Test
  public void return_parser_with_matching_parse_return_type() {
    Parser stateVariableParser = mock(Parser.class);
    when(stateVariableParser.parserFor(StateVariable.class)).thenReturn(stateVariableParser);
    testee.add(stateVariableParser);

    Parser blockParser = mock(Parser.class);
    when(blockParser.parserFor(Block.class)).thenReturn(blockParser);
    testee.add(blockParser);

    assertEquals(stateVariableParser, testee.parserFor(StateVariable.class));
    assertEquals(blockParser, testee.parserFor(Block.class));
  }

}
