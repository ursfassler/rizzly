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
import ast.data.template.Template;
import ast.data.type.TypeReference;
import ast.data.variable.TemplateParameter;
import ast.meta.MetaInformation;

public class Writer_Template_Test {
  final private XmlStreamWriter stream = mock(XmlStreamWriter.class);
  final private Write testee = new Write(stream);
  final private MetaInformation info = mock(MetaInformation.class);
  final private TemplateParameter parameter1 = mock(TemplateParameter.class);
  final private TemplateParameter parameter2 = mock(TemplateParameter.class);
  final private Named object = mock(Named.class);
  final private TypeReference typeReference = mock(TypeReference.class);
  final private InOrder order = Mockito.inOrder(stream, info, parameter1, parameter2, typeReference, object);
  final private AstList<TemplateParameter> parameter;

  public Writer_Template_Test() {
    super();
    parameter = new AstList<TemplateParameter>();
    parameter.add(parameter1);
    parameter.add(parameter2);
  }

  @Test
  public void write_Template() {
    Template item = new Template("the Template", parameter, object);
    item.metadata().add(info);

    testee.visit(item);

    order.verify(stream).beginNode(eq("Template"));
    order.verify(stream).attribute("name", "the Template");
    order.verify(info).accept(eq(testee));
    order.verify(parameter1).accept(eq(testee));
    order.verify(parameter2).accept(eq(testee));
    order.verify(object).accept(eq(testee));
    order.verify(stream).endNode();
  }

  @Test
  public void write_TemplateParameter() {
    TemplateParameter item = new TemplateParameter("the param", typeReference);
    item.metadata().add(info);

    testee.visit(item);

    order.verify(stream).beginNode(eq("TemplateParameter"));
    order.verify(stream).attribute("name", "the param");
    order.verify(info).accept(eq(testee));
    order.verify(typeReference).accept(eq(testee));
    order.verify(stream).endNode();
  }

}
