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

import java.math.BigInteger;

import org.junit.Test;
import org.mockito.InOrder;
import org.mockito.Mockito;

import ast.data.Range;
import ast.data.reference.Reference;
import ast.data.type.base.BooleanType;
import ast.data.type.base.RangeType;
import ast.data.type.base.StringType;
import ast.data.type.special.AnyType;
import ast.data.type.special.IntegerType;
import ast.data.type.special.NaturalType;
import ast.data.type.special.TypeType;
import ast.data.type.special.VoidType;
import ast.pass.output.xml.IdReader;
import ast.visitor.VisitExecutor;
import ast.visitor.Visitor;

public class Writer_Type_Test {
  final private XmlStreamWriter stream = mock(XmlStreamWriter.class);
  final private IdReader astId = mock(IdReader.class);
  final private Visitor idWriter = mock(Visitor.class);
  final private VisitExecutor executor = mock(VisitExecutor.class);
  final private Write testee = new Write(stream, astId, idWriter, executor);
  final private InOrder order = Mockito.inOrder(stream, idWriter, executor);

  @Test
  public void write_BooleanType() {
    BooleanType item = new BooleanType();

    testee.visit(item);

    order.verify(stream).beginNode(eq("Boolean"));
    order.verify(executor).visit(eq(idWriter), eq(item));
    order.verify(stream).endNode();
  }

  @Test
  public void write_IntegerType() {
    IntegerType item = new IntegerType();

    testee.visit(item);

    order.verify(stream).beginNode(eq("Integer"));
    order.verify(executor).visit(eq(idWriter), eq(item));
    order.verify(stream).endNode();
  }

  @Test
  public void write_NaturalType() {
    NaturalType item = new NaturalType();

    testee.visit(item);

    order.verify(stream).beginNode(eq("Natural"));
    order.verify(executor).visit(eq(idWriter), eq(item));
    order.verify(stream).endNode();
  }

  @Test
  public void write_AnyType() {
    AnyType item = new AnyType();

    testee.visit(item);

    order.verify(stream).beginNode(eq("AnyType"));
    order.verify(executor).visit(eq(idWriter), eq(item));
    order.verify(stream).endNode();
  }

  @Test
  public void write_TypeType() {
    Reference typeReference = mock(Reference.class);
    TypeType item = new TypeType("the name", typeReference);

    testee.visit(item);

    order.verify(stream).beginNode(eq("TypeType"));
    order.verify(stream).attribute("name", "the name");
    order.verify(executor).visit(eq(idWriter), eq(item));
    order.verify(executor).visit(eq(testee), eq(item.metadata()));
    order.verify(executor).visit(eq(testee), eq(item.type));
    order.verify(stream).endNode();
  }

  @Test
  public void write_VoidType() {
    VoidType item = new VoidType();

    testee.visit(item);

    order.verify(stream).beginNode(eq("Void"));
    order.verify(executor).visit(eq(idWriter), eq(item));
    order.verify(stream).endNode();
  }

  @Test
  public void write_StringType() {
    StringType item = new StringType();

    testee.visit(item);

    order.verify(stream).beginNode(eq("String"));
    order.verify(executor).visit(eq(idWriter), eq(item));
    order.verify(stream).endNode();
  }

  @Test
  public void write_RangeType() {
    RangeType item = new RangeType("the name", new Range(BigInteger.valueOf(-3), BigInteger.valueOf(57)));

    testee.visit(item);

    order.verify(stream).beginNode(eq("RangeType"));
    order.verify(stream).attribute("low", "-3");
    order.verify(stream).attribute("high", "57");
    order.verify(stream).attribute("name", "the name");
    order.verify(executor).visit(eq(idWriter), eq(item));
    order.verify(stream).endNode();
  }

}
