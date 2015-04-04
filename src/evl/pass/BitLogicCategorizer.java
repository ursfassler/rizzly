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
import pass.NoItem;
import util.Range;
import error.ErrorType;
import error.RError;
import evl.data.Namespace;
import evl.data.expression.Expression;
import evl.data.expression.binop.And;
import evl.data.expression.binop.BitAnd;
import evl.data.expression.binop.BitOr;
import evl.data.expression.binop.BitXor;
import evl.data.expression.binop.LogicAnd;
import evl.data.expression.binop.LogicOr;
import evl.data.expression.binop.Notequal;
import evl.data.expression.binop.Or;
import evl.data.expression.unop.BitNot;
import evl.data.expression.unop.LogicNot;
import evl.data.expression.unop.Not;
import evl.data.type.Type;
import evl.data.type.base.BooleanType;
import evl.data.type.base.RangeType;
import evl.knowledge.KnowType;
import evl.knowledge.KnowledgeBase;
import evl.traverser.other.ExprReplacer;

/**
 * Replaces "and" with "bitand" or "logicand", replaces "or" with "bitor" or "logicor", replaces "not" with "bitnot" or
 * "logicnot"
 *
 * @author urs
 *
 */
public class BitLogicCategorizer extends EvlPass {
  public BitLogicCategorizer() {
    postcondition.add(new NoItem(And.class));
    postcondition.add(new NoItem(Or.class));
    postcondition.add(new NoItem(Not.class));
  }

  @Override
  public void process(Namespace evl, KnowledgeBase kb) {
    KnowType kt = kb.getEntry(KnowType.class);
    new BitLogicCategorizerWorker().traverse(evl, kt);
  }

}

class BitLogicCategorizerWorker extends ExprReplacer<KnowType> {

  @Override
  protected Expression visitBitXor(BitXor obj, KnowType param) {
    super.visitBitXor(obj, param);
    Type lt = param.get(obj.left);
    Type rt = param.get(obj.right);
    assert ((lt instanceof BooleanType) == (rt instanceof BooleanType));
    if (lt instanceof BooleanType) {
      return new Notequal(obj.getInfo(), obj.left, obj.right);
    } else {
      return obj;
    }
  }

  @Override
  protected Expression visitAnd(And obj, KnowType param) {
    super.visitAnd(obj, param);
    Type lt = param.get(obj.left);
    Type rt = param.get(obj.right);
    assert ((lt instanceof BooleanType) == (rt instanceof BooleanType));
    if (lt instanceof BooleanType) {
      return new LogicAnd(obj.getInfo(), obj.left, obj.right);
    } else {
      return new BitAnd(obj.getInfo(), obj.left, obj.right);
    }
  }

  @Override
  protected Expression visitOr(Or obj, KnowType param) {
    super.visitOr(obj, param);
    Type lt = param.get(obj.left);
    Type rt = param.get(obj.right);
    assert ((lt instanceof BooleanType) == (rt instanceof BooleanType));
    if (lt instanceof BooleanType) {
      return new LogicOr(obj.getInfo(), obj.left, obj.right);
    } else {
      return new BitOr(obj.getInfo(), obj.left, obj.right);
    }
  }

  @Override
  protected Expression visitNot(Not obj, KnowType param) {
    super.visitNot(obj, param);
    Type type = param.get(obj.expr);
    if (type instanceof BooleanType) {
      return new LogicNot(obj.getInfo(), obj.expr);
    } else if (type instanceof RangeType) {
      Range range = ((RangeType) type).range;
      int bits = range.getHigh().bitCount();
      BigInteger exp = BigInteger.valueOf(2).pow(bits).subtract(BigInteger.ONE);
      if (!range.getLow().equals(BigInteger.ZERO) || !exp.equals(range.getHigh())) {
        RError.err(ErrorType.Error, obj.getInfo(), "not only allowed for R{0,2^n-1}");
        return null;
      }
      return new BitNot(obj.getInfo(), obj.expr);
    } else {
      RError.err(ErrorType.Error, obj.getInfo(), "not only implemented for boolean and range types");
      return null;
    }
  }

}
