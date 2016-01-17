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

import org.junit.Test;
import org.mockito.Mockito;

import ast.data.AstList;
import ast.data.component.composition.Connection;
import ast.data.component.composition.EndpointSelf;
import ast.data.component.composition.SynchroniusConnection;
import ast.data.function.FuncRef;
import ast.data.function.header.FuncFunction;
import ast.data.reference.RefItem;
import ast.data.reference.Reference;
import ast.data.variable.FunctionVariable;
import ast.meta.MetaList;
import error.ErrorType;
import error.RizzlyError;

public class QueryIsConnectedToOneResponse_Test {
  private final RizzlyError error = mock(RizzlyError.class);
  final private QueryIsConnectedToOneResponse testee = new QueryIsConnectedToOneResponse(error);

  @Test
  public void does_nothing_if_there_are_no_connections() {
    AstList<Connection> connections = new AstList<Connection>();

    testee.check(connections);

    verify(error, never()).err(any(ErrorType.class), anyString(), any(MetaList.class));
  }

  // TODO extend to EndpointSub or unify EndpointSub and EndpointSelf

  @Test
  public void does_nothing_when_the_query_is_connected_to_one_response() {
    EndpointSelf src = selfEp();
    EndpointSelf dst = selfEp();

    SynchroniusConnection connection = new SynchroniusConnection(src, dst);
    AstList<Connection> connections = new AstList<Connection>();
    connections.add(connection);

    testee.check(connections);

    verify(error, never()).err(any(ErrorType.class), anyString(), any(MetaList.class));
  }

  @Test
  public void reports_an_error_when_a_query_is_connected_to_more_than_one_response() {
    FuncFunction srcFunc = new FuncFunction("", new AstList<FunctionVariable>(), null, null);

    Connection connection1 = mock(Connection.class);
    EndpointSelf src1 = selfEp(srcFunc);
    Mockito.when(connection1.getSrc()).thenReturn(src1);
    MetaList info1 = mock(MetaList.class);
    Mockito.when(connection1.metadata()).thenReturn(info1);

    Connection connection2 = mock(Connection.class);
    EndpointSelf src2 = selfEp(srcFunc);
    Mockito.when(connection2.getSrc()).thenReturn(src2);
    MetaList info2 = mock(MetaList.class);
    Mockito.when(connection2.metadata()).thenReturn(info2);

    AstList<Connection> connections = new AstList<Connection>();
    connections.add(connection1);
    connections.add(connection2);

    testee.check(connections);

    verify(error).err(ErrorType.Hint, "previous connection was here", info1);
    verify(error).err(ErrorType.Error, "query needs exactly one connection, got more", info2);
  }

  @Test
  public void no_error_when_2_queries_are_connected_to_different_responses() {
    EndpointSelf src1 = selfEp();
    EndpointSelf src2 = selfEp();
    EndpointSelf dst1 = selfEp();
    EndpointSelf dst2 = selfEp();

    Connection connection1 = new SynchroniusConnection(src1, dst1);
    Connection connection2 = new SynchroniusConnection(src2, dst2);

    AstList<Connection> connections = new AstList<Connection>();
    connections.add(connection1);
    connections.add(connection2);

    testee.check(connections);

    verify(error, never()).err(any(ErrorType.class), anyString(), any(MetaList.class));
  }

  private EndpointSelf selfEp() {
    FuncFunction function = new FuncFunction("", new AstList<FunctionVariable>(), null, null);
    return selfEp(function);
  }

  private EndpointSelf selfEp(FuncFunction function) {
    return new EndpointSelf(new FuncRef(new Reference(function, new AstList<RefItem>())));
  }

}
