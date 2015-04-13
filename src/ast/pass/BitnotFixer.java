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

package ast.pass;

import java.math.BigInteger;

import pass.AstPass;
import util.Range;
import ast.data.Namespace;
import ast.data.expression.Expression;
import ast.data.expression.Number;
import ast.data.expression.binop.BitAnd;
import ast.data.expression.unop.BitNot;
import ast.data.type.Type;
import ast.data.type.base.RangeType;
import ast.knowledge.KnowType;
import ast.knowledge.KnowledgeBase;
import ast.traverser.other.ExprReplacer;

import common.ElementInfo;

/**
 * Cuts back the result of bitnot operations
 *
 * @author urs
 *
 */
public class BitnotFixer extends AstPass {

  @Override
  public void process(Namespace ast, KnowledgeBase kb) {
    BitnotFixerWorker changer = new BitnotFixerWorker(kb);
    changer.traverse(ast, null);
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
    assert (range.low.equals(BigInteger.ZERO));
    int bits = range.high.bitCount();
    BigInteger mask = BigInteger.valueOf(2).pow(bits).subtract(BigInteger.ONE);
    assert (mask.equals(range.high));
    return new BitAnd(info, obj, new Number(info, mask));
  }

}
