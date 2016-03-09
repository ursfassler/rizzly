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

package ast.pass.input.xml.parser.function;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.Test;
import org.mockito.InOrder;
import org.mockito.Mockito;

import ast.data.AstList;
import ast.data.expression.Expression;
import ast.data.function.FunctionProperty;
import ast.data.function.header.Procedure;
import ast.data.function.ret.FuncReturnNone;
import ast.data.statement.Block;
import ast.data.variable.FunctionVariable;
import ast.pass.input.xml.infrastructure.XmlParser;
import ast.pass.input.xml.linker.ObjectRegistrar;
import ast.pass.input.xml.scanner.ExpectionParser;
import error.RizzlyError;

public class ProcedureParser_Test {
  final private ExpectionParser stream = mock(ExpectionParser.class);
  final private ObjectRegistrar objectRegistrar = mock(ObjectRegistrar.class);
  final private XmlParser parser = mock(XmlParser.class);
  final private RizzlyError error = mock(RizzlyError.class);
  final private ProcedureParser testee = new ProcedureParser(stream, objectRegistrar, parser, error);
  final private InOrder order = Mockito.inOrder(stream, parser, objectRegistrar);
  final private AstList<FunctionVariable> arguments = mock(AstList.class);
  final private Block body = mock(Block.class);

  @Test
  public void returns_itself_for_correct_name() {
    assertEquals(testee, testee.parserFor("Procedure"));
  }

  @Test
  public void returns_nothing_for_wrong_name() {
    assertEquals(null, testee.parserFor(""));
  }

  @Test
  public void returns_itself_for_correct_type() {
    assertEquals(testee, testee.parserFor(Procedure.class));
  }

  @Test
  public void returns_nothing_for_wrong_type() {
    assertEquals(null, testee.parserFor(Expression.class));
  }

  @Test
  public void parse_Procedure() {
    when(stream.attribute(eq("scope"))).thenReturn("extern");
    when(stream.attribute(eq("name"))).thenReturn("func name");
    when(parser.id()).thenReturn("proc id");
    when(parser.itemsOf(eq(FunctionVariable.class))).thenReturn(arguments);
    when(parser.itemOf(eq(Block.class))).thenReturn(body);

    Procedure func = testee.parse();

    assertEquals(FunctionProperty.External, func.property);
    assertEquals("func name", func.getName());
    assertEquals(arguments, func.param);
    assertTrue(func.ret instanceof FuncReturnNone);
    assertEquals(body, func.body);

    order.verify(stream).elementStart(eq("Procedure"));
    order.verify(stream).attribute(eq("scope"));
    order.verify(stream).attribute(eq("name"));
    order.verify(parser).id();
    order.verify(parser).itemsOf(eq(FunctionVariable.class));
    order.verify(parser).itemOf(eq(Block.class));
    order.verify(stream).elementEnd();
    order.verify(objectRegistrar).register(eq("proc id"), eq(func));
  }

}
