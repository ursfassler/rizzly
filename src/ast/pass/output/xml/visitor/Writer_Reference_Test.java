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
import ast.data.Named;
import ast.data.expression.value.TupleValue;
import ast.data.reference.LinkTarget;
import ast.data.reference.RefCall;
import ast.data.reference.RefItem;
import ast.data.reference.Reference;
import ast.data.type.TypeReference;
import ast.meta.MetaInformation;

public class Writer_Reference_Test {
  final private XmlStreamWriter stream = mock(XmlStreamWriter.class);
  final private Write testee = new Write(stream);
  final private MetaInformation meta = mock(MetaInformation.class);
  final private Named link = mock(Named.class);
  final private RefItem offset1 = mock(RefItem.class);
  final private TupleValue tuple = mock(TupleValue.class);
  final private Reference reference = mock(Reference.class);
  final private InOrder order = Mockito.inOrder(stream, meta, link, offset1, tuple, reference);
  final private AstList<RefItem> offset;

  public Writer_Reference_Test() {
    offset = new AstList<RefItem>();
    offset.add(offset1);
  }

  @Test
  public void write_link_target() {
    LinkTarget item = new LinkTarget("the target");
    item.metadata().add(meta);

    testee.visit(item);

    order.verify(stream).beginNode(eq("LinkTarget"));
    order.verify(stream).attribute("name", "the target");
    order.verify(meta).accept(eq(testee));
    order.verify(stream).endNode();
  }

  @Test
  public void write_TypeReference() {
    TypeReference item = new TypeReference(reference);
    item.metadata().add(meta);

    testee.visit(item);

    order.verify(stream).beginNode(eq("TypeReference"));
    order.verify(meta).accept(eq(testee));
    order.verify(reference).accept(eq(testee));
    order.verify(stream).endNode();
  }

  @Test
  public void write_Reference() {
    Mockito.when(link.getName()).thenReturn("the link id");
    Reference item = new Reference(link, offset);
    item.metadata().add(meta);

    testee.visit(item);

    order.verify(stream).beginNode(eq("Reference"));
    order.verify(stream).attribute("link", "the link id");
    order.verify(meta).accept(eq(testee));
    order.verify(offset1).accept(eq(testee));
    order.verify(stream).endNode();
  }

  @Test
  public void write_ReferenceItemCall() {
    RefCall item = new RefCall(tuple);
    item.metadata().add(meta);

    testee.visit(item);

    order.verify(stream).beginNode(eq("ReferenceCall"));
    order.verify(meta).accept(eq(testee));
    order.verify(tuple).accept(eq(testee));
    order.verify(stream).endNode();
  }
}
