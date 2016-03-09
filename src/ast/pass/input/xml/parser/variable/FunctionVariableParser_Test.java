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

package ast.pass.input.xml.parser.variable;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.Test;
import org.mockito.InOrder;
import org.mockito.Mockito;

import ast.data.reference.Reference;
import ast.data.variable.FunctionVariable;
import ast.pass.input.xml.infrastructure.XmlParser;
import ast.pass.input.xml.linker.ObjectRegistrar;
import ast.pass.input.xml.parser.Names;
import ast.pass.input.xml.scanner.ExpectionParser;
import error.RizzlyError;

public class FunctionVariableParser_Test {
  final private ExpectionParser stream = mock(ExpectionParser.class);
  final private ObjectRegistrar objectRegistrar = mock(ObjectRegistrar.class);
  final private XmlParser parser = mock(XmlParser.class);
  final private RizzlyError error = mock(RizzlyError.class);
  final private FunctionVariableParser testee = new FunctionVariableParser(stream, objectRegistrar, parser, error);
  final private InOrder order = Mockito.inOrder(stream, parser, objectRegistrar);
  final private Reference reference = mock(Reference.class);

  @Test
  public void has_correct_name() {
    assertEquals(Names.list("FunctionVariable"), testee.names());
  }

  @Test
  public void has_correct_type() {
    assertEquals(FunctionVariable.class, testee.type());
  }

  @Test
  public void parse_FunctionVariable() {
    when(stream.attribute(eq("name"))).thenReturn("the variable name");
    when(parser.itemOf(Reference.class)).thenReturn(reference);
    when(parser.id()).thenReturn("the id");

    FunctionVariable variable = testee.parse();

    assertEquals("the variable name", variable.getName());
    assertEquals(reference, variable.type);

    order.verify(stream).elementStart(eq("FunctionVariable"));
    order.verify(stream).attribute(eq("name"));
    order.verify(parser).id();
    order.verify(parser).itemOf(eq(Reference.class));
    order.verify(stream).elementEnd();
    order.verify(objectRegistrar).register(eq("the id"), eq(variable));
  }

}
