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
import static org.mockito.Mockito.times;

import org.junit.Test;
import org.mockito.InOrder;
import org.mockito.Mockito;

import ast.data.raw.RawElementary;
import ast.pass.output.xml.IdReader;
import ast.visitor.VisitExecutor;
import ast.visitor.Visitor;

public class Writer_RawComponent_Test {
  final private XmlStreamWriter stream = mock(XmlStreamWriter.class);
  final private IdReader astId = mock(IdReader.class);
  final private Visitor idWriter = mock(Visitor.class);
  final private VisitExecutor executor = mock(VisitExecutor.class);
  final private Write testee = new Write(stream, astId, idWriter, executor);
  final private InOrder order = Mockito.inOrder(stream, idWriter, executor);

  @Test
  public void write_RawElementary() {
    RawElementary item = new RawElementary("theName");

    testee.visit(item);

    order.verify(stream).beginNode(eq("RawElementary"));
    order.verify(stream).attribute("name", "theName");
    order.verify(executor).visit(idWriter, item);
    order.verify(executor).visit(testee, item.metadata());

    order.verify(stream).beginNode(eq("interface"));
    order.verify(executor).visit(eq(testee), eq(item.getIface()));
    order.verify(stream).endNode();

    order.verify(stream).beginNode(eq("entry"));
    order.verify(executor).visit(eq(testee), eq(item.getEntryFunc()));
    order.verify(stream).endNode();

    order.verify(stream).beginNode(eq("exit"));
    order.verify(executor).visit(eq(testee), eq(item.getExitFunc()));
    order.verify(stream).endNode();

    order.verify(stream).beginNode(eq("declaration"));
    order.verify(executor).visit(eq(testee), eq(item.getDeclaration()));
    order.verify(stream).endNode();

    order.verify(stream).beginNode(eq("instantiation"));
    order.verify(executor).visit(eq(testee), eq(item.getInstantiation()));
    order.verify(stream, times(2)).endNode();
  }
}
