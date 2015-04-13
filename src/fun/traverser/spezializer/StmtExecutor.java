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

package fun.traverser.spezializer;

import java.util.List;

import common.ElementInfo;

import error.RError;
import evl.copy.Copy;
import evl.data.Evl;
import evl.data.EvlList;
import evl.data.expression.AnyValue;
import evl.data.expression.BoolValue;
import evl.data.expression.Expression;
import evl.data.expression.TupleValue;
import evl.data.expression.reference.Reference;
import evl.data.function.header.FuncFunction;
import evl.data.statement.AssignmentMulti;
import evl.data.statement.Block;
import evl.data.statement.IfOption;
import evl.data.statement.Statement;
import evl.data.statement.VarDefInitStmt;
import evl.data.type.Type;
import evl.data.variable.Variable;
import evl.knowledge.KnowEmptyValue;
import evl.knowledge.KnowledgeBase;
import evl.traverser.NullTraverser;
import fun.traverser.Memory;

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

  public static evl.data.expression.Expression process(FuncFunction func, List<Expression> actparam, Memory mem, KnowledgeBase kb) {
    Memory memory = new Memory(mem);

    assert (func.param.size() == actparam.size());

    for (int i = 0; i < actparam.size(); i++) {
      evl.data.variable.FuncVariable var = func.param.get(i);
      Expression val = (Expression) ExprEvaluator.evaluate(actparam.get(i), mem, kb);
      memory.createVar(var);
      memory.set(var, val);
    }

    StmtExecutor executor = new StmtExecutor(kb);
    evl.data.expression.Expression ret = executor.traverse(func, memory);
    assert (ret != null);
    return ret;
  }

  private Expression exeval(Expression expr, Memory mem) {
    return (Expression) ExprEvaluator.evaluate(expr, mem, kb);
  }

  private boolean toBool(evl.data.expression.Expression expr) {
    assert (expr instanceof BoolValue);
    return ((evl.data.expression.BoolValue) expr).value;
  }

  @Override
  protected evl.data.expression.Expression visitDefault(Evl obj, Memory param) {
    throw new RuntimeException("not yet implemented: " + obj.getClass().getCanonicalName());
  }

  @Override
  protected evl.data.expression.Expression visitFuncFunction(FuncFunction obj, Memory param) {
    for (Statement stmt : obj.body.statements) {
      evl.data.expression.Expression ret = visit(stmt, param);
      if (ret != null) {
        return ret;
      }
    }
    return new AnyValue(ElementInfo.NO);
  }

  @Override
  protected evl.data.expression.Expression visitBlock(Block obj, Memory param) {
    for (Statement stmt : obj.statements) {
      evl.data.expression.Expression ret = visit(stmt, param);
      if (ret != null) {
        return ret;
      }
    }
    return null;
  }

  @Override
  protected evl.data.expression.Expression visitVarDefInitStmt(VarDefInitStmt obj, Memory param) {
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
  protected evl.data.expression.Expression visitAssignmentMulti(AssignmentMulti obj, Memory param) {
    Expression rhs = exeval(obj.right, param);

    EvlList<Expression> value;

    if (obj.left.size() > 1) {
      RError.ass(rhs instanceof TupleValue, obj.getInfo(), "expected tuple on the right");
      value = ((evl.data.expression.TupleValue) rhs).value;
    } else {
      value = new EvlList<Expression>();
      value.add(rhs);
    }

    // FIXME what if a function call is on the rhs?
    RError.ass(obj.left.size() == value.size(), obj.getInfo(), "expect same number of elemnts on both sides, got " + obj.left.size() + " <-> " + value.size());

    for (int i = 0; i < value.size(); i++) {
      assign(obj.left.get(i), value.get(i), param);
    }

    return null;
  }

  private void assign(evl.data.expression.reference.Reference lhs, Expression rhs, Memory param) {
    rhs = Copy.copy(rhs);

    Variable var = (Variable) lhs.link;
    Expression root = param.get(var);

    Evl lvalue = RefEvaluator.execute(root, lhs.offset, param, kb);
    root = ValueReplacer.set(root, (Expression) lvalue, rhs);
    param.set(var, root);
  }

  @Override
  protected evl.data.expression.Expression visitReturnExpr(evl.data.statement.ReturnExpr obj, Memory param) {
    evl.data.expression.Expression rhs = exeval(obj.expr, param);
    return rhs;
  }

  @Override
  protected evl.data.expression.Expression visitReturnVoid(evl.data.statement.ReturnVoid obj, Memory param) {
    return new AnyValue(ElementInfo.NO);
  }

  @Override
  protected evl.data.expression.Expression visitWhileStmt(evl.data.statement.WhileStmt obj, Memory param) {
    while (toBool(exeval(obj.condition, param))) {
      evl.data.expression.Expression ret = visit(obj.body, param);
      if (ret != null) {
        return ret;
      }
    }
    return null;
  }

  @Override
  protected evl.data.expression.Expression visitIfStmt(evl.data.statement.IfStmt obj, Memory param) {
    for (IfOption opt : obj.option) {
      if (toBool(exeval(opt.condition, param))) {
        return visit(opt.code, param);
      }
    }
    return visit(obj.defblock, param);
  }
}
