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
import ast.data.reference.Reference;
import ast.data.template.Template;
import ast.data.variable.TemplateParameter;
import ast.pass.output.xml.IdReader;
import ast.visitor.VisitExecutor;
import ast.visitor.Visitor;

public class Writer_Template_Test {
  final private XmlStreamWriter stream = mock(XmlStreamWriter.class);
  final private IdReader astId = mock(IdReader.class);
  final private Visitor idWriter = mock(Visitor.class);
  final private VisitExecutor executor = mock(VisitExecutor.class);
  final private Write testee = new Write(stream, astId, idWriter, executor);
  final private InOrder order = Mockito.inOrder(stream, idWriter, executor);

  @Test
  public void write_Template() {
    AstList<TemplateParameter> parameter = new AstList<TemplateParameter>();
    Named object = mock(Named.class);
    Template item = new Template("the Template", parameter, object);

    testee.visit(item);

    order.verify(stream).beginNode(eq("Template"));
    order.verify(stream).attribute("name", "the Template");
    order.verify(executor).visit(eq(idWriter), eq(item));
    order.verify(executor).visit(eq(testee), eq(item.metadata()));
    order.verify(executor).visit(eq(testee), eq(item.getTempl()));
    order.verify(executor).visit(eq(testee), eq(item.getObject()));
    order.verify(stream).endNode();
  }

  @Test
  public void write_TemplateParameter() {
    Reference typeReference = mock(Reference.class);
    TemplateParameter item = new TemplateParameter("the param", typeReference);

    testee.visit(item);

    order.verify(stream).beginNode(eq("TemplateParameter"));
    order.verify(stream).attribute("name", "the param");
    order.verify(executor).visit(eq(idWriter), eq(item));
    order.verify(executor).visit(eq(testee), eq(item.metadata()));
    order.verify(executor).visit(eq(testee), eq(item.type));
    order.verify(stream).endNode();
  }

}
