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

import ast.data.type.special.NaturalType;
import ast.pass.input.xml.infrastructure.XmlParser;
import ast.pass.input.xml.linker.ObjectRegistrar;
import ast.pass.input.xml.parser.Names;
import ast.pass.input.xml.scanner.ExpectionParser;
import error.RizzlyError;

public class NaturalParser_Test {
  final private ExpectionParser stream = mock(ExpectionParser.class);
  final private ObjectRegistrar objectRegistrar = mock(ObjectRegistrar.class);
  final private XmlParser parser = mock(XmlParser.class);
  final private RizzlyError error = mock(RizzlyError.class);
  final private NaturalParser testee = new NaturalParser(stream, objectRegistrar, parser, error);
  final private InOrder order = Mockito.inOrder(stream, parser, objectRegistrar);

  @Test
  public void has_correct_name() {
    assertEquals(Names.list("Natural"), testee.names());
  }

  @Test
  public void has_correct_type() {
    assertEquals(NaturalType.class, testee.type());
  }

  @Test
  public void parse_NaturalType() {
    when(stream.attribute(eq("name"))).thenReturn("the name");
    when(parser.id()).thenReturn("the id");

    NaturalType value = testee.parse();

    assertEquals("the name", value.getName());

    order.verify(stream).elementStart(eq("Natural"));
    order.verify(stream).attribute(eq("name"));
    order.verify(parser).id();
    order.verify(stream).elementEnd();
    order.verify(objectRegistrar).register(eq("the id"), eq(value));
  }

}
