package evl.traverser;

import java.math.BigInteger;

import util.Range;

import common.ElementInfo;

import evl.Evl;
import evl.expression.Expression;
import evl.expression.Number;
import evl.expression.binop.BitAnd;
import evl.expression.unop.BitNot;
import evl.knowledge.KnowType;
import evl.knowledge.KnowledgeBase;
import evl.type.Type;
import evl.type.base.RangeType;

/**
 * Cuts back the result of bitnot operations
 * 
 * @author urs
 * 
 */
public class BitnotFixer extends ExprReplacer<Void> {
  private final static ElementInfo info = new ElementInfo();
  private final KnowType kt;

  public BitnotFixer(KnowledgeBase kb) {
    super();
    this.kt = kb.getEntry(KnowType.class);
  }

  public static void process(Evl obj, KnowledgeBase kb) {
    BitnotFixer changer = new BitnotFixer(kb);
    changer.traverse(obj, null);
  }

  @Override
  protected Expression visitBitNot(BitNot obj, Void param) {
    obj = (BitNot) super.visitBitNot(obj, param);
    Type type = kt.get(obj);
    Range range = ((RangeType) type).getNumbers();
    assert (range.getLow().equals(BigInteger.ZERO));
    int bits = range.getHigh().bitCount();
    BigInteger mask = BigInteger.valueOf(2).pow(bits).subtract(BigInteger.ONE);
    assert (mask.equals(range.getHigh()));
    return new BitAnd(info, obj, new Number(info, mask));
  }

}
