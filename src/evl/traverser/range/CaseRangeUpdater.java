package evl.traverser.range;

import java.math.BigInteger;
import java.util.List;
import java.util.Map;

import evl.Evl;
import evl.NullTraverser;
import evl.cfg.CaseOptEntry;
import evl.cfg.CaseOptRange;
import evl.cfg.CaseOptValue;
import evl.expression.Expression;
import evl.expression.Number;
import evl.knowledge.KnowBaseItem;
import evl.knowledge.KnowledgeBase;
import evl.type.base.Range;
import evl.variable.Variable;

public class CaseRangeUpdater extends NullTraverser<Void, Map<Variable, Range>> {
  private BigInteger low = null;
  private BigInteger high = null;

  public static Range process(List<CaseOptEntry> values,  KnowledgeBase kb) {
    assert( !values.isEmpty() );
    CaseRangeUpdater updater = new CaseRangeUpdater();
    updater.visitItr(values, null);
    KnowBaseItem kbi = kb.getEntry(KnowBaseItem.class);
    return kbi.getRangeType(updater.low, updater.high);
  }

  private BigInteger evalToInt(Expression value) {
    assert (value instanceof Number);
    return BigInteger.valueOf(((Number) value).getValue());
  }

  private void setHigh(BigInteger num) {
    if( (high == null) || (high.compareTo(num) < 0) ){  //TODO correct?
      high = num;
    }
  }

  private void setLow(BigInteger num) {
    if( (low == null) || (low.compareTo(num) > 0) ){  //TODO correct?
      low = num;
    }
  }

  @Override
  protected Void visitDefault(Evl obj, Map<Variable, Range> param) {
    throw new RuntimeException("not yet implemented: " + obj.getClass().getCanonicalName());
  }

  @Override
  protected Void visitCaseOptRange(CaseOptRange obj, Map<Variable, Range> param) {
    BigInteger low = evalToInt(obj.getStart());
    setLow(low);
    setHigh(low);
    BigInteger high = evalToInt(obj.getEnd());
    setLow(high);
    setHigh(high);
    return null;
  }

  @Override
  protected Void visitCaseOptValue(CaseOptValue obj, Map<Variable, Range> param) {
    BigInteger num = evalToInt(obj.getValue());
    setLow(num);
    setHigh(num);
    return null;
  }

}
