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
import static org.mockito.Mockito.never;

import org.junit.Test;
import org.mockito.InOrder;
import org.mockito.Mockito;

import ast.data.AstList;
import ast.data.Named;
import ast.data.expression.value.TupleValue;
import ast.data.reference.LinkedAnchor;
import ast.data.reference.RefCall;
import ast.data.reference.RefItem;
import ast.data.reference.Reference;
import ast.data.reference.UnlinkedAnchor;
import ast.pass.output.xml.IdReader;
import ast.visitor.VisitExecutor;
import ast.visitor.Visitor;

public class Writer_Reference_Test {
  final private XmlStreamWriter stream = mock(XmlStreamWriter.class);
  final private IdReader astId = mock(IdReader.class);
  final private Visitor idWriter = mock(Visitor.class);
  final private VisitExecutor executor = mock(VisitExecutor.class);
  final private Write testee = new Write(stream, astId, idWriter, executor);
  final private Named link = mock(Named.class);
  final private TupleValue tuple = mock(TupleValue.class);
  final private Reference reference = mock(Reference.class);
  final private InOrder order = Mockito.inOrder(stream, link, tuple, reference, astId, idWriter, executor);
  final private AstList<RefItem> offset = mock(AstList.class);

  @Test
  public void write_LinkedAnchor_with_id() {
    Mockito.when(astId.hasId(link)).thenReturn(true);
    Mockito.when(astId.getId(link)).thenReturn("the link id");
    LinkedAnchor item = new LinkedAnchor(link);

    testee.visit(item);

    order.verify(stream).beginNode(eq("LinkedAnchor"));
    order.verify(executor).visit(idWriter, item);
    order.verify(stream).attribute("link", "the link id");
    order.verify(executor, never()).visit(eq(testee), eq(item.getLink()));
    order.verify(executor).visit(testee, item.metadata());
    order.verify(stream).endNode();
  }

  @Test
  public void write_UnlinkedAnchor() {
    Mockito.when(astId.hasId(link)).thenReturn(false);
    UnlinkedAnchor item = new UnlinkedAnchor("the link");

    testee.visit(item);

    order.verify(stream).beginNode(eq("UnlinkedAnchor"));
    order.verify(executor).visit(idWriter, item);
    order.verify(stream).attribute(eq("link"), eq("the link"));
    order.verify(executor).visit(testee, item.metadata());
    order.verify(stream).endNode();
  }

  @Test
  public void write_ReferenceItemCall() {
    RefCall item = new RefCall(tuple);

    testee.visit(item);

    order.verify(stream).beginNode(eq("ReferenceCall"));
    order.verify(executor).visit(idWriter, item);
    order.verify(executor).visit(testee, item.metadata());
    order.verify(executor).visit(eq(testee), eq(item.actualParameter));
    order.verify(stream).endNode();
  }
}
