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

import ast.data.expression.Expression;
import ast.data.expression.value.BooleanValue;
import ast.data.expression.value.NumberValue;
import ast.data.expression.value.TupleValue;
import ast.meta.MetaInformation;

public class Writer_ValueExpression_Test {
  final private XmlStreamWriter stream = mock(XmlStreamWriter.class);
  final private Write testee = new Write(stream);
  final private MetaInformation info = mock(MetaInformation.class);
  final private Expression child = mock(Expression.class);
  final private InOrder order = Mockito.inOrder(stream, info, child);

  @Test
  public void write_boolean_false() {
    BooleanValue value = new BooleanValue(false);
    value.metadata().add(info);

    testee.visit(value);

    order.verify(stream).beginNode(eq("BooleanValue"));
    order.verify(stream).attribute(eq("value"), eq("False"));
    order.verify(info).accept(eq(testee));
    order.verify(stream).endNode();
  }

  @Test
  public void write_boolean_true() {
    BooleanValue value = new BooleanValue(true);
    value.metadata().add(info);

    testee.visit(value);

    order.verify(stream).beginNode(eq("BooleanValue"));
    order.verify(stream).attribute(eq("value"), eq("True"));
    order.verify(info).accept(eq(testee));
    order.verify(stream).endNode();
  }

  @Test
  public void write_numberic_0() {
    NumberValue value = new NumberValue(BigInteger.valueOf(0));
    value.metadata().add(info);

    testee.visit(value);

    order.verify(stream).beginNode(eq("NumberValue"));
    order.verify(stream).attribute(eq("value"), eq("0"));
    order.verify(info).accept(eq(testee));
    order.verify(stream).endNode();
  }

  @Test
  public void write_numberic_42() {
    NumberValue value = new NumberValue(BigInteger.valueOf(42));
    value.metadata().add(info);

    testee.visit(value);

    order.verify(stream).beginNode(eq("NumberValue"));
    order.verify(stream).attribute(eq("value"), eq("42"));
    order.verify(info).accept(eq(testee));
    order.verify(stream).endNode();
  }

  @Test
  public void write_numberic_minus_1000() {
    NumberValue value = new NumberValue(BigInteger.valueOf(-1000));
    value.metadata().add(info);

    testee.visit(value);

    order.verify(stream).beginNode(eq("NumberValue"));
    order.verify(stream).attribute(eq("value"), eq("-1000"));
    order.verify(info).accept(eq(testee));
    order.verify(stream).endNode();
  }

  @Test
  public void write_Tuple() {
    TupleValue value = new TupleValue();
    value.metadata().add(info);
    value.value.add(child);

    testee.visit(value);

    order.verify(stream).beginNode(eq("TupleValue"));
    order.verify(info).accept(eq(testee));
    order.verify(child).accept(eq(testee));
    order.verify(stream).endNode();
  }

}
