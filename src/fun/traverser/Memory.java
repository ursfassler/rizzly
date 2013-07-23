package fun.traverser;

import java.util.HashMap;
import java.util.Map;

import error.ErrorType;
import error.RError;
import fun.expression.Expression;
import fun.expression.reference.Reference;
import fun.variable.Variable;

public class Memory {
  private Map<Variable, Expression> varInt = new HashMap<Variable, Expression>();

  public Memory() {
  }

  public Memory(Memory param) {
    varInt.putAll(param.varInt);
  }

  public void createVar(Variable var) {
    assert (var != null);
    if (varInt.containsKey(var)) {
      RError.err(ErrorType.Fatal, var.getInfo(), "Variable exists: " + var);
    }
    varInt.put(var, null);
  }

  public void setInt(Variable var, Expression value) {
    assert (var != null);
    assert (varInt.containsKey(var));
    assert (!(value instanceof Reference));
    varInt.put(var, value);
  }

  public Expression getInt(Variable var) {
    assert (var != null);
    assert (varInt.containsKey(var));
    Expression expr = varInt.get(var);
    assert (expr != null);
    return expr;
  }

  public boolean contains(Variable name) {
    return varInt.containsKey(name);
  }

  public Variable find(String name) {
    for (Variable var : varInt.keySet()) {
      if (var.getName().equals(name)) {
        return var;
      }
    }
    return null;
  }
}
