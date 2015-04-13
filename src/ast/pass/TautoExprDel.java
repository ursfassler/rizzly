package ast.pass;

import pass.AstPass;
import ast.data.Namespace;
import ast.data.expression.BoolValue;
import ast.data.expression.Expression;
import ast.data.expression.binop.LogicAnd;
import ast.data.expression.binop.LogicOr;
import ast.data.expression.unop.LogicNot;
import ast.knowledge.KnowledgeBase;
import ast.traverser.other.ExprReplacer;

public class TautoExprDel extends AstPass {

  @Override
  public void process(Namespace ast, KnowledgeBase kb) {
    TautoExprDelWorker worker = new TautoExprDelWorker();
    worker.traverse(ast, null);
  }

}

class TautoExprDelWorker extends ExprReplacer<Void> {

  private boolean isTrue(Expression expr) {
    return (expr instanceof BoolValue) && ((BoolValue) expr).value;
  }

  private boolean isFalse(Expression expr) {
    return (expr instanceof BoolValue) && !((BoolValue) expr).value;
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
      return new BoolValue(obj.getInfo(), true);
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
      return new BoolValue(obj.getInfo(), false);
    }

    return obj;
  }

  @Override
  protected Expression visitLogicNot(LogicNot obj, Void param) {
    obj = (LogicNot) super.visitLogicNot(obj, param);

    if (isTrue(obj.expr)) {
      return new BoolValue(obj.getInfo(), false);
    }
    if (isFalse(obj.expr)) {
      return new BoolValue(obj.getInfo(), true);
    }

    return obj;
  }

}
