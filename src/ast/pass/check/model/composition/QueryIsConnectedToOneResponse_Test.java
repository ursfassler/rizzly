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

import ast.ElementInfo;
import ast.data.AstList;
import ast.data.component.composition.Connection;
import ast.data.component.composition.EndpointSelf;
import ast.data.component.composition.SynchroniusConnection;
import ast.data.function.FuncRef;
import ast.data.function.header.FuncFunction;
import ast.data.reference.RefItem;
import ast.data.reference.Reference;
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

  // TODO extend to EndpointSub or unify EndpointSub and EndpointSelf

  @Test
  public void does_nothing_when_the_query_is_connected_to_one_response() {
    EndpointSelf src = selfEp();
    EndpointSelf dst = selfEp();

    SynchroniusConnection connection = new SynchroniusConnection(ElementInfo.NO, src, dst);
    AstList<Connection> connections = new AstList<Connection>();
    connections.add(connection);

    testee.check(connections);

    verify(error, never()).err(any(ErrorType.class), any(ElementInfo.class), anyString());
  }

  @Test
  public void reports_an_error_when_a_query_is_connected_to_more_than_one_response() {
    FuncFunction srcFunc = new FuncFunction(ElementInfo.NO, "", new AstList<FuncVariable>(), null, null);

    EndpointSelf src1 = selfEp(srcFunc);
    EndpointSelf src2 = selfEp(srcFunc);
    EndpointSelf dst1 = selfEp();
    EndpointSelf dst2 = selfEp();

    ElementInfo info1 = mock(ElementInfo.class);
    Connection connection1 = new SynchroniusConnection(info1, src1, dst1);

    ElementInfo info2 = mock(ElementInfo.class);
    Connection connection2 = new SynchroniusConnection(info2, src2, dst2);

    AstList<Connection> connections = new AstList<Connection>();
    connections.add(connection1);
    connections.add(connection2);

    testee.check(connections);

    verify(error).err(ErrorType.Hint, info1, "previous connection was here");
    verify(error).err(ErrorType.Error, info2, "query needs exactly one connection, got more");
  }

  @Test
  public void no_error_when_2_queries_are_connected_to_different_responses() {
    EndpointSelf src1 = selfEp();
    EndpointSelf src2 = selfEp();
    EndpointSelf dst1 = selfEp();
    EndpointSelf dst2 = selfEp();

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

  private EndpointSelf selfEp() {
    FuncFunction function = new FuncFunction(ElementInfo.NO, "", new AstList<FuncVariable>(), null, null);
    return selfEp(function);
  }

  private EndpointSelf selfEp(FuncFunction function) {
    return new EndpointSelf(null, new FuncRef(null, new Reference(null, function, new AstList<RefItem>())));
  }

}
