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

package evl.other;

import common.ElementInfo;

import evl.expression.reference.SimpleRef;
import evl.function.header.FuncPrivateVoid;
import evl.variable.Constant;
import evl.variable.Variable;

final public class ImplElementary extends Component {
  final private EvlList<Variable> variable = new EvlList<Variable>();
  final private EvlList<Constant> constant = new EvlList<Constant>();
  final private EvlList<CompUse> component = new EvlList<CompUse>();
  final private EvlList<SubCallbacks> subCallback = new EvlList<SubCallbacks>();
  final private SimpleRef<FuncPrivateVoid> entryFunc;
  final private SimpleRef<FuncPrivateVoid> exitFunc;

  public ImplElementary(ElementInfo info, String name, SimpleRef<FuncPrivateVoid> entryFunc, SimpleRef<FuncPrivateVoid> exitFunc) {
    super(info, name);
    this.entryFunc = entryFunc;
    this.exitFunc = exitFunc;
  }

  public EvlList<Variable> getVariable() {
    return variable;
  }

  public EvlList<Constant> getConstant() {
    return constant;
  }

  public EvlList<CompUse> getComponent() {
    return component;
  }

  public EvlList<SubCallbacks> getSubCallback() {
    return subCallback;
  }

  public SubCallbacks getSubCallback(CompUse use) {
    for (SubCallbacks itr : subCallback) {
      if (itr.getCompUse().getLink() == use) {
        return itr;
      }
    }
    return null;
  }

  public SimpleRef<FuncPrivateVoid> getEntryFunc() {
    return entryFunc;
  }

  public SimpleRef<FuncPrivateVoid> getExitFunc() {
    return exitFunc;
  }

}
