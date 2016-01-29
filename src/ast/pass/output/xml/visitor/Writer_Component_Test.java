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

import ast.data.component.ComponentReference;
import ast.data.component.composition.ComponentUse;
import ast.data.component.composition.Queue;
import ast.data.component.elementary.ImplElementary;
import ast.data.function.FunctionReference;
import ast.pass.output.xml.IdReader;
import ast.visitor.VisitExecutor;
import ast.visitor.Visitor;

public class Writer_Component_Test {
  final private XmlStreamWriter stream = mock(XmlStreamWriter.class);
  final private IdReader astId = mock(IdReader.class);
  final private Visitor idWriter = mock(Visitor.class);
  final private VisitExecutor executor = mock(VisitExecutor.class);
  final private Write testee = new Write(stream, astId, idWriter, executor);
  final private FunctionReference entry = mock(FunctionReference.class);
  final private FunctionReference exit = mock(FunctionReference.class);
  final private ComponentReference componentReference = mock(ComponentReference.class);
  final private InOrder order = Mockito.inOrder(stream, entry, exit, idWriter, componentReference, executor);

  @Test
  public void write_Elementary() {
    ImplElementary item = new ImplElementary("theName", entry, exit);

    testee.visit(item);

    order.verify(stream).beginNode(eq("Elementary"));
    order.verify(stream).attribute("name", "theName");
    order.verify(executor).visit(idWriter, item);
    order.verify(executor).visit(testee, item.metadata());

    order.verify(executor).visit(testee, item.queue);

    order.verify(stream).beginNode(eq("interface"));
    order.verify(executor).visit(testee, item.iface);
    order.verify(stream).endNode();

    order.verify(stream).beginNode(eq("function"));
    order.verify(executor).visit(testee, item.function);
    order.verify(stream).endNode();

    order.verify(stream).beginNode(eq("entry"));
    order.verify(executor).visit(testee, item.entryFunc);
    order.verify(stream).endNode();

    order.verify(stream).beginNode(eq("exit"));
    order.verify(executor).visit(testee, item.exitFunc);
    order.verify(stream).endNode();

    order.verify(stream).beginNode(eq("type"));
    order.verify(executor).visit(testee, item.type);
    order.verify(stream).endNode();

    order.verify(stream).beginNode(eq("variable"));
    order.verify(executor).visit(testee, item.variable);
    order.verify(stream).endNode();

    order.verify(stream).beginNode(eq("constant"));
    order.verify(executor).visit(testee, item.constant);
    order.verify(stream).endNode();

    order.verify(stream).beginNode(eq("component"));
    order.verify(executor).visit(testee, item.component);
    order.verify(stream).endNode();

    order.verify(stream).beginNode(eq("subCallback"));
    order.verify(executor).visit(testee, item.subCallback);
    order.verify(stream, times(2)).endNode();

  }

  @Test
  public void write_Queue() {
    Queue item = new Queue();
    item.setName("the queue name");

    testee.visit(item);

    order.verify(stream).beginNode(eq("Queue"));
    order.verify(stream).attribute("name", "the queue name");
    order.verify(executor).visit(idWriter, item);
    order.verify(executor).visit(testee, item.metadata());
    order.verify(stream).endNode();
  }

  @Test
  public void write_ComponentUse() {
    ComponentUse item = new ComponentUse("the name", componentReference);

    testee.visit(item);

    order.verify(stream).beginNode(eq("ComponentUse"));
    order.verify(stream).attribute(eq("name"), eq("the name"));
    order.verify(executor).visit(idWriter, item);
    order.verify(executor).visit(testee, item.metadata());
    order.verify(executor).visit(testee, componentReference);
    order.verify(stream).endNode();

  }
}
