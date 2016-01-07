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
import ast.data.component.composition.Direction;
import ast.data.component.composition.Endpoint;
import ast.data.function.ret.FuncReturn;
import ast.data.function.ret.FuncReturnNone;
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
    Map<Endpoint, Connection> graph = new HashMap<Endpoint, Connection>();

    for (Connection connection : connections) {
      if (isQuery(connection)) {
        Endpoint query = connection.endpoint.get(Direction.in);

        if (graph.containsKey(query)) {
          Connection oldConnection = graph.get(query);
          error.err(ErrorType.Hint, oldConnection.getInfo(), "previous connection was here");
          error.err(ErrorType.Error, connection.getInfo(), "query needs exactly one connection, got more");

        } else {
          graph.put(query, connection);
        }
      }
    }

  }

  private boolean isQuery(Connection connection) {
    Endpoint ep = connection.endpoint.get(Direction.in);
    FuncReturn ret = ep.getFunc().ret;
    return !(ret instanceof FuncReturnNone);
  }
}
