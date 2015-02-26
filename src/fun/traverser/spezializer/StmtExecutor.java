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
import fun.Copy;
import fun.Fun;
import fun.NullTraverser;
import fun.expression.AnyValue;
import fun.expression.BoolValue;
import fun.expression.Expression;
import fun.expression.TupleValue;
import fun.expression.reference.Reference;
import fun.function.FuncFunction;
import fun.knowledge.KnowEmptyValue;
import fun.knowledge.KnowledgeBase;
import fun.other.FunList;
import fun.statement.Assignment;
import fun.statement.Block;
import fun.statement.IfOption;
import fun.statement.IfStmt;
import fun.statement.ReturnExpr;
import fun.statement.ReturnVoid;
import fun.statement.Statement;
import fun.statement.VarDefStmt;
import fun.statement.While;
import fun.traverser.Memory;
import fun.type.Type;
import fun.variable.FuncVariable;
import fun.variable.Variable;

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

  public static Expression process(FuncFunction func, List<Expression> actparam, Memory mem, KnowledgeBase kb) {
    Memory memory = new Memory(mem);

    assert (func.getParam().size() == actparam.size());

    for (int i = 0; i < actparam.size(); i++) {
      FuncVariable var = func.getParam().get(i);
      Expression val = (Expression) ExprEvaluator.evaluate(actparam.get(i), mem, kb);
      memory.createVar(var);
      memory.set(var, val);
    }

    StmtExecutor executor = new StmtExecutor(kb);
    Expression ret = executor.traverse(func, memory);
    assert (ret != null);
    return ret;
  }

  private Expression exeval(Expression expr, Memory mem) {
    return (Expression) ExprEvaluator.evaluate(expr, mem, kb);
  }

  private boolean toBool(Expression expr) {
    assert (expr instanceof BoolValue);
    return ((BoolValue) expr).isValue();
  }

  @Override
  protected Expression visitDefault(Fun obj, Memory param) {
    throw new RuntimeException("not yet implemented: " + obj.getClass().getCanonicalName());
  }

  @Override
  protected Expression visitFuncFunction(FuncFunction obj, Memory param) {
    for (Statement stmt : obj.getBody().getStatements()) {
      Expression ret = visit(stmt, param);
      if (ret != null) {
        return ret;
      }
    }
    return new AnyValue(ElementInfo.NO);
  }

  @Override
  protected Expression visitBlock(Block obj, Memory param) {
    for (Statement stmt : obj.getStatements()) {
      Expression ret = visit(stmt, param);
      if (ret != null) {
        return ret;
      }
    }
    return null;
  }

  @Override
  protected Expression visitVarDefStmt(VarDefStmt obj, Memory param) {
    Expression value = exeval(obj.getInitial(), param);

    for (Variable var : obj.getVariable()) {
      param.createVar(var);
      if (value instanceof AnyValue) {
        Type type = (Type) var.getType().getLink();
        Expression empty = kb.getEntry(KnowEmptyValue.class).get(type);
        param.set(var, Copy.copy(empty));
      } else {
        param.set(var, Copy.copy(value));
      }
    }

    return null;
  }

  @Override
  protected Expression visitAssignment(Assignment obj, Memory param) {
    Expression rhs = exeval(obj.getRight(), param);

    FunList<Expression> value;

    if (obj.getLeft().size() > 1) {
      RError.ass(rhs instanceof TupleValue, obj.getInfo(), "expected tuple on the right");
      value = ((TupleValue) rhs).getValue();
    } else {
      value = new FunList<Expression>();
      value.add(rhs);
    }

    // FIXME what if a function call is on the rhs?
    RError.ass(obj.getLeft().size() == value.size(), obj.getInfo(), "expect same number of elemnts on both sides, got " + obj.getLeft().size() + " <-> " + value.size());

    for (int i = 0; i < value.size(); i++) {
      assign(obj.getLeft().get(i), value.get(i), param);
    }

    return null;
  }

  private void assign(Reference lhs, Expression rhs, Memory param) {
    rhs = Copy.copy(rhs);

    Variable var = (Variable) lhs.getLink();
    Expression root = param.get(var);

    Fun lvalue = RefEvaluator.execute(root, lhs.getOffset(), param, kb);
    root = ValueReplacer.set(root, (Expression) lvalue, rhs);
    param.set(var, root);
  }

  @Override
  protected Expression visitReturnExpr(ReturnExpr obj, Memory param) {
    Expression rhs = exeval(obj.getExpr(), param);
    return rhs;
  }

  @Override
  protected Expression visitReturnVoid(ReturnVoid obj, Memory param) {
    return new AnyValue(ElementInfo.NO);
  }

  @Override
  protected Expression visitWhile(While obj, Memory param) {
    while (toBool(exeval(obj.getCondition(), param))) {
      Expression ret = visit(obj.getBody(), param);
      if (ret != null) {
        return ret;
      }
    }
    return null;
  }

  @Override
  protected Expression visitIfStmt(IfStmt obj, Memory param) {
    for (IfOption opt : obj.getOption()) {
      if (toBool(exeval(opt.getCondition(), param))) {
        return visit(opt.getCode(), param);
      }
    }
    return visit(obj.getDefblock(), param);
  }
}
