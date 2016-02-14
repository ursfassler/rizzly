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

import ast.data.Ast;
import ast.data.AstList;
import ast.data.Namespace;
import ast.pass.input.xml.scanner.ExpectionParser;
import error.RizzlyError;

public class XmlTopParser_Test {
  final private ExpectionParser expected = mock(ExpectionParser.class);
  final private RizzlyError error = mock(RizzlyError.class);
  final private XmlParser parser = mock(XmlParser.class);
  final private XmlTopParser testee = new XmlTopParser(expected, parser, error);
  final private InOrder order = Mockito.inOrder(expected, parser);

  @Test
  public void has_correct_name() {
    assertEquals("rizzly", testee.name());
  }

  @Test
  public void parse_xml_file() {
    Ast child = mock(Ast.class);
    AstList<Ast> children = new AstList<Ast>();
    children.add(child);
    when(parser.astItems()).thenReturn(children);

    Namespace namespace = testee.parse();

    assertEquals(children, namespace.children);
    assertEquals("!", namespace.getName());

    order.verify(expected).elementStart(eq("rizzly"));
    order.verify(parser).astItems();
    order.verify(expected).elementEnd();
  }

}
