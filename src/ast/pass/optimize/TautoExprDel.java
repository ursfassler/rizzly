package ast.pass.optimize;

import ast.data.Namespace;
import ast.data.expression.Expression;
import ast.data.expression.binop.LogicAnd;
import ast.data.expression.binop.LogicOr;
import ast.data.expression.unop.LogicNot;
import ast.data.expression.value.BooleanValue;
import ast.dispatcher.other.ExprReplacer;
import ast.knowledge.KnowledgeBase;
import ast.pass.AstPass;

public class TautoExprDel implements AstPass {

  @Override
  public void process(Namespace ast, KnowledgeBase kb) {
    TautoExprDelWorker worker = new TautoExprDelWorker();
    worker.traverse(ast, null);
  }

}

class TautoExprDelWorker extends ExprReplacer<Void> {

  private boolean isTrue(Expression expr) {
    return (expr instanceof BooleanValue) && ((BooleanValue) expr).value;
  }

  private boolean isFalse(Expression expr) {
    return (expr instanceof BooleanValue) && !((BooleanValue) expr).value;
  }

  @Override
  protected Expression visitLogicOr(LogicOr obj, Void param) {
    obj = (LogicOr) super.visitLogicOr(obj, param);

    if (isFalse(obj.left)) {
      return obj.right;
    }
    if (isFalse(obj.right)) {
      return obj.left;
    }
    if (isTrue(obj.left) || isTrue(obj.right)) {
      // FIXME keep side effects
      BooleanValue ret = new BooleanValue(true);
      ret.metadata().add(obj.metadata());
      return ret;
    }

    return obj;
  }

  @Override
  protected Expression visitLogicAnd(LogicAnd obj, Void param) {
    obj = (LogicAnd) super.visitLogicAnd(obj, param);

    if (isTrue(obj.left)) {
      return obj.right;
    }
    if (isTrue(obj.right)) {
      return obj.left;
    }
    if (isFalse(obj.left) || isFalse(obj.right)) {
      // FIXME keep side effects
      BooleanValue ret = new BooleanValue(false);
      ret.metadata().add(obj.metadata());
      return ret;
    }

    return obj;
  }

  @Override
  protected Expression visitLogicNot(LogicNot obj, Void param) {
    obj = (LogicNot) super.visitLogicNot(obj, param);

    if (isTrue(obj.expression)) {
      BooleanValue ret = new BooleanValue(false);
      ret.metadata().add(obj.metadata());
      return ret;
    }
    if (isFalse(obj.expression)) {
      BooleanValue ret = new BooleanValue(true);
      ret.metadata().add(obj.metadata());
      return ret;
    }

    return obj;
  }

}
