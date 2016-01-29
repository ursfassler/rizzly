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

package ast.data.component.composition;

import ast.data.component.Component;
import ast.data.function.Function;
import ast.meta.MetaList;
import ast.repository.query.NameFilter;
import ast.visitor.Visitor;

final public class EndpointSub extends Endpoint {
  final public CompUseRef component;
  final public String function;

  public EndpointSub(CompUseRef component, String function) {
    this.component = component;
    this.function = function;
  }

  @Deprecated
  public EndpointSub(MetaList info, CompUseRef component, String function) {
    metadata().add(info);
    this.component = component;
    this.function = function;
  }

  @Override
  public Function getFunc() {
    Component comp = component.getTarget().compRef.getTarget();
    return NameFilter.select(comp.iface, function);
  }

  @Override
  public String toString() {
    return component.getTarget().getName() + "." + function;
  }


}
