package evl.pass;

import pass.EvlPass;
import util.Range;
import evl.data.Namespace;
import evl.data.expression.BoolValue;
import evl.data.expression.Expression;
import evl.data.expression.binop.Equal;
import evl.data.expression.binop.Greater;
import evl.data.expression.binop.Greaterequal;
import evl.data.expression.binop.Less;
import evl.data.expression.binop.Lessequal;
import evl.data.expression.binop.Notequal;
import evl.data.type.Type;
import evl.data.type.base.RangeType;
import evl.knowledge.KnowType;
import evl.knowledge.KnowledgeBase;
import evl.traverser.other.ExprReplacer;

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
