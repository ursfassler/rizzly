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

package ast.pass.output.xml.visitor;

import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;

import org.junit.Test;
import org.mockito.InOrder;
import org.mockito.Mockito;

import ast.data.expression.Expression;
import ast.data.reference.Reference;
import ast.data.variable.PrivateConstant;
import ast.data.variable.FunctionVariable;
import ast.data.variable.GlobalConstant;
import ast.data.variable.StateVariable;
import ast.pass.output.xml.IdReader;
import ast.visitor.VisitExecutor;
import ast.visitor.Visitor;

public class Writer_Variable_Test {
  final private XmlStreamWriter stream = mock(XmlStreamWriter.class);
  final private IdReader astId = mock(IdReader.class);
  final private Visitor idWriter = mock(Visitor.class);
  final private VisitExecutor executor = mock(VisitExecutor.class);
  final private Write testee = new Write(stream, astId, idWriter, executor);
  final private Reference type = mock(Reference.class);
  final private Expression defaultValue = mock(Expression.class);
  final private InOrder order = Mockito.inOrder(stream, idWriter, executor);

  @Test
  public void write_StateVariable() {
    StateVariable item = new StateVariable("the variable", type, defaultValue);

    testee.visit(item);

    order.verify(stream).beginNode(eq("StateVariable"));
    order.verify(stream).attribute("name", "the variable");
    order.verify(executor).visit(eq(idWriter), eq(item));
    order.verify(executor).visit(eq(testee), eq(item.metadata()));
    order.verify(executor).visit(eq(testee), eq(item.type));
    order.verify(executor).visit(eq(testee), eq(item.def));
    order.verify(stream).endNode();
  }

  @Test
  public void write_FunctionVariable() {
    FunctionVariable item = new FunctionVariable("the variable", type);

    testee.visit(item);

    order.verify(stream).beginNode(eq("FunctionVariable"));
    order.verify(stream).attribute("name", "the variable");
    order.verify(executor).visit(eq(idWriter), eq(item));
    order.verify(executor).visit(eq(testee), eq(item.metadata()));
    order.verify(executor).visit(eq(testee), eq(item.type));
    order.verify(stream).endNode();
  }

  @Test
  public void write_GlobalConstant() {
    GlobalConstant item = new GlobalConstant("the variable", type, defaultValue);

    testee.visit(item);

    order.verify(stream).beginNode(eq("GlobalConstant"));
    order.verify(stream).attribute("name", "the variable");
    order.verify(executor).visit(eq(idWriter), eq(item));
    order.verify(executor).visit(eq(testee), eq(item.metadata()));
    order.verify(executor).visit(eq(testee), eq(item.type));
    order.verify(executor).visit(eq(testee), eq(item.def));
    order.verify(stream).endNode();
  }

  @Test
  public void write_PrivateConstant() {
    PrivateConstant item = new PrivateConstant("the variable", type, defaultValue);

    testee.visit(item);

    order.verify(stream).beginNode(eq("PrivateConstant"));
  }
}
