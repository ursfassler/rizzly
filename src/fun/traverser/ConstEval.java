package fun.traverser;

import fun.DefTraverser;
import fun.expression.Expression;
import fun.knowledge.KnowledgeBase;
import fun.other.Namespace;
import fun.traverser.spezializer.ExprEvaluator;
import fun.variable.Constant;

public class ConstEval extends DefTraverser<Void, Void> {
  private final KnowledgeBase kb;

  public ConstEval(KnowledgeBase kb) {
    super();
    this.kb = kb;
  }

  public static void process(Namespace classes, KnowledgeBase kb) {
    ConstEval eval = new ConstEval(kb);
    eval.traverse(classes, null);
  }

  @Override
  protected Void visitConstant(Constant obj, Void param) {
    Expression value = ExprEvaluator.evaluate(obj.getDef(), new Memory(), kb);
    obj.setDef(value);
    return null;
  }

}
