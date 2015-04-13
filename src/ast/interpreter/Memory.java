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

import ast.data.expression.Expression;
import ast.data.expression.reference.Reference;
import ast.data.variable.Variable;
import error.ErrorType;
import error.RError;

public class Memory {
  private Map<Variable, Expression> values = new HashMap<Variable, Expression>();

  public Memory() {
  }

  public Memory(Memory param) {
    values.putAll(param.values);
  }

  public void createVar(Variable var) {
    assert (var != null);
    if (values.containsKey(var)) {
      RError.err(ErrorType.Fatal, var.getInfo(), "Variable exists: " + var);
    }

    // assert( var.getType().getOffset().isEmpty() );
    // Type type = (Type) var.getType().getLink();
    // values.put(var, ValueCreator.INSTANCE.traverse(type, null));
    values.put(var, null);
  }

  public void set(Variable var, Expression value) {
    assert (var != null);
    assert (values.containsKey(var));
    assert (!(value instanceof Reference));
    values.put(var, value);
  }

  public Expression get(ast.data.variable.Variable var) {
    assert (var != null);
    assert (values.containsKey(var));
    Expression expr = values.get(var);
    assert (expr != null);
    return expr;
  }

  public boolean contains(ast.data.variable.Variable name) {
    return values.containsKey(name);
  }

  public ast.data.variable.Variable find(String name) {
    for (ast.data.variable.Variable var : values.keySet()) {
      if (var.name.equals(name)) {
        return var;
      }
    }
    return null;
  }
}