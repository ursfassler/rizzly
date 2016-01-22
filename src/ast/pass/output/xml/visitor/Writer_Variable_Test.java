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
import ast.data.type.TypeReference;
import ast.data.variable.FunctionVariable;
import ast.data.variable.GlobalConstant;
import ast.data.variable.StateVariable;
import ast.meta.MetaInformation;
import ast.pass.output.xml.IdReader;
import ast.visitor.Visitor;

public class Writer_Variable_Test {
  final private XmlStreamWriter stream = mock(XmlStreamWriter.class);
  final private IdReader astId = mock(IdReader.class);
  final private Visitor idWriter = mock(Visitor.class);
  final private Write testee = new Write(stream, astId, idWriter);
  final private MetaInformation info = mock(MetaInformation.class);
  final private TypeReference type = mock(TypeReference.class);
  final private Expression defaultValue = mock(Expression.class);
  final private InOrder order = Mockito.inOrder(stream, info, type, defaultValue, idWriter);

  @Test
  public void write_StateVariable() {
    StateVariable item = new StateVariable("the variable", type, defaultValue);
    item.metadata().add(info);

    testee.visit(item);

    order.verify(stream).beginNode(eq("StateVariable"));
    order.verify(stream).attribute("name", "the variable");
    order.verify(idWriter).visit(item);
    order.verify(info).accept(eq(testee));
    order.verify(type).accept(eq(testee));
    order.verify(defaultValue).accept(eq(testee));
    order.verify(stream).endNode();
  }

  @Test
  public void write_FunctionVariable() {
    FunctionVariable item = new FunctionVariable("the variable", type);
    item.metadata().add(info);

    testee.visit(item);

    order.verify(stream).beginNode(eq("FunctionVariable"));
    order.verify(stream).attribute("name", "the variable");
    order.verify(idWriter).visit(item);
    order.verify(info).accept(eq(testee));
    order.verify(type).accept(eq(testee));
    order.verify(stream).endNode();
  }

  @Test
  public void write_GlobalConstant() {
    GlobalConstant item = new GlobalConstant("the variable", type, defaultValue);
    item.metadata().add(info);

    testee.visit(item);

    order.verify(stream).beginNode(eq("GlobalConstant"));
    order.verify(stream).attribute("name", "the variable");
    order.verify(idWriter).visit(item);
    order.verify(info).accept(eq(testee));
    order.verify(type).accept(eq(testee));
    order.verify(defaultValue).accept(eq(testee));
    order.verify(stream).endNode();
  }
}
