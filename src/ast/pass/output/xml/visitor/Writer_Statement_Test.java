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

import ast.data.AstList;
import ast.data.expression.Expression;
import ast.data.reference.Reference;
import ast.data.statement.Block;
import ast.data.statement.CallStmt;
import ast.data.statement.ExpressionReturn;
import ast.data.statement.IfOption;
import ast.data.statement.IfStatement;
import ast.data.statement.MultiAssignment;
import ast.meta.MetaInformation;

public class Writer_Statement_Test {
  final private XmlStreamWriter stream = mock(XmlStreamWriter.class);
  final private Write testee = new Write(stream);
  final private MetaInformation info = mock(MetaInformation.class);
  final private Block child = mock(Block.class);
  final private Reference reference = mock(Reference.class);
  final private Expression expression = mock(Expression.class);
  final private IfOption ifOption = mock(IfOption.class);
  final private InOrder order = Mockito.inOrder(stream, info, child, reference, expression, ifOption);

  @Test
  public void write_Block() {
    Block item = new Block();
    item.metadata().add(info);
    item.statements.add(child);

    testee.visit(item);

    order.verify(stream).beginNode(eq("Block"));
    order.verify(info).accept(eq(testee));
    order.verify(child).accept(eq(testee));
    order.verify(stream).endNode();
  }

  @Test
  public void write_CallStatement() {
    CallStmt item = new CallStmt(reference);
    item.metadata().add(info);

    testee.visit(item);

    order.verify(stream).beginNode(eq("CallStatement"));
    order.verify(info).accept(eq(testee));
    order.verify(reference).accept(eq(testee));
    order.verify(stream).endNode();
  }

  @Test
  public void write_MultiAssignment() {
    AstList<Reference> left = new AstList<Reference>();
    left.add(reference);
    MultiAssignment item = new MultiAssignment(left, expression);
    item.metadata().add(info);

    testee.visit(item);

    order.verify(stream).beginNode(eq("MultiAssignment"));
    order.verify(info).accept(eq(testee));
    order.verify(reference).accept(eq(testee));
    order.verify(expression).accept(eq(testee));
    order.verify(stream).endNode();
  }

  @Test
  public void write_IfStatement() {
    AstList<IfOption> options = new AstList<IfOption>();
    options.add(ifOption);
    IfStatement item = new IfStatement(options, child);
    item.metadata().add(info);

    testee.visit(item);

    order.verify(stream).beginNode(eq("IfStatement"));
    order.verify(info).accept(eq(testee));
    order.verify(ifOption).accept(eq(testee));
    order.verify(child).accept(eq(testee));
    order.verify(stream).endNode();
  }

  @Test
  public void write_IfOption() {
    IfOption item = new IfOption(expression, child);
    item.metadata().add(info);

    testee.visit(item);

    order.verify(stream).beginNode(eq("IfOption"));
    order.verify(info).accept(eq(testee));
    order.verify(expression).accept(eq(testee));
    order.verify(child).accept(eq(testee));
    order.verify(stream).endNode();
  }

  @Test
  public void write_ExpressionReturn() {
    ExpressionReturn item = new ExpressionReturn(expression);
    item.metadata().add(info);

    testee.visit(item);

    order.verify(stream).beginNode(eq("ExpressionReturn"));
    order.verify(info).accept(eq(testee));
    order.verify(expression).accept(eq(testee));
    order.verify(stream).endNode();
  }
}
