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

import ast.data.Ast;
import ast.data.function.Function;
import ast.data.raw.RawElementary;
import ast.data.statement.Block;
import ast.data.template.Template;
import ast.meta.MetaInformation;
import ast.pass.output.xml.IdReader;
import ast.visitor.Visitor;

public class Writer_RawComponent_Test {
  final private XmlStreamWriter stream = mock(XmlStreamWriter.class);
  final private IdReader astId = mock(IdReader.class);
  final private Visitor idWriter = mock(Visitor.class);
  final private Write testee = new Write(stream, astId, idWriter);
  final private MetaInformation info = mock(MetaInformation.class);
  final private Function interfaceFunction = mock(Function.class);
  final private Block entry = mock(Block.class);
  final private Block exit = mock(Block.class);
  final private Template declaration = mock(Template.class);
  final private Ast instantiation = mock(Ast.class);
  final private InOrder order = Mockito.inOrder(stream, info, interfaceFunction, entry, exit, declaration, instantiation, idWriter);

  @Test
  public void write_RawElementary() {
    RawElementary item = new RawElementary("theName");
    item.metadata().add(info);
    item.getIface().add(interfaceFunction);
    item.setEntryFunc(entry);
    item.setExitFunc(exit);
    item.getDeclaration().add(declaration);
    item.getInstantiation().add(instantiation);

    testee.visit(item);

    order.verify(stream).beginNode(eq("RawElementary"));
    order.verify(stream).attribute("name", "theName");
    order.verify(idWriter).visit(item);
    order.verify(info).accept(eq(testee));

    order.verify(stream).beginNode(eq("interface"));
    order.verify(interfaceFunction).accept(eq(testee));
    order.verify(stream).endNode();

    order.verify(stream).beginNode(eq("entry"));
    order.verify(entry).accept(eq(testee));
    order.verify(stream).endNode();

    order.verify(stream).beginNode(eq("exit"));
    order.verify(exit).accept(eq(testee));
    order.verify(stream).endNode();

    order.verify(stream).beginNode(eq("declaration"));
    order.verify(declaration).accept(eq(testee));
    order.verify(stream).endNode();

    order.verify(stream).beginNode(eq("instantiation"));
    order.verify(instantiation).accept(eq(testee));
    order.verify(stream, times(2)).endNode();
  }
}
