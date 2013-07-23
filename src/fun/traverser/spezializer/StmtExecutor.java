package fun.traverser.spezializer;

import java.util.List;

import common.ElementInfo;

import fun.Fun;
import fun.NullTraverser;
import fun.expression.AnyValue;
import fun.expression.BoolValue;
import fun.expression.Expression;
import fun.expression.reference.ReferenceLinked;
import fun.function.impl.FuncGlobal;
import fun.knowledge.KnowledgeBase;
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

  public static Expression process(FuncGlobal func, List<Expression> actparam, Memory mem, KnowledgeBase kb) {
    Memory memory = new Memory(mem);

    assert (func.getParam().size() == actparam.size());

    for (int i = 0; i < actparam.size(); i++) {
      Variable var = func.getParam().getList().get(i);
      Expression val = actparam.get(i);
      memory.createVar(var);
      memory.setInt(var, val);
    }

    StmtExecutor executor = new StmtExecutor(kb);
    Expression ret = executor.traverse(func, memory);
    assert (ret != null);
    return ret;
  }

  private Expression exeval(Expression expr, Memory mem) {
    return ExprEvaluator.evaluate(expr, mem, kb);
  }

  private boolean toBool(Expression expr) {
    assert (expr instanceof BoolValue);
    return ((BoolValue) expr).isValue();
  }

  private void setVariable(ReferenceLinked left, Expression val, Memory param) {
    assert (left.getOffset().isEmpty());
    assert (left.getLink() instanceof Variable);
    param.setInt((Variable) left.getLink(), val);
  }

  @Override
  protected Expression visitDefault(Fun obj, Memory param) {
    throw new RuntimeException("not yet implemented: " + obj.getClass().getCanonicalName());
  }

  @Override
  protected Expression visitFuncGlobal(FuncGlobal obj, Memory param) {
    for (Statement stmt : obj.getBody().getStatements()) {
      Expression ret = visit(stmt, param);
      if (ret != null) {
        return ret;
      }
    }
    return new AnyValue(new ElementInfo());
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
    param.createVar(obj.getVariable());
    return null;
  }

  @Override
  protected Expression visitAssignment(Assignment obj, Memory param) {
    Expression rhs = exeval(obj.getRight(), param);
    setVariable((ReferenceLinked) obj.getLeft(), rhs, param);
    return null;
  }

  @Override
  protected Expression visitReturnExpr(ReturnExpr obj, Memory param) {
    Expression rhs = exeval(obj.getExpr(), param);
    return rhs;
  }

  @Override
  protected Expression visitReturnVoid(ReturnVoid obj, Memory param) {
    return new AnyValue(new ElementInfo());
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
