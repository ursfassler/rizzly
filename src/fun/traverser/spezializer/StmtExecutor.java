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

import java.util.LinkedList;
import java.util.List;

import common.ElementInfo;

import error.ErrorType;
import error.RError;
import fun.Fun;
import fun.NullTraverser;
import fun.expression.AnyValue;
import fun.expression.ArrayValue;
import fun.expression.BoolValue;
import fun.expression.Expression;
import fun.expression.reference.RefIndex;
import fun.expression.reference.RefItem;
import fun.expression.reference.Reference;
import fun.function.FuncFunction;
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
import fun.type.base.NaturalType;
import fun.type.template.Array;
import fun.type.template.Range;
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
  protected Expression visitVarDef(VarDefStmt obj, Memory param) {
    Expression value;

    Reference tr = obj.getVariable().getType();
    assert (tr.getOffset().isEmpty());
    Type type = (Type) tr.getLink();
    if (type instanceof Range) {
      value = null;
    } else if (type instanceof NaturalType) {
      value = null;
    } else if (type instanceof Array) {
      Array at = (Array) type;
      FunList<Expression> vals = new FunList<Expression>();
      for (int i = 0; i < at.getSize().intValue(); i++) {
        vals.add(null);
      }
      value = new ArrayValue(ElementInfo.NO, vals);
    } else {
      RError.err(ErrorType.Fatal, obj.getInfo(), "Unhandled type: " + type.getName());
      value = null;
    }

    param.createVar(obj.getVariable());
    if (value != null) {
      param.set(obj.getVariable(), value);
    }

    return null;
  }

  @Override
  protected Expression visitAssignment(Assignment obj, Memory param) {
    Variable var = (Variable) obj.getLeft().getLink();
    Expression rhs = exeval(obj.getRight(), param);

    if (obj.getLeft().getOffset().isEmpty()) {
      param.set(var, rhs);
      return null;
    }

    Expression value = param.get(var);

    LinkedList<RefItem> offset = new LinkedList<RefItem>();
    for (RefItem itm : obj.getLeft().getOffset()) {
      if (itm instanceof RefIndex) {
        itm = new RefIndex(ElementInfo.NO, exeval(((RefIndex) itm).getIndex(), param));
      }
      offset.add(itm);
    }
    RefItem last = offset.pollLast();
    Expression elem = ElementGetter.get(value, offset);

    ElementSetter.set(elem, last, rhs);

    return null;
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
