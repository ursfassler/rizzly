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
import ast.pass.output.xml.IdReader;
import ast.visitor.VisitExecutor;
import ast.visitor.Visitor;

public class Writer_Statement_Test {
  final private XmlStreamWriter stream = mock(XmlStreamWriter.class);
  final private IdReader astId = mock(IdReader.class);
  final private Visitor idWriter = mock(Visitor.class);
  final private VisitExecutor executor = mock(VisitExecutor.class);
  final private Write testee = new Write(stream, astId, idWriter, executor);
  final private Block child = mock(Block.class);
  final private Expression expression = mock(Expression.class);
  final private InOrder order = Mockito.inOrder(stream, idWriter, executor);

  @Test
  public void write_Block() {
    Block item = new Block();

    testee.visit(item);

    order.verify(stream).beginNode(eq("Block"));
    order.verify(executor).visit(eq(idWriter), eq(item));
    order.verify(executor).visit(eq(testee), eq(item.metadata()));
    order.verify(executor).visit(eq(testee), eq(item.statements));
    order.verify(stream).endNode();
  }

  @Test
  public void write_CallStatement() {
    Reference reference = mock(Reference.class);
    CallStmt item = new CallStmt(reference);

    testee.visit(item);

    order.verify(stream).beginNode(eq("CallStatement"));
    order.verify(executor).visit(eq(idWriter), eq(item));
    order.verify(executor).visit(eq(testee), eq(item.metadata()));
    order.verify(executor).visit(eq(testee), eq(item.call));
    order.verify(stream).endNode();
  }

  @Test
  public void write_MultiAssignment() {
    AstList<Reference> left = new AstList<Reference>();
    MultiAssignment item = new MultiAssignment(left, expression);

    testee.visit(item);

    order.verify(stream).beginNode(eq("MultiAssignment"));
    order.verify(executor).visit(eq(idWriter), eq(item));
    order.verify(executor).visit(eq(testee), eq(item.metadata()));
    order.verify(executor).visit(eq(testee), eq(item.left));
    order.verify(executor).visit(eq(testee), eq(item.right));
    order.verify(stream).endNode();
  }

  @Test
  public void write_IfStatement() {
    AstList<IfOption> options = new AstList<IfOption>();
    IfStatement item = new IfStatement(options, child);

    testee.visit(item);

    order.verify(stream).beginNode(eq("IfStatement"));
    order.verify(executor).visit(eq(idWriter), eq(item));
    order.verify(executor).visit(eq(testee), eq(item.metadata()));
    order.verify(executor).visit(eq(testee), eq(item.option));
    order.verify(executor).visit(eq(testee), eq(item.defblock));
    order.verify(stream).endNode();
  }

  @Test
  public void write_IfOption() {
    IfOption item = new IfOption(expression, child);

    testee.visit(item);

    order.verify(stream).beginNode(eq("IfOption"));
    order.verify(executor).visit(eq(idWriter), eq(item));
    order.verify(executor).visit(eq(testee), eq(item.metadata()));
    order.verify(executor).visit(eq(testee), eq(item.condition));
    order.verify(executor).visit(eq(testee), eq(item.code));
    order.verify(stream).endNode();
  }

  @Test
  public void write_ExpressionReturn() {
    ExpressionReturn item = new ExpressionReturn(expression);

    testee.visit(item);

    order.verify(stream).beginNode(eq("ExpressionReturn"));
    order.verify(executor).visit(eq(idWriter), eq(item));
    order.verify(executor).visit(eq(testee), eq(item.metadata()));
    order.verify(executor).visit(eq(testee), eq(item.expression));
    order.verify(stream).endNode();
  }
}
