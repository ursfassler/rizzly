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

import ast.meta.MetaInformation;
import ast.meta.MetaList;
import ast.meta.MetaListImplementation;
import ast.meta.SourcePosition;
import ast.pass.output.xml.IdReader;
import ast.visitor.VisitExecutor;
import ast.visitor.Visitor;

public class Writer_MetaInformation_Test {
  private static final String MetaNamespace = "http://www.bitzgi.ch/2016/rizzly/test/meta";
  final private XmlStreamWriter stream = mock(XmlStreamWriter.class);
  final private IdReader astId = mock(IdReader.class);
  final private Visitor idWriter = mock(Visitor.class);
  final private VisitExecutor executor = mock(VisitExecutor.class);
  final private Write testee = new Write(stream, astId, idWriter, executor);
  final private InOrder order = Mockito.inOrder(stream, executor);

  @Test
  public void write_SourcePosition() {
    SourcePosition item = new SourcePosition("the file name", 42, 57);

    testee.visit(item);

    order.verify(stream).beginNode(eq(MetaNamespace), eq("SourcePosition"));
    order.verify(stream).attribute("filename", "the file name");
    order.verify(stream).attribute("line", "42");
    order.verify(stream).attribute("row", "57");
    order.verify(stream).endNode();
  }

  @Test
  public void write_Metalist() {
    MetaInformation item1 = mock(MetaInformation.class);
    MetaInformation item2 = mock(MetaInformation.class);
    MetaList list = new MetaListImplementation();
    list.add(item1);
    list.add(item2);

    testee.visit(list);

    order.verify(executor).visit(eq(testee), eq(item1));
    order.verify(executor).visit(eq(testee), eq(item2));
  }

}
