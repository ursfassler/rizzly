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

package ast.pass.input.xml.parser.type;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.Test;
import org.mockito.InOrder;
import org.mockito.Mockito;

import ast.data.expression.Expression;
import ast.data.type.base.BooleanType;
import ast.pass.input.xml.infrastructure.XmlParser;
import ast.pass.input.xml.linker.ObjectRegistrar;
import ast.pass.input.xml.scanner.ExpectionParser;
import error.RizzlyError;

public class BooleanParser_Test {
  final private ExpectionParser stream = mock(ExpectionParser.class);
  final private ObjectRegistrar objectRegistrar = mock(ObjectRegistrar.class);
  final private XmlParser parser = mock(XmlParser.class);
  final private RizzlyError error = mock(RizzlyError.class);
  final private BooleanParser testee = new BooleanParser(stream, objectRegistrar, parser, error);
  final private InOrder order = Mockito.inOrder(stream, parser, objectRegistrar);

  @Test
  public void returns_itself_for_correct_name() {
    assertEquals(testee, testee.parserFor("Boolean"));
  }

  @Test
  public void returns_nothing_for_wrong_name() {
    assertEquals(null, testee.parserFor(""));
  }

  @Test
  public void returns_itself_for_correct_type() {
    assertEquals(testee, testee.parserFor(BooleanType.class));
  }

  @Test
  public void returns_nothing_for_wrong_type() {
    assertEquals(null, testee.parserFor(Expression.class));
  }

  @Test
  public void parse_IntegerType() {
    when(stream.attribute(eq("name"))).thenReturn("the name");
    when(parser.id()).thenReturn("the id");

    BooleanType type = testee.parse();

    assertEquals("the name", type.getName());

    order.verify(stream).elementStart(eq("Boolean"));
    order.verify(stream).attribute(eq("name"));
    order.verify(parser).id();
    order.verify(stream).elementEnd();
    order.verify(objectRegistrar).register(eq("the id"), eq(type));
  }

}
