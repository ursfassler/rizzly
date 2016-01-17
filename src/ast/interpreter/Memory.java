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

package ast.interpreter;

import java.util.HashMap;
import java.util.Map;

import ast.data.expression.value.ValueExpr;
import ast.data.variable.Variable;
import error.ErrorType;
import error.RError;

public class Memory {
  private Map<Variable, ValueExpr> values = new HashMap<Variable, ValueExpr>();

  public Memory() {
  }

  public Memory(Memory param) {
    values.putAll(param.values);
  }

  public void createVar(Variable var) {
    assert (var != null);
    if (values.containsKey(var)) {
      RError.err(ErrorType.Fatal, "Variable exists: " + var, var.metadata());
    }

    // assert( var.getType().getOffset().isEmpty() );
    // Type type = (Type) var.getType().getLink();
    // values.put(var, ValueCreator.INSTANCE.traverse(type, null));
    values.put(var, null);
  }

  public void set(Variable var, ValueExpr value) {
    assert (var != null);
    assert (values.containsKey(var));
    values.put(var, value);
  }

  public ValueExpr get(Variable var) {
    assert (var != null);
    assert (values.containsKey(var));
    ValueExpr expr = values.get(var);
    assert (expr != null);
    return expr;
  }

  public boolean contains(Variable name) {
    return values.containsKey(name);
  }

  public Variable find(String name) {
    for (Variable var : values.keySet()) {
      if (var.getName().equals(name)) {
        return var;
      }
    }
    return null;
  }
}
