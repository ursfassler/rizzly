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

import java.math.BigInteger;

import org.junit.Test;
import org.mockito.InOrder;
import org.mockito.Mockito;

import ast.data.expression.value.BooleanValue;
import ast.data.expression.value.NumberValue;
import ast.data.expression.value.TupleValue;
import ast.pass.output.xml.IdReader;
import ast.visitor.VisitExecutor;
import ast.visitor.Visitor;

public class Writer_ValueExpression_Test {
  final private XmlStreamWriter stream = mock(XmlStreamWriter.class);
  final private IdReader astId = mock(IdReader.class);
  final private Visitor idWriter = mock(Visitor.class);
  final private VisitExecutor executor = mock(VisitExecutor.class);
  final private Write testee = new Write(stream, astId, idWriter, executor);
  final private InOrder order = Mockito.inOrder(stream, idWriter, executor);

  @Test
  public void write_boolean_false() {
    BooleanValue item = new BooleanValue(false);

    testee.visit(item);

    order.verify(stream).beginNode(eq("BooleanValue"));
    order.verify(stream).attribute(eq("value"), eq("False"));
    order.verify(executor).visit(eq(idWriter), eq(item));
    order.verify(executor).visit(eq(testee), eq(item.metadata()));
    order.verify(stream).endNode();
  }

  @Test
  public void write_boolean_true() {
    BooleanValue item = new BooleanValue(true);

    testee.visit(item);

    order.verify(stream).beginNode(eq("BooleanValue"));
    order.verify(stream).attribute(eq("value"), eq("True"));
    order.verify(executor).visit(eq(idWriter), eq(item));
    order.verify(executor).visit(eq(testee), eq(item.metadata()));
    order.verify(stream).endNode();
  }

  @Test
  public void write_numberic_0() {
    NumberValue item = new NumberValue(BigInteger.valueOf(0));

    testee.visit(item);

    order.verify(stream).beginNode(eq("NumberValue"));
    order.verify(stream).attribute(eq("value"), eq("0"));
    order.verify(executor).visit(eq(idWriter), eq(item));
    order.verify(executor).visit(eq(testee), eq(item.metadata()));
    order.verify(stream).endNode();
  }

  @Test
  public void write_numberic_42() {
    NumberValue item = new NumberValue(BigInteger.valueOf(42));

    testee.visit(item);

    order.verify(stream).beginNode(eq("NumberValue"));
    order.verify(stream).attribute(eq("value"), eq("42"));
    order.verify(executor).visit(eq(idWriter), eq(item));
    order.verify(executor).visit(eq(testee), eq(item.metadata()));
    order.verify(stream).endNode();
  }

  @Test
  public void write_numberic_minus_1000() {
    NumberValue item = new NumberValue(BigInteger.valueOf(-1000));

    testee.visit(item);

    order.verify(stream).beginNode(eq("NumberValue"));
    order.verify(stream).attribute(eq("value"), eq("-1000"));
    order.verify(executor).visit(eq(idWriter), eq(item));
    order.verify(executor).visit(eq(testee), eq(item.metadata()));
    order.verify(stream).endNode();
  }

  @Test
  public void write_Tuple() {
    TupleValue item = new TupleValue();

    testee.visit(item);

    order.verify(stream).beginNode(eq("TupleValue"));
    order.verify(executor).visit(eq(idWriter), eq(item));
    order.verify(executor).visit(eq(testee), eq(item.metadata()));
    order.verify(executor).visit(eq(testee), eq(item.value));
    order.verify(stream).endNode();
  }

}
