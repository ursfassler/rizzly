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

package cir.other;

import java.util.ArrayList;
import java.util.List;

import cir.CirBase;
import cir.function.Function;
import cir.type.Type;
import cir.variable.Variable;

public class Program extends CirBase {
  final private String name;
  final private List<Type> type = new ArrayList<Type>();
  final private List<Variable> variable = new ArrayList<Variable>();
  final private List<Function> function = new ArrayList<Function>();

  public Program(String name) {
    super();
    this.name = name;
  }

  public String getName() {
    return name;
  }

  public List<Type> getType() {
    return type;
  }

  public List<Variable> getVariable() {
    return variable;
  }

  public List<Function> getFunction() {
    return function;
  }

}
