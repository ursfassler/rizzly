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

package ast.pass.input.xml.parser.reference;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.Test;
import org.mockito.InOrder;
import org.mockito.Mockito;

import ast.data.expression.Expression;
import ast.data.reference.UnlinkedAnchor;
import ast.pass.input.xml.infrastructure.XmlParser;
import ast.pass.input.xml.scanner.ExpectionParser;
import error.RizzlyError;

public class UnlinkedAnchorParser_Test {
  final private ExpectionParser stream = mock(ExpectionParser.class);
  final private XmlParser parser = mock(XmlParser.class);
  final private RizzlyError error = mock(RizzlyError.class);
  final private UnlinkedAnchorParser testee = new UnlinkedAnchorParser(stream, parser, error);
  final private InOrder order = Mockito.inOrder(stream, parser);

  @Test
  public void returns_itself_for_correct_name() {
    assertEquals(testee, testee.parserFor("UnlinkedAnchor"));
  }

  @Test
  public void returns_nothing_for_wrong_name() {
    assertEquals(null, testee.parserFor(""));
  }

  @Test
  public void returns_itself_for_correct_type() {
    assertEquals(testee, testee.parserFor(UnlinkedAnchor.class));
  }

  @Test
  public void returns_nothing_for_wrong_type() {
    assertEquals(null, testee.parserFor(Expression.class));
  }

  @Test
  public void parse_UnlinkedAnchor() {
    when(stream.attribute(eq("target"))).thenReturn("the target");

    UnlinkedAnchor anchor = testee.parse();

    assertEquals("the target", anchor.targetName());

    order.verify(stream).elementStart(eq("UnlinkedAnchor"));
    order.verify(stream).attribute(eq("target"));
    order.verify(stream).elementEnd();
  }

}
