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

import util.Pair;
import ast.data.AstList;
import ast.data.component.composition.CompUse;
import ast.data.component.composition.Connection;
import ast.data.component.composition.Endpoint;
import ast.data.component.composition.EndpointRaw;
import ast.data.component.composition.EndpointSelf;
import ast.data.component.composition.EndpointSub;
import ast.data.function.Function;
import ast.data.function.ret.FuncReturn;
import ast.data.function.ret.FuncReturnNone;
import ast.visitor.NullVisitor;
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
    Map<Pair<CompUse, Function>, Connection> graph = new HashMap<Pair<CompUse, Function>, Connection>();

    for (Connection connection : connections) {
      if (isQuery(connection)) {
        Endpoint endpoint = connection.getSrc();
        Pair<CompUse, Function> query = getDescriptor(endpoint);

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

  private boolean isQuery(Connection connection) {
    Endpoint ep = connection.getSrc();
    FuncReturn ret = ep.getFunc().ret;
    return !(ret instanceof FuncReturnNone);
  }

  private Pair<CompUse, Function> getDescriptor(Endpoint endpoint) {
    DescriptorBuilder visitor = new DescriptorBuilder();
    endpoint.accept(visitor);
    return visitor.descriptor;
  }

}

class DescriptorBuilder extends NullVisitor {
  public Pair<CompUse, Function> descriptor = null;

  @Override
  public void visit(EndpointRaw endpointRaw) {
    throw new RuntimeException("not yet implemented");
  }

  @Override
  public void visit(EndpointSelf endpointSelf) {
    CompUse first = null;
    Function second = endpointSelf.getFunc();
    descriptor = new Pair<CompUse, Function>(first, second);
  }

  @Override
  public void visit(EndpointSub endpointSub) {
    CompUse first = endpointSub.component.getTarget();
    Function second = endpointSub.getFunc();
    descriptor = new Pair<CompUse, Function>(first, second);
  }

}
