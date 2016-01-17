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

import ast.data.expression.ReferenceExpression;
import ast.data.reference.Reference;
import ast.meta.MetaInformation;

public class Writer_Expression_Test {
  final private XmlStreamWriter stream = mock(XmlStreamWriter.class);
  final private Write testee = new Write(stream);
  final private MetaInformation info = mock(MetaInformation.class);
  final private Reference reference = mock(Reference.class);
  final private InOrder order = Mockito.inOrder(stream, info, reference);

  @Test
  public void write_ReferenceExpression() {
    ReferenceExpression item = new ReferenceExpression(reference);
    item.metadata().add(info);

    testee.visit(item);

    order.verify(stream).beginNode(eq("ReferenceExpression"));
    order.verify(info).accept(eq(testee));
    order.verify(reference).accept(eq(testee));
    order.verify(stream).endNode();
  }
}
