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

import java.util.HashMap;
import java.util.Map;

import ast.data.AstList;
import ast.data.component.composition.Connection;
import ast.data.component.composition.Endpoint;
import ast.data.function.ret.FuncReturn;
import ast.data.function.ret.FuncReturnNone;
import ast.repository.query.EndpointFunctionQuery;
import ast.visitor.VisitExecutorImplementation;
import error.ErrorType;
import error.RizzlyError;

public class QueryIsConnectedToOneResponse {
  final private RizzlyError error;

  public QueryIsConnectedToOneResponse(RizzlyError error) {
    super();
    this.error = error;
  }

  // TODO this does not work since the endpoints can not be compared with equals
  public void check(AstList<Connection> connections) {
    Map<EndpointDescriptor, Connection> graph = new HashMap<EndpointDescriptor, Connection>();

    for (Connection connection : connections) {
      Endpoint endpoint = connection.getSrc();
      EndpointDescriptor query = getDescriptor(endpoint);

      if (isQuery(query)) {
        if (graph.containsKey(query)) {
          Connection oldConnection = graph.get(query);
          error.err(ErrorType.Hint, "previous connection was here", oldConnection.metadata());
          error.err(ErrorType.Error, "query needs exactly one connection, got more", connection.metadata());
        } else {
          graph.put(query, connection);
        }
      }
    }

  }

  private boolean isQuery(EndpointDescriptor descriptor) {
    FuncReturn ret = descriptor.function.ret;
    return !(ret instanceof FuncReturnNone);
  }

  private EndpointDescriptor getDescriptor(Endpoint endpoint) {
    EndpointFunctionQuery visitor = new EndpointFunctionQuery();
    VisitExecutorImplementation.instance().visit(visitor, endpoint);
    return new EndpointDescriptor(visitor.getComponentType(), visitor.getInstanceName(), visitor.getFunction());
  }

}
