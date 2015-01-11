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
import error.ErrorType;
import error.RError;
import evl.expression.Expression;
import evl.expression.binop.And;
import evl.expression.binop.BitAnd;
import evl.expression.binop.BitOr;
import evl.expression.binop.LogicAnd;
import evl.expression.binop.LogicOr;
import evl.expression.binop.Or;
import evl.expression.unop.BitNot;
import evl.expression.unop.LogicNot;
import evl.expression.unop.Not;
import evl.knowledge.KnowType;
import evl.knowledge.KnowledgeBase;
import evl.other.Namespace;
import evl.traverser.ExprReplacer;
import evl.type.Type;
import evl.type.base.BooleanType;
import evl.type.base.RangeType;

/**
 * Replaces "and" with "bitand" or "logicand", replaces "or" with "bitor" or "logicor", replaces "not" with "bitnot" or
 * "logicnot"
 *
 * @author urs
 *
 */
public class BitLogicCategorizer extends EvlPass {
  private final static BitLogicCategorizerWorker INSTANCE = new BitLogicCategorizerWorker();

  public BitLogicCategorizer() {
    super();
    addDependency(InitVarTyper.class);
  }

  @Override
  public void process(Namespace evl, KnowledgeBase kb) {
    KnowType kt = kb.getEntry(KnowType.class);
    INSTANCE.traverse(evl, kt);
  }

}

class BitLogicCategorizerWorker extends ExprReplacer<KnowType> {

  @Override
  protected Expression visitAnd(And obj, KnowType param) {
    super.visitAnd(obj, param);
    Type lt = param.get(obj.getLeft());
    Type rt = param.get(obj.getRight());
    assert ((lt instanceof BooleanType) == (rt instanceof BooleanType));
    if (lt instanceof BooleanType) {
      return new LogicAnd(obj.getInfo(), obj.getLeft(), obj.getRight());
    } else {
      return new BitAnd(obj.getInfo(), obj.getLeft(), obj.getRight());
    }
  }

  @Override
  protected Expression visitOr(Or obj, KnowType param) {
    super.visitOr(obj, param);
    Type lt = param.get(obj.getLeft());
    Type rt = param.get(obj.getRight());
    assert ((lt instanceof BooleanType) == (rt instanceof BooleanType));
    if (lt instanceof BooleanType) {
      return new LogicOr(obj.getInfo(), obj.getLeft(), obj.getRight());
    } else {
      return new BitOr(obj.getInfo(), obj.getLeft(), obj.getRight());
    }
  }

  @Override
  protected Expression visitNot(Not obj, KnowType param) {
    super.visitNot(obj, param);
    Type type = param.get(obj.getExpr());
    if (type instanceof BooleanType) {
      return new LogicNot(obj.getInfo(), obj.getExpr());
    } else if (type instanceof RangeType) {
      Range range = ((RangeType) type).getNumbers();
      int bits = range.getHigh().bitCount();
      BigInteger exp = BigInteger.valueOf(2).pow(bits).subtract(BigInteger.ONE);
      if (!range.getLow().equals(BigInteger.ZERO) || !exp.equals(range.getHigh())) {
        RError.err(ErrorType.Error, obj.getInfo(), "not only allowed for R{0,2^n-1}");
        return null;
      }
      return new BitNot(obj.getInfo(), obj.getExpr());
    } else {
      RError.err(ErrorType.Error, obj.getInfo(), "not only implemented for boolean and range types");
      return null;
    }
  }

}
