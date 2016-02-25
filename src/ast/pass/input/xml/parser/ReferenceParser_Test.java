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

package ast.pass.input.xml.parser;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.Test;
import org.mockito.InOrder;
import org.mockito.Mockito;

import ast.data.AstList;
import ast.data.reference.Anchor;
import ast.data.reference.OffsetReference;
import ast.data.reference.RefItem;
import ast.data.reference.Reference;
import ast.pass.input.xml.infrastructure.XmlParser;
import ast.pass.input.xml.scanner.ExpectionParser;
import error.RizzlyError;

public class ReferenceParser_Test {
  final private ExpectionParser stream = mock(ExpectionParser.class);
  final private XmlParser parser = mock(XmlParser.class);
  final private RizzlyError error = mock(RizzlyError.class);
  final private ReferenceParser testee = new ReferenceParser(stream, parser, error);
  final private InOrder order = Mockito.inOrder(stream, parser);

  @Test
  public void has_correct_name() {
    assertEquals(Names.list("Reference"), testee.names());
  }

  @Test
  public void has_correct_type() {
    assertEquals(Reference.class, testee.type());
  }

  @Test
  public void parse_reference() {
    Anchor anchor = mock(Anchor.class);
    RefItem refItem1 = mock(RefItem.class);
    RefItem refItem2 = mock(RefItem.class);
    AstList<RefItem> offset = new AstList<RefItem>();
    offset.add(refItem1);
    offset.add(refItem2);
    when(stream.attribute(eq("name"))).thenReturn("the variable name");
    when(parser.itemOf(Anchor.class)).thenReturn(anchor);
    when(parser.itemsOf(RefItem.class)).thenReturn(offset);

    Reference reference = testee.parse();

    assertEquals(anchor, reference.getAnchor());
    assertEquals(offset, ((OffsetReference) reference).getOffset());

    order.verify(stream).elementStart(eq("Reference"));
    order.verify(parser).itemOf(eq(Anchor.class));
    order.verify(parser).itemsOf(eq(RefItem.class));
    order.verify(stream).elementEnd();
  }

}
