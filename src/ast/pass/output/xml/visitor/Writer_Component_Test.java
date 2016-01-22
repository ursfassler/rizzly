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
import ast.data.component.composition.SubCallbacks;
import ast.data.component.elementary.ImplElementary;
import ast.data.function.Function;
import ast.data.function.FunctionReference;
import ast.data.function.InterfaceFunction;
import ast.data.type.Type;
import ast.data.variable.Constant;
import ast.data.variable.Variable;
import ast.meta.MetaInformation;
import ast.pass.output.xml.IdReader;
import ast.visitor.Visitor;

public class Writer_Component_Test {
  final private XmlStreamWriter stream = mock(XmlStreamWriter.class);
  final private IdReader astId = mock(IdReader.class);
  final private Visitor idWriter = mock(Visitor.class);
  final private Write testee = new Write(stream, astId, idWriter);
  final private MetaInformation info = mock(MetaInformation.class);
  final private Type type = mock(Type.class);
  final private FunctionReference entry = mock(FunctionReference.class);
  final private FunctionReference exit = mock(FunctionReference.class);
  final private Queue queue = mock(Queue.class);
  final private InterfaceFunction interfaceFunction = mock(InterfaceFunction.class);
  final private Function function = mock(Function.class);
  final private Variable variable = mock(Variable.class);
  final private Constant constant = mock(Constant.class);
  final private ComponentUse component = mock(ComponentUse.class);
  final private SubCallbacks subCallback = mock(SubCallbacks.class);
  final private ComponentReference componentReference = mock(ComponentReference.class);
  final private InOrder order = Mockito.inOrder(stream, info, type, entry, exit, queue, interfaceFunction, idWriter, function, variable, constant, component, subCallback, componentReference);

  @Test
  public void write_Elementary() {
    ImplElementary item = new ImplElementary("theName", entry, exit);
    item.metadata().add(info);

    item.queue = queue;
    item.iface.add(interfaceFunction);
    item.function.add(function);

    item.type.add(type);
    item.variable.add(variable);
    item.constant.add(constant);
    item.component.add(component);
    item.subCallback.add(subCallback);

    testee.visit(item);

    order.verify(stream).beginNode(eq("Elementary"));
    order.verify(stream).attribute("name", "theName");
    order.verify(idWriter).visit(item);
    order.verify(info).accept(eq(testee));

    order.verify(queue).accept(eq(testee));

    order.verify(stream).beginNode(eq("interface"));
    order.verify(interfaceFunction).accept(eq(testee));
    order.verify(stream).endNode();

    order.verify(stream).beginNode(eq("function"));
    order.verify(function).accept(eq(testee));
    order.verify(stream).endNode();

    order.verify(stream).beginNode(eq("entry"));
    order.verify(entry).accept(eq(testee));
    order.verify(stream).endNode();

    order.verify(stream).beginNode(eq("exit"));
    order.verify(exit).accept(eq(testee));
    order.verify(stream).endNode();

    order.verify(stream).beginNode(eq("type"));
    order.verify(type).accept(eq(testee));
    order.verify(stream).endNode();

    order.verify(stream).beginNode(eq("variable"));
    order.verify(variable).accept(eq(testee));
    order.verify(stream).endNode();

    order.verify(stream).beginNode(eq("constant"));
    order.verify(constant).accept(eq(testee));
    order.verify(stream).endNode();

    order.verify(stream).beginNode(eq("component"));
    order.verify(component).accept(eq(testee));
    order.verify(stream).endNode();

    order.verify(stream).beginNode(eq("subCallback"));
    order.verify(subCallback).accept(eq(testee));
    order.verify(stream, times(2)).endNode();

  }

  @Test
  public void write_Queue() {
    Queue item = new Queue();
    item.setName("the queue name");
    item.metadata().add(info);

    testee.visit(item);

    order.verify(stream).beginNode(eq("Queue"));
    order.verify(stream).attribute("name", "the queue name");
    order.verify(idWriter).visit(item);
    order.verify(info).accept(eq(testee));
    order.verify(stream).endNode();
  }

  @Test
  public void write_ComponentUse() {
    ComponentUse item = new ComponentUse("the name", componentReference);
    item.metadata().add(info);

    testee.visit(item);

    order.verify(stream).beginNode(eq("ComponentUse"));
    order.verify(stream).attribute(eq("name"), eq("the name"));
    order.verify(idWriter).visit(item);
    order.verify(info).accept(eq(testee));
    order.verify(componentReference).accept(eq(testee));
    order.verify(stream).endNode();

  }
}
