package evl.pass;

import pass.EvlPass;
import evl.expression.BoolValue;
import evl.expression.Expression;
import evl.expression.binop.LogicAnd;
import evl.expression.binop.LogicOr;
import evl.expression.unop.LogicNot;
import evl.knowledge.KnowledgeBase;
import evl.other.Namespace;
import evl.traverser.ExprReplacer;

public class TautoExprDel extends EvlPass {

  @Override
  public void process(Namespace evl, KnowledgeBase kb) {
    TautoExprDelWorker worker = new TautoExprDelWorker();
    worker.traverse(evl, null);
  }

}

class TautoExprDelWorker extends ExprReplacer<Void> {

  private boolean isTrue(Expression expr) {
    return (expr instanceof BoolValue) && ((BoolValue) expr).isValue();
  }

  private boolean isFalse(Expression expr) {
    return (expr instanceof BoolValue) && !((BoolValue) expr).isValue();
  }

  @Override
  protected Expression visitLogicOr(LogicOr obj, Void param) {
    obj = (LogicOr) super.visitLogicOr(obj, param);

    if (isFalse(obj.getLeft())) {
      return obj.getRight();
    }
    if (isFalse(obj.getRight())) {
      return obj.getLeft();
    }
    if (isTrue(obj.getLeft()) || isTrue(obj.getRight())) {
      // FIXME keep side effects
      return new BoolValue(obj.getInfo(), true);
    }

    return obj;
  }

  @Override
  protected Expression visitLogicAnd(LogicAnd obj, Void param) {
    obj = (LogicAnd) super.visitLogicAnd(obj, param);

    if (isTrue(obj.getLeft())) {
      return obj.getRight();
    }
    if (isTrue(obj.getRight())) {
      return obj.getLeft();
    }
    if (isFalse(obj.getLeft()) || isFalse(obj.getRight())) {
      // FIXME keep side effects
      return new BoolValue(obj.getInfo(), false);
    }

    return obj;
  }

  @Override
  protected Expression visitLogicNot(LogicNot obj, Void param) {
    obj = (LogicNot) super.visitLogicNot(obj, param);

    if (isTrue(obj.getExpr())) {
      return new BoolValue(obj.getInfo(), false);
    }
    if (isFalse(obj.getExpr())) {
      return new BoolValue(obj.getInfo(), true);
    }

    return obj;
  }

}
