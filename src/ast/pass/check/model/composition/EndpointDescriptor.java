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

import ast.data.component.Component;
import ast.data.function.Function;

public class EndpointDescriptor {
  public final Component component;
  public final String instanceName;
  public final Function function;

  public EndpointDescriptor(Component component, String instanceName, Function function) {
    this.component = component;
    this.instanceName = instanceName;
    this.function = function;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((component == null) ? 0 : component.hashCode());
    result = prime * result + ((function == null) ? 0 : function.hashCode());
    result = prime * result + ((instanceName == null) ? 0 : instanceName.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    EndpointDescriptor other = (EndpointDescriptor) obj;
    if (component == null) {
      if (other.component != null)
        return false;
    } else if (!component.equals(other.component))
      return false;
    if (function == null) {
      if (other.function != null)
        return false;
    } else if (!function.equals(other.function))
      return false;
    if (instanceName == null) {
      if (other.instanceName != null)
        return false;
    } else if (!instanceName.equals(other.instanceName))
      return false;
    return true;
  }

}
