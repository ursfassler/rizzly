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
import ast.repository.query.Referencees.TargetResolver;
import ast.visitor.Visitor;

public class EndpointFunctionQuery implements Visitor {
  final private TargetResolver targetResolver;

  private Component componentType = null;
  private String instanceName = null;
  private Function function = null;

  public EndpointFunctionQuery(TargetResolver targetResolver) {
    super();
    this.targetResolver = targetResolver;
  }

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
    function = targetResolver.targetOf(object.getFuncRef(), Function.class);
  }

  public void visit(EndpointRaw object) {
    instanceName = "";
    componentType = null;
    function = targetResolver.targetOf(object.getRef(), Function.class);
  }

  public void visit(EndpointSub object) {
    ComponentUse compRef = targetResolver.targetOf(object.getComponent(), ComponentUse.class);
    instanceName = compRef.getName();
    componentType = targetResolver.targetOf(compRef.getCompRef(), Component.class);
    function = NameFilter.select(componentType.iface, object.getFunction());
  }
}
