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
import ast.data.function.FunctionProperty;
import ast.data.function.header.FuncFunction;
import ast.data.function.header.Procedure;
import ast.data.function.header.Response;
import ast.data.function.header.Signal;
import ast.data.function.header.Slot;
import ast.data.function.ret.FuncReturn;
import ast.data.function.ret.FunctionReturnType;
import ast.data.statement.Block;
import ast.data.type.TypeReference;
import ast.data.variable.FunctionVariable;
import ast.meta.MetaInformation;
import ast.pass.output.xml.IdReader;
import ast.visitor.Visitor;

public class Writer_Function_Test {
  final private XmlStreamWriter stream = mock(XmlStreamWriter.class);
  final private IdReader astId = mock(IdReader.class);
  final private Visitor idWriter = mock(Visitor.class);
  final private Write testee = new Write(stream, astId, idWriter);
  final private MetaInformation info = mock(MetaInformation.class);
  final private FunctionVariable parameter1 = mock(FunctionVariable.class);
  final private FuncReturn ret = mock(FuncReturn.class);
  final private Block body = mock(Block.class);
  final private TypeReference typeReference = mock(TypeReference.class);
  final private InOrder order = Mockito.inOrder(stream, info, parameter1, ret, body, typeReference, idWriter);
  final private AstList<FunctionVariable> parameter;

  public Writer_Function_Test() {
    parameter = new AstList<FunctionVariable>();
    parameter.add(parameter1);
  }

  @Test
  public void write_Slot() {
    Slot item = new Slot("slotname", parameter, ret, body);// TODO remove ret
    item.property = FunctionProperty.Public;
    item.metadata().add(info);

    testee.visit(item);

    order.verify(stream).beginNode(eq("Slot"));
    order.verify(stream).attribute("scope", "public");
    order.verify(stream).attribute("name", "slotname");
    order.verify(idWriter).visit(item);
    order.verify(info).accept(eq(testee));
    order.verify(parameter1).accept(eq(testee));
    order.verify(body).accept(eq(testee));
    order.verify(stream).endNode();
  }

  @Test
  public void write_Signal() {
    Signal item = new Signal("signalname", parameter, ret, body);// TODO remove ret
    item.property = FunctionProperty.External;
    item.metadata().add(info);

    testee.visit(item);

    order.verify(stream).beginNode(eq("Signal"));
    order.verify(stream).attribute("scope", "extern");
    order.verify(stream).attribute("name", "signalname");
    order.verify(idWriter).visit(item);
    order.verify(info).accept(eq(testee));
    order.verify(parameter1).accept(eq(testee));
    order.verify(body).accept(eq(testee));
    order.verify(stream).endNode();
  }

  @Test
  public void write_Function() {
    FuncFunction item = new FuncFunction("function name", parameter, ret, body);
    item.property = FunctionProperty.Private;
    item.metadata().add(info);

    testee.visit(item);

    order.verify(stream).beginNode(eq("Function"));
    order.verify(stream).attribute("scope", "private");
    order.verify(stream).attribute("name", "function name");
    order.verify(idWriter).visit(item);
    order.verify(info).accept(eq(testee));
    order.verify(parameter1).accept(eq(testee));
    order.verify(ret).accept(eq(testee));
    order.verify(body).accept(eq(testee));
    order.verify(stream).endNode();
  }

  @Test
  public void write_Response() {
    Response item = new Response("response name", parameter, ret, body);
    item.property = FunctionProperty.Public;
    item.metadata().add(info);

    testee.visit(item);

    order.verify(stream).beginNode(eq("Response"));
    order.verify(stream).attribute("scope", "public");
    order.verify(stream).attribute("name", "response name");
    order.verify(idWriter).visit(item);
    order.verify(info).accept(eq(testee));
    order.verify(parameter1).accept(eq(testee));
    order.verify(ret).accept(eq(testee));
    order.verify(body).accept(eq(testee));
    order.verify(stream).endNode();
  }

  @Test
  public void write_Procedure() {
    Procedure item = new Procedure("the procedure", parameter, ret, body);// TODO remove ret
    item.property = FunctionProperty.External;
    item.metadata().add(info);

    testee.visit(item);

    order.verify(stream).beginNode(eq("Procedure"));
    order.verify(stream).attribute("scope", "extern");
    order.verify(stream).attribute("name", "the procedure");
    order.verify(idWriter).visit(item);
    order.verify(info).accept(eq(testee));
    order.verify(parameter1).accept(eq(testee));
    order.verify(body).accept(eq(testee));
    order.verify(stream).endNode();
  }

  @Test
  public void write_ReturnType() {
    FunctionReturnType item = new FunctionReturnType(typeReference);
    item.metadata().add(info);

    testee.visit(item);

    order.verify(stream).beginNode(eq("ReturnType"));
    order.verify(idWriter).visit(item);
    order.verify(info).accept(eq(testee));
    order.verify(typeReference).accept(eq(testee));
    order.verify(stream).endNode();
  }
}
