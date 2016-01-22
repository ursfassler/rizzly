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

package ast.pass.specializer;

import java.util.List;

import ast.copy.Copy;
import ast.data.Ast;
import ast.data.AstList;
import ast.data.expression.Expression;
import ast.data.expression.value.AnyValue;
import ast.data.expression.value.BooleanValue;
import ast.data.expression.value.TupleValue;
import ast.data.expression.value.ValueExpr;
import ast.data.function.header.FuncFunction;
import ast.data.reference.LinkedReferenceWithOffset;
import ast.data.statement.Block;
import ast.data.statement.ExpressionReturn;
import ast.data.statement.IfOption;
import ast.data.statement.IfStatement;
import ast.data.statement.MultiAssignment;
import ast.data.statement.Statement;
import ast.data.statement.VarDefInitStmt;
import ast.data.statement.VoidReturn;
import ast.data.statement.WhileStmt;
import ast.data.type.Type;
import ast.data.variable.FunctionVariable;
import ast.data.variable.Variable;
import ast.dispatcher.NullDispatcher;
import ast.interpreter.Memory;
import ast.knowledge.KnowEmptyValue;
import ast.knowledge.KnowledgeBase;
import error.RError;

/**
 * If a visit() returns null, this means normal execution. If !null is returned, this means the function called "return"
 * and execution is aborted.
 *
 * @author urs
 *
 */
public class StmtExecutor extends NullDispatcher<Expression, Memory> {
  private final KnowledgeBase kb;

  public StmtExecutor(KnowledgeBase kb) {
    super();
    this.kb = kb;
  }

  public static Expression process(FuncFunction func, List<Expression> actparam, Memory mem, KnowledgeBase kb) {
    Memory memory = new Memory(mem);

    assert (func.param.size() == actparam.size());

    for (int i = 0; i < actparam.size(); i++) {
      FunctionVariable var = func.param.get(i);
      ValueExpr val = ExprEvaluator.evaluate(actparam.get(i), mem, kb);
      memory.createVar(var);
      memory.set(var, val);
    }

    StmtExecutor executor = new StmtExecutor(kb);
    Expression ret = executor.traverse(func, memory);
    assert (ret != null);
    return ret;
  }

  private ValueExpr exeval(Expression expr, Memory mem) {
    return ExprEvaluator.evaluate(expr, mem, kb);
  }

  private boolean toBool(Expression expr) {
    assert (expr instanceof BooleanValue);
    return ((BooleanValue) expr).value;
  }

  @Override
  protected Expression visitDefault(Ast obj, Memory param) {
    throw new RuntimeException("not yet implemented: " + obj.getClass().getCanonicalName());
  }

  @Override
  protected Expression visitFuncFunction(FuncFunction obj, Memory param) {
    for (Statement stmt : obj.body.statements) {
      Expression ret = visit(stmt, param);
      if (ret != null) {
        return ret;
      }
    }
    return new AnyValue();
  }

  @Override
  protected Expression visitBlock(Block obj, Memory param) {
    for (Statement stmt : obj.statements) {
      Expression ret = visit(stmt, param);
      if (ret != null) {
        return ret;
      }
    }
    return null;
  }

  @Override
  protected Expression visitVarDefInitStmt(VarDefInitStmt obj, Memory param) {
    ValueExpr value = exeval(obj.initial, param);

    for (Variable var : obj.variable) {
      param.createVar(var);
      if (value instanceof AnyValue) {
        Type type = (Type) var.type.ref.getTarget();
        ValueExpr empty = kb.getEntry(KnowEmptyValue.class).get(type);
        param.set(var, Copy.copy(empty));
      } else {
        param.set(var, Copy.copy(value));
      }
    }

    return null;
  }

  @Override
  protected Expression visitAssignmentMulti(MultiAssignment obj, Memory param) {
    Expression rhs = exeval(obj.right, param);

    AstList<Expression> value;

    if (obj.left.size() > 1) {
      RError.ass(rhs instanceof TupleValue, obj.metadata(), "expected tuple on the right");
      value = ((TupleValue) rhs).value;
    } else {
      value = new AstList<Expression>();
      value.add(rhs);
    }

    // FIXME what if a function call is on the rhs?
    RError.ass(obj.left.size() == value.size(), obj.metadata(), "expect same number of elemnts on both sides, got " + obj.left.size() + " <-> " + value.size());

    for (int i = 0; i < value.size(); i++) {
      assign(obj.left.get(i), value.get(i), param);
    }

    return null;
  }

  private void assign(LinkedReferenceWithOffset lhs, Expression rhs, Memory param) {
    rhs = Copy.copy(rhs);

    Variable var = (Variable) lhs.getLink();
    ValueExpr root = param.get(var);

    Ast lvalue = RefEvaluator.execute(root, lhs.getOffset(), param, kb);
    root = (ValueExpr) ValueReplacer.set(root, (ValueExpr) lvalue, rhs);
    param.set(var, root);
  }

  @Override
  protected Expression visitReturnExpr(ExpressionReturn obj, Memory param) {
    Expression rhs = exeval(obj.expression, param);
    return rhs;
  }

  @Override
  protected Expression visitReturnVoid(VoidReturn obj, Memory param) {
    return new AnyValue();
  }

  @Override
  protected Expression visitWhileStmt(WhileStmt obj, Memory param) {
    while (toBool(exeval(obj.condition, param))) {
      Expression ret = visit(obj.body, param);
      if (ret != null) {
        return ret;
      }
    }
    return null;
  }

  @Override
  protected Expression visitIfStmt(IfStatement obj, Memory param) {
    for (IfOption opt : obj.option) {
      if (toBool(exeval(opt.condition, param))) {
        return visit(opt.code, param);
      }
    }
    return visit(obj.defblock, param);
  }
}
