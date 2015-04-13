package ast.pass.optimize;

import ast.data.Namespace;
import ast.data.Range;
import ast.data.expression.BoolValue;
import ast.data.expression.Expression;
import ast.data.expression.binop.Equal;
import ast.data.expression.binop.Greater;
import ast.data.expression.binop.Greaterequal;
import ast.data.expression.binop.Less;
import ast.data.expression.binop.Lessequal;
import ast.data.expression.binop.Notequal;
import ast.data.type.Type;
import ast.data.type.base.RangeType;
import ast.knowledge.KnowType;
import ast.knowledge.KnowledgeBase;
import ast.pass.AstPass;
import ast.traverser.other.ExprReplacer;

public class AlwaysGreater extends AstPass {

  @Override
  public void process(Namespace ast, KnowledgeBase kb) {
    AlwaysGreaterWorker worker = new AlwaysGreaterWorker(kb);
    worker.traverse(ast, null);
  }

}

class AlwaysGreaterWorker extends ExprReplacer<Void> {
  private final KnowType kt;

  public AlwaysGreaterWorker(KnowledgeBase kb) {
    super();
    this.kt = kb.getEntry(KnowType.class);
  }

  private Range getRange(Expression expr) {
    Type type = kt.get(expr);
    if (type instanceof RangeType) {
      return ((RangeType) type).range;
    } else {
      return null;
    }
  }

  @Override
  protected Expression visitEqual(Equal obj, Void param) {
    // TODO implement
    return obj;
  }

  @Override
  protected Expression visitNotequal(Notequal obj, Void param) {
    // TODO implement
    return obj;
  }

  @Override
  protected Expression visitGreater(Greater obj, Void param) {
    // TODO implement
    return obj;
  }

  @Override
  protected Expression visitGreaterequal(Greaterequal obj, Void param) {
    // TODO implement
    return obj;
  }

  @Override
  protected Expression visitLess(Less obj, Void param) {
    // TODO implement
    return obj;
  }

  @Override
  protected Expression visitLessequal(Lessequal obj, Void param) {
    Range lr = getRange(obj.left);
    Range rr = getRange(obj.right);

    if ((lr != null) && (rr != null)) {
      if (lr.high.compareTo(rr.low) <= 0) {
        // 0,1 <= 1,257
        return new BoolValue(obj.getInfo(), true);
      }
      if (rr.high.compareTo(lr.low) < 0) {
        // 10,20 <= 0,9
        return new BoolValue(obj.getInfo(), false);
      }
    }

    return obj;
  }

}
