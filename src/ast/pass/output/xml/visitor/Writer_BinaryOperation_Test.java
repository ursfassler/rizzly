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
import ast.data.expression.binop.Division;
import ast.data.expression.binop.Minus;
import ast.data.expression.binop.Modulo;
import ast.data.expression.binop.Multiplication;
import ast.data.expression.binop.Plus;
import ast.pass.output.xml.IdReader;
import ast.visitor.VisitExecutor;
import ast.visitor.Visitor;

public class Writer_BinaryOperation_Test {
  final private XmlStreamWriter stream = mock(XmlStreamWriter.class);
  final private IdReader astId = mock(IdReader.class);
  final private Visitor idWriter = mock(Visitor.class);
  final private VisitExecutor executor = mock(VisitExecutor.class);
  final private Write testee = new Write(stream, astId, idWriter, executor);
  final private Expression left = mock(Expression.class);
  final private Expression right = mock(Expression.class);
  InOrder order = Mockito.inOrder(stream, executor);

  @Test
  public void write_plus() {
    Plus operation = new Plus(left, right);

    testee.visit(operation);

    order.verify(stream).beginNode(eq("Plus"));
    order.verify(executor).visit(eq(idWriter), eq(operation));
    order.verify(executor).visit(eq(testee), eq(operation.metadata()));
    order.verify(executor).visit(eq(testee), eq(left));
    order.verify(executor).visit(eq(testee), eq(right));
    order.verify(stream).endNode();
  }

  @Test
  public void write_minus() {
    Minus operation = new Minus(left, right);

    testee.visit(operation);

    order.verify(stream).beginNode(eq("Minus"));
  }

  @Test
  public void write_multiplication() {
    Multiplication operation = new Multiplication(left, right);

    testee.visit(operation);

    order.verify(stream).beginNode(eq("Multiplication"));
  }

  @Test
  public void write_division() {
    Division operation = new Division(left, right);

    testee.visit(operation);

    order.verify(stream).beginNode(eq("Division"));
  }

  @Test
  public void write_modulo() {
    Modulo operation = new Modulo(left, right);

    testee.visit(operation);

    order.verify(stream).beginNode(eq("Modulo"));
  }
}
