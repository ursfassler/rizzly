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

package ast.repository.query;

import ast.data.component.Component;
import ast.data.component.composition.ComponentUse;
import ast.data.component.composition.EndpointRaw;
import ast.data.component.composition.EndpointSelf;
import ast.data.component.composition.EndpointSub;
import ast.data.function.Function;
import ast.pass.check.model.composition.EndpointDescriptor;
import ast.visitor.Visitor;

public class EndpointFunctionQuery implements Visitor {
  private Component componentType = null;
  private String instanceName = null;
  private Function function = null;

  public Function getFunction() {
    return function;
  }

  public Component getComponentType() {
    return componentType;
  }

  public String getInstanceName() {
    return instanceName;
  }

  public EndpointDescriptor descriptor() {
    return new EndpointDescriptor(getComponentType(), getInstanceName(), getFunction());
  }

  public void visit(EndpointSelf object) {
    instanceName = "";
    componentType = null;
    function = (Function) object.getFuncRef().getTarget();
  }

  public void visit(EndpointRaw object) {
    instanceName = "";
    componentType = null;
    function = (Function) object.getRef().getTarget();
  }

  public void visit(EndpointSub object) {
    ComponentUse compRef = (ComponentUse) object.getComponent().getTarget();
    instanceName = compRef.getName();
    componentType = (Component) compRef.getCompRef().getTarget();
    function = NameFilter.select(componentType.iface, object.getFunction());
  }
}
