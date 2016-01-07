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

package ast.pass.check.model.composition;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Ignore;
import org.junit.Test;

import ast.ElementInfo;
import ast.data.AstList;
import ast.data.component.composition.Connection;
import ast.data.component.composition.Endpoint;
import ast.data.component.composition.SynchroniusConnection;
import ast.data.function.Function;
import ast.data.function.header.FuncFunction;
import ast.data.variable.FuncVariable;
import error.ErrorType;
import error.RizzlyError;

public class QueryIsConnectedToOneResponse_Test {
  private final RizzlyError error = mock(RizzlyError.class);
  final private QueryIsConnectedToOneResponse testee = new QueryIsConnectedToOneResponse(error);

  @Test
  public void does_nothing_if_there_are_no_connections() {
    AstList<Connection> connections = new AstList<Connection>();

    testee.check(connections);

    verify(error, never()).err(any(ErrorType.class), any(ElementInfo.class), anyString());
  }

  @Test
  public void does_nothing_when_the_query_is_connected_to_one_response() {
    Endpoint src = mock(Endpoint.class);
    Function srcFunc = new FuncFunction(ElementInfo.NO, "", new AstList<FuncVariable>(), null, null);
    when(src.getFunc()).thenReturn(srcFunc);

    Endpoint dst = mock(Endpoint.class);

    SynchroniusConnection connection = new SynchroniusConnection(ElementInfo.NO, src, dst);
    AstList<Connection> connections = new AstList<Connection>();
    connections.add(connection);

    testee.check(connections);

    verify(error, never()).err(any(ErrorType.class), any(ElementInfo.class), anyString());
  }

  // TODO make tests easier: separate class to create connection graph; different checks over that

  // TODO make simpler
  // TODO need easy way to compare 2 endpoints (e.g. equals)
  @Test
  public void reports_an_error_when_a_query_is_connected_to_more_than_one_response() {
    Function srcFunc = new FuncFunction(ElementInfo.NO, "", new AstList<FuncVariable>(), null, null);

    Endpoint src = mock(Endpoint.class);
    Endpoint dst1 = mock(Endpoint.class);
    Endpoint dst2 = mock(Endpoint.class);
    when(src.getFunc()).thenReturn(srcFunc);

    ElementInfo info1 = mock(ElementInfo.class);
    Connection connection1 = new SynchroniusConnection(info1, src, dst1);

    ElementInfo info2 = mock(ElementInfo.class);
    Connection connection2 = new SynchroniusConnection(info2, src, dst2);

    AstList<Connection> connections = new AstList<Connection>();
    connections.add(connection1);
    connections.add(connection2);

    testee.check(connections);

    verify(error).err(ErrorType.Hint, info1, "previous connection was here");
    verify(error).err(ErrorType.Error, info2, "query needs exactly one connection, got more");
  }

  @Ignore
  @Test
  public void no_error_when_a_2_queries_are_connected_to_different_responses() {
    Endpoint src1 = mock(Endpoint.class);
    Endpoint src2 = mock(Endpoint.class);
    Endpoint dst1 = mock(Endpoint.class);
    Endpoint dst2 = mock(Endpoint.class);

    ElementInfo info1 = mock(ElementInfo.class);
    Connection connection1 = new SynchroniusConnection(info1, src1, dst1);

    ElementInfo info2 = mock(ElementInfo.class);
    Connection connection2 = new SynchroniusConnection(info2, src2, dst2);

    AstList<Connection> connections = new AstList<Connection>();
    connections.add(connection1);
    connections.add(connection2);

    testee.check(connections);

    verify(error, never()).err(any(ErrorType.class), any(ElementInfo.class), anyString());
  }
}
