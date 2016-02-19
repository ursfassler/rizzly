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
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.Test;
import org.mockito.InOrder;
import org.mockito.Mockito;

import ast.data.expression.Expression;
import ast.data.reference.Reference;
import ast.data.variable.GlobalConstant;
import ast.pass.input.xml.infrastructure.XmlParser;
import ast.pass.input.xml.scanner.ExpectionParser;
import error.RizzlyError;

public class GlobalConstantParser_Test {
  final private ExpectionParser stream = mock(ExpectionParser.class);
  final private XmlParser parser = mock(XmlParser.class);
  final private RizzlyError error = mock(RizzlyError.class);
  final private GlobalConstantParser testee = new GlobalConstantParser(stream, parser, error);
  final private InOrder order = Mockito.inOrder(stream, parser);
  final private Reference reference = mock(Reference.class);
  final private Expression expression = mock(Expression.class);

  @Test
  public void has_correct_name() {
    assertEquals("GlobalConstant", testee.name());
  }

  @Test
  public void has_correct_type() {
    assertEquals(GlobalConstant.class, testee.type());
  }

  @Test
  public void parse_global_constant() {
    when(stream.attribute(eq("name"))).thenReturn("the variable name");
    when(parser.itemOf(Reference.class)).thenReturn(reference);
    when(parser.itemOf(Expression.class)).thenReturn(expression);

    GlobalConstant globalConstant = testee.parse();

    assertEquals("the variable name", globalConstant.getName());
    assertEquals(reference, globalConstant.type);
    assertEquals(expression, globalConstant.def);

    order.verify(stream).elementStart(eq("GlobalConstant"));
    order.verify(stream).attribute(eq("name"));
    order.verify(parser).itemOf(eq(Reference.class));
    order.verify(parser).itemOf(eq(Expression.class));
    order.verify(stream).elementEnd();
  }

}
