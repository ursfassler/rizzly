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

package ast.pass.reduction;

import java.math.BigInteger;

import ast.data.Namespace;
import ast.data.Range;
import ast.data.expression.Expression;
import ast.data.expression.binop.And;
import ast.data.expression.binop.BitAnd;
import ast.data.expression.binop.BitOr;
import ast.data.expression.binop.BitXor;
import ast.data.expression.binop.LogicAnd;
import ast.data.expression.binop.LogicOr;
import ast.data.expression.binop.NotEqual;
import ast.data.expression.binop.Or;
import ast.data.expression.unop.BitNot;
import ast.data.expression.unop.LogicNot;
import ast.data.expression.unop.Not;
import ast.data.type.Type;
import ast.data.type.base.BooleanType;
import ast.data.type.base.RangeType;
import ast.dispatcher.other.ExprReplacer;
import ast.knowledge.KnowType;
import ast.knowledge.KnowledgeBase;
import ast.pass.AstPass;
import error.ErrorType;
import error.RError;

/**
 * Replaces "and" with "bitand" or "logicand", replaces "or" with "bitor" or "logicor", replaces "not" with "bitnot" or
 * "logicnot"
 *
 * @author urs
 *
 */
public class BitLogicCategorizer implements AstPass {

  @Override
  public void process(Namespace ast, KnowledgeBase kb) {
    KnowType kt = kb.getEntry(KnowType.class);
    new BitLogicCategorizerWorker().traverse(ast, kt);
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
      NotEqual ret = new NotEqual(obj.left, obj.right);
      ret.metadata().add(obj.metadata());
      return ret;
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
    Expression ret;
    if (lt instanceof BooleanType) {
      ret = new LogicAnd(obj.left, obj.right);
    } else {
      ret = new BitAnd(obj.left, obj.right);
    }
    ret.metadata().add(obj.metadata());
    return ret;
  }

  @Override
  protected Expression visitOr(Or obj, KnowType param) {
    super.visitOr(obj, param);
    Type lt = param.get(obj.left);
    Type rt = param.get(obj.right);
    assert ((lt instanceof BooleanType) == (rt instanceof BooleanType));
    Expression ret;
    if (lt instanceof BooleanType) {
      ret = new LogicOr(obj.left, obj.right);
    } else {
      ret = new BitOr(obj.left, obj.right);
    }
    ret.metadata().add(obj.metadata());
    return ret;
  }

  @Override
  protected Expression visitNot(Not obj, KnowType param) {
    super.visitNot(obj, param);
    Type type = param.get(obj.expression);
    if (type instanceof BooleanType) {
      LogicNot ret = new LogicNot(obj.expression);
      ret.metadata().add(obj.metadata());
      return ret;
    } else if (type instanceof RangeType) {
      Range range = ((RangeType) type).range;
      int bits = range.high.bitCount();
      BigInteger exp = BigInteger.valueOf(2).pow(bits).subtract(BigInteger.ONE);
      if (!range.low.equals(BigInteger.ZERO) || !exp.equals(range.high)) {
        RError.err(ErrorType.Error, "not only allowed for R{0,2^n-1}", obj.metadata());
        return null;
      }
      BitNot ret = new BitNot(obj.expression);
      ret.metadata().add(obj.metadata());
      return ret;
    } else {
      RError.err(ErrorType.Error, "not only implemented for boolean and range types", obj.metadata());
      return null;
    }
  }

}
