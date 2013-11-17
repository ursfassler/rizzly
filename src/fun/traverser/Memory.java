package fun.traverser;

import java.util.HashMap;
import java.util.Map;

import error.ErrorType;
import error.RError;
import fun.expression.Expression;
import fun.expression.reference.Reference;
import fun.variable.Variable;

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

  public Expression get(Variable var) {
    assert (var != null);
    assert (values.containsKey(var));
    Expression expr = values.get(var);
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
