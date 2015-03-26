package evl.pass;

import pass.EvlPass;
import util.Range;
import evl.expression.BoolValue;
import evl.expression.Expression;
import evl.expression.binop.Equal;
import evl.expression.binop.Greater;
import evl.expression.binop.Greaterequal;
import evl.expression.binop.Less;
import evl.expression.binop.Lessequal;
import evl.expression.binop.Notequal;
import evl.knowledge.KnowType;
import evl.knowledge.KnowledgeBase;
import evl.other.Namespace;
import evl.traverser.ExprReplacer;
import evl.type.Type;
import evl.type.base.RangeType;

public class AlwaysGreater extends EvlPass {

  @Override
  public void process(Namespace evl, KnowledgeBase kb) {
    AlwaysGreaterWorker worker = new AlwaysGreaterWorker(kb);
    worker.traverse(evl, null);
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
      return ((RangeType) type).getNumbers();
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
    Range lr = getRange(obj.getLeft());
    Range rr = getRange(obj.getRight());

    if ((lr != null) && (rr != null)) {
      if (lr.getHigh().compareTo(rr.getLow()) <= 0) {
        // 0,1 <= 1,257
        return new BoolValue(obj.getInfo(), true);
      }
      if (rr.getHigh().compareTo(lr.getLow()) < 0) {
        // 10,20 <= 0,9
        return new BoolValue(obj.getInfo(), false);
      }
    }

    return obj;
  }

}
