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

package ast.data.component.elementary;

import ast.data.AstList;
import ast.data.component.Component;
import ast.data.component.composition.ComponentUse;
import ast.data.component.composition.SubCallbacks;
import ast.data.reference.Reference;
import ast.data.type.Type;
import ast.data.variable.Constant;
import ast.data.variable.Variable;

final public class ImplElementary extends Component {
  final public AstList<Type> type = new AstList<Type>();
  final public AstList<Variable> variable = new AstList<Variable>();
  final public AstList<Constant> constant = new AstList<Constant>();
  final public AstList<ComponentUse> component = new AstList<ComponentUse>();
  final public AstList<SubCallbacks> subCallback = new AstList<SubCallbacks>();
  public Reference entryFunc;
  public Reference exitFunc;

  public ImplElementary(String name, Reference entryFunc, Reference exitFunc) {
    super(name);
    this.entryFunc = entryFunc;
    this.exitFunc = exitFunc;
  }

  @Deprecated
  public SubCallbacks getSubCallback(ComponentUse use) {
    for (SubCallbacks itr : subCallback) {
      if (itr.compUse.getTarget() == use) {
        return itr;
      }
    }
    return null;
  }

}
