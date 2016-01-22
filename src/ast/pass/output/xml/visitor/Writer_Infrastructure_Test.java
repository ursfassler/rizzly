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

import ast.Designator;
import ast.data.Named;
import ast.data.Namespace;
import ast.data.file.RizzlyFile;
import ast.meta.MetaInformation;
import ast.meta.SourcePosition;
import ast.pass.output.xml.IdReader;
import ast.visitor.Visitor;

public class Writer_Infrastructure_Test {
  final private XmlStreamWriter stream = mock(XmlStreamWriter.class);
  final private IdReader astId = mock(IdReader.class);
  final private Visitor idWriter = mock(Visitor.class);
  final private Write testee = new Write(stream, astId, idWriter);
  final private MetaInformation info = mock(MetaInformation.class);
  final private Named child1 = mock(Named.class);
  final private Named child2 = mock(Named.class);
  final private Named child3 = mock(Named.class);
  final private InOrder order = Mockito.inOrder(stream, info, child1, child2, child3, idWriter);

  @Test
  public void write_namespace() {
    Namespace item = new Namespace("ns");
    item.metadata().add(info);
    item.children.add(child1);
    item.children.add(child2);
    item.children.add(child3);

    testee.visit(item);

    order.verify(stream).beginNode(eq("Namespace"));
    order.verify(stream).attribute("name", "ns");
    order.verify(idWriter).visit(item);
    order.verify(info).accept(eq(testee));
    order.verify(child1).accept(eq(testee));
    order.verify(child2).accept(eq(testee));
    order.verify(child3).accept(eq(testee));
    order.verify(stream).endNode();
  }

  @Test
  public void write_SourcePosition() {
    SourcePosition item = new SourcePosition("the file name", 42, 57);

    testee.visit(item);

    order.verify(stream).beginNode(eq("SourcePosition"));
    order.verify(stream).attribute("filename", "the file name");
    order.verify(stream).attribute("line", "42");
    order.verify(stream).attribute("row", "57");
    order.verify(stream).endNode();
  }

  @Test
  public void write_RizzlyFile() {
    RizzlyFile item = new RizzlyFile("the file name");
    item.metadata().add(info);
    item.imports.add(new Designator("first", "second"));
    item.objects.add(child1);
    item.objects.add(child2);
    item.objects.add(child3);

    testee.visit(item);

    order.verify(stream).beginNode(eq("RizzlyFile"));
    order.verify(stream).attribute("name", "the file name");
    order.verify(idWriter).visit(item);
    order.verify(info).accept(eq(testee));

    order.verify(stream).beginNode(eq("import"));
    order.verify(stream).attribute("file", "first.second");
    order.verify(stream).endNode();

    order.verify(child1).accept(eq(testee));
    order.verify(child2).accept(eq(testee));
    order.verify(child3).accept(eq(testee));

    order.verify(stream).endNode();
  }

}
