/**
 *  This file is part of Rizzly.
 *
 *  Rizzly is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Rizzly is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with Rizzly.  If not, see <http://www.gnu.org/licenses/>.
 */

package evl.pass;

import java.math.BigInteger;

import pass.EvlPass;
import util.Range;

import common.ElementInfo;

import evl.expression.Expression;
import evl.expression.Number;
import evl.expression.binop.BitAnd;
import evl.expression.unop.BitNot;
import evl.knowledge.KnowType;
import evl.knowledge.KnowledgeBase;
import evl.other.Namespace;
import evl.traverser.ExprReplacer;
import evl.type.Type;
import evl.type.base.RangeType;

/**
 * Cuts back the result of bitnot operations
 *
 * @author urs
 *
 */
public class BitnotFixer extends EvlPass {

  @Override
  public void process(Namespace evl, KnowledgeBase kb) {
    BitnotFixerWorker changer = new BitnotFixerWorker(kb);
    changer.traverse(evl, null);
  }

}

class BitnotFixerWorker extends ExprReplacer<Void> {
  private final static ElementInfo info = ElementInfo.NO;
  private final KnowType kt;

  public BitnotFixerWorker(KnowledgeBase kb) {
    super();
    this.kt = kb.getEntry(KnowType.class);
  }

  @Override
  protected Expression visitBitNot(BitNot obj, Void param) {
    obj = (BitNot) super.visitBitNot(obj, param);
    Type type = kt.get(obj);
    Range range = ((RangeType) type).range;
    assert (range.getLow().equals(BigInteger.ZERO));
    int bits = range.getHigh().bitCount();
    BigInteger mask = BigInteger.valueOf(2).pow(bits).subtract(BigInteger.ONE);
    assert (mask.equals(range.getHigh()));
    return new BitAnd(info, obj, new Number(info, mask));
  }

}
