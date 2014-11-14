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

import evl.EvlBase;
import evl.function.Function;
import evl.type.Type;
import evl.variable.Constant;
import evl.variable.StateVariable;

public class RizzlyProgram extends EvlBase {
  private String name;
  private EvlList<Type> type = new EvlList<Type>();
  private EvlList<StateVariable> variable = new EvlList<StateVariable>();
  private EvlList<Constant> constant = new EvlList<Constant>();
  private EvlList<Function> function = new EvlList<Function>();

  public RizzlyProgram(String name) {
    super(ElementInfo.NO);
    this.name = name;
  }

  public EvlList<Type> getType() {
    return type;
  }

  public EvlList<StateVariable> getVariable() {
    return variable;
  }

  public EvlList<Function> getFunction() {
    return function;
  }

  public EvlList<Constant> getConstant() {
    return constant;
  }

  public String getName() {
    return name;
  }

}
