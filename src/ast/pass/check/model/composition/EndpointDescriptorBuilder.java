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

import util.Pair;
import ast.data.component.composition.ComponentUse;
import ast.data.component.composition.EndpointRaw;
import ast.data.component.composition.EndpointSelf;
import ast.data.component.composition.EndpointSub;
import ast.data.function.Function;
import ast.visitor.Visitor;

public class EndpointDescriptorBuilder implements Visitor {
  public Pair<ComponentUse, Function> descriptor = null;

  public void visit(EndpointRaw endpointRaw) {
    throw new RuntimeException("not yet implemented");
  }

  public void visit(EndpointSelf endpointSelf) {
    ComponentUse first = null;
    Function second = endpointSelf.getFunc();
    descriptor = new Pair<ComponentUse, Function>(first, second);
  }

  public void visit(EndpointSub endpointSub) {
    ComponentUse first = (ComponentUse) endpointSub.component.getTarget();
    Function second = endpointSub.getFunc();
    descriptor = new Pair<ComponentUse, Function>(first, second);
  }

}
