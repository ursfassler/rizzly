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

package ast.pass.input.xml.parser.statement;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.Test;
import org.mockito.InOrder;
import org.mockito.Mockito;

import ast.data.AstList;
import ast.data.expression.Expression;
import ast.data.statement.Block;
import ast.data.statement.Statement;
import ast.pass.input.xml.infrastructure.XmlParser;
import ast.pass.input.xml.scanner.ExpectionParser;
import error.RizzlyError;

public class BlockParser_Test {
  final private ExpectionParser stream = mock(ExpectionParser.class);
  final private XmlParser parser = mock(XmlParser.class);
  final private RizzlyError error = mock(RizzlyError.class);
  final private BlockParser testee = new BlockParser(stream, parser, error);
  final private InOrder order = Mockito.inOrder(stream, parser);

  @Test
  public void returns_itself_for_correct_name() {
    assertEquals(testee, testee.parserFor("Block"));
  }

  @Test
  public void returns_nothing_for_wrong_name() {
    assertEquals(null, testee.parserFor(""));
  }

  @Test
  public void returns_itself_for_correct_type() {
    assertEquals(testee, testee.parserFor(Block.class));
  }

  @Test
  public void returns_nothing_for_wrong_type() {
    assertEquals(null, testee.parserFor(Expression.class));
  }

  @Test
  public void parse_Block() {
    Statement statement1 = mock(Statement.class);
    Statement statement2 = mock(Statement.class);
    AstList<Statement> statements = new AstList<Statement>();
    statements.add(statement1);
    statements.add(statement2);
    when(parser.itemsOf(Statement.class)).thenReturn(statements);

    Block block = testee.parse();

    assertEquals(statements, block.statements);

    order.verify(stream).elementStart(eq("Block"));
    order.verify(parser).itemsOf(eq(Statement.class));
    order.verify(stream).elementEnd();
  }

}
