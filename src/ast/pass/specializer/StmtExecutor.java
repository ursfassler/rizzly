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

import ast.ElementInfo;
import ast.copy.Copy;
import ast.data.Ast;
import ast.data.AstList;
import ast.data.expression.AnyValue;
import ast.data.expression.BoolValue;
import ast.data.expression.Expression;
import ast.data.expression.TupleValue;
import ast.data.expression.reference.Reference;
import ast.data.function.header.FuncFunction;
import ast.data.statement.AssignmentMulti;
import ast.data.statement.Block;
import ast.data.statement.IfOption;
import ast.data.statement.Statement;
import ast.data.statement.VarDefInitStmt;
import ast.data.type.Type;
import ast.data.variable.Variable;
import ast.interpreter.Memory;
import ast.knowledge.KnowEmptyValue;
import ast.knowledge.KnowledgeBase;
import ast.traverser.NullTraverser;
import error.RError;

/**
 * If a visit() returns null, this means normal execution. If !null is returned, this means the function called "return"
 * and execution is aborted.
 *
 * @author urs
 *
 */
public class StmtExecutor extends NullTraverser<Expression, Memory> {
  private KnowledgeBase kb;

  public StmtExecutor(KnowledgeBase kb) {
    super();
    this.kb = kb;
  }

  public static ast.data.expression.Expression process(FuncFunction func, List<Expression> actparam, Memory mem, KnowledgeBase kb) {
    Memory memory = new Memory(mem);

    assert (func.param.size() == actparam.size());

    for (int i = 0; i < actparam.size(); i++) {
      ast.data.variable.FuncVariable var = func.param.get(i);
      Expression val = (Expression) ExprEvaluator.evaluate(actparam.get(i), mem, kb);
      memory.createVar(var);
      memory.set(var, val);
    }

    StmtExecutor executor = new StmtExecutor(kb);
    ast.data.expression.Expression ret = executor.traverse(func, memory);
    assert (ret != null);
    return ret;
  }

  private Expression exeval(Expression expr, Memory mem) {
    return (Expression) ExprEvaluator.evaluate(expr, mem, kb);
  }

  private boolean toBool(ast.data.expression.Expression expr) {
    assert (expr instanceof BoolValue);
    return ((ast.data.expression.BoolValue) expr).value;
  }

  @Override
  protected ast.data.expression.Expression visitDefault(Ast obj, Memory param) {
    throw new RuntimeException("not yet implemented: " + obj.getClass().getCanonicalName());
  }

  @Override
  protected ast.data.expression.Expression visitFuncFunction(FuncFunction obj, Memory param) {
    for (Statement stmt : obj.body.statements) {
      ast.data.expression.Expression ret = visit(stmt, param);
      if (ret != null) {
        return ret;
      }
    }
    return new AnyValue(ElementInfo.NO);
  }

  @Override
  protected ast.data.expression.Expression visitBlock(Block obj, Memory param) {
    for (Statement stmt : obj.statements) {
      ast.data.expression.Expression ret = visit(stmt, param);
      if (ret != null) {
        return ret;
      }
    }
    return null;
  }

  @Override
  protected ast.data.expression.Expression visitVarDefInitStmt(VarDefInitStmt obj, Memory param) {
    Expression value = exeval(obj.initial, param);

    for (Variable var : obj.variable) {
      param.createVar(var);
      if (value instanceof AnyValue) {
        Type type = (Type) ((Reference) var.type).link;
        Expression empty = kb.getEntry(KnowEmptyValue.class).get(type);
        param.set(var, Copy.copy(empty));
      } else {
        param.set(var, Copy.copy(value));
      }
    }

    return null;
  }

  @Override
  protected ast.data.expression.Expression visitAssignmentMulti(AssignmentMulti obj, Memory param) {
    Expression rhs = exeval(obj.right, param);

    AstList<Expression> value;

    if (obj.left.size() > 1) {
      RError.ass(rhs instanceof TupleValue, obj.getInfo(), "expected tuple on the right");
      value = ((ast.data.expression.TupleValue) rhs).value;
    } else {
      value = new AstList<Expression>();
      value.add(rhs);
    }

    // FIXME what if a function call is on the rhs?
    RError.ass(obj.left.size() == value.size(), obj.getInfo(), "expect same number of elemnts on both sides, got " + obj.left.size() + " <-> " + value.size());

    for (int i = 0; i < value.size(); i++) {
      assign(obj.left.get(i), value.get(i), param);
    }

    return null;
  }

  private void assign(ast.data.expression.reference.Reference lhs, Expression rhs, Memory param) {
    rhs = Copy.copy(rhs);

    Variable var = (Variable) lhs.link;
    Expression root = param.get(var);

    Ast lvalue = RefEvaluator.execute(root, lhs.offset, param, kb);
    root = ValueReplacer.set(root, (Expression) lvalue, rhs);
    param.set(var, root);
  }

  @Override
  protected ast.data.expression.Expression visitReturnExpr(ast.data.statement.ReturnExpr obj, Memory param) {
    ast.data.expression.Expression rhs = exeval(obj.expr, param);
    return rhs;
  }

  @Override
  protected ast.data.expression.Expression visitReturnVoid(ast.data.statement.ReturnVoid obj, Memory param) {
    return new AnyValue(ElementInfo.NO);
  }

  @Override
  protected ast.data.expression.Expression visitWhileStmt(ast.data.statement.WhileStmt obj, Memory param) {
    while (toBool(exeval(obj.condition, param))) {
      ast.data.expression.Expression ret = visit(obj.body, param);
      if (ret != null) {
        return ret;
      }
    }
    return null;
  }

  @Override
  protected ast.data.expression.Expression visitIfStmt(ast.data.statement.IfStmt obj, Memory param) {
    for (IfOption opt : obj.option) {
      if (toBool(exeval(opt.condition, param))) {
        return visit(opt.code, param);
      }
    }
    return visit(obj.defblock, param);
  }
}
