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

package ast.pass.check.type;

import java.math.BigInteger;

import ast.ElementInfo;
import ast.data.Ast;
import ast.data.Range;
import ast.data.expression.Expression;
import ast.data.expression.TypeCast;
import ast.data.expression.binop.BitAnd;
import ast.data.expression.binop.BitOr;
import ast.data.expression.binop.Div;
import ast.data.expression.binop.Equal;
import ast.data.expression.binop.Greater;
import ast.data.expression.binop.Greaterequal;
import ast.data.expression.binop.Less;
import ast.data.expression.binop.Lessequal;
import ast.data.expression.binop.LogicAnd;
import ast.data.expression.binop.LogicOr;
import ast.data.expression.binop.Minus;
import ast.data.expression.binop.Mod;
import ast.data.expression.binop.Mul;
import ast.data.expression.binop.Notequal;
import ast.data.expression.binop.Or;
import ast.data.expression.binop.Plus;
import ast.data.expression.binop.Shl;
import ast.data.expression.binop.Shr;
import ast.data.expression.unop.Not;
import ast.data.expression.unop.Uminus;
import ast.data.expression.value.BoolValue;
import ast.data.expression.value.NumberValue;
import ast.data.expression.value.StringValue;
import ast.data.reference.Reference;
import ast.data.type.Type;
import ast.data.type.base.BooleanType;
import ast.data.type.base.EnumType;
import ast.data.type.base.RangeType;
import ast.dispatcher.DfsTraverser;
import ast.knowledge.KnowComparable;
import ast.knowledge.KnowType;
import ast.knowledge.KnowledgeBase;
import error.ErrorType;
import error.RError;

public class ExpressionTypecheck extends DfsTraverser<Void, Void> {
  final private KnowledgeBase kb;
  final private KnowType kt;
  final private KnowComparable kc;

  public ExpressionTypecheck(KnowledgeBase kb) {
    super();
    this.kb = kb;
    kt = kb.getEntry(KnowType.class);
    kc = kb.getEntry(KnowComparable.class);
  }

  static public void process(Ast ast, KnowledgeBase kb) {
    ExpressionTypecheck adder = new ExpressionTypecheck(kb);
    adder.traverse(ast, null);
  }

  private void checkPositive(ElementInfo info, String op, Range lhs, Range rhs) {
    checkPositive(info, op, lhs);
    checkPositive(info, op, rhs);
  }

  private void checkPositive(ElementInfo info, String op, Range range) {
    if (range.low.compareTo(BigInteger.ZERO) < 0) {
      RError.err(ErrorType.Error, info, op + " only allowed for positive types");
    }
  }

  private Range getRange(Expression expr) {
    Type lhs = kt.get(expr);
    if (!(lhs instanceof RangeType)) {
      RError.err(ErrorType.Fatal, expr.getInfo(), "Expected range type, got " + lhs.name);
      return null;
    } else {
      return ((RangeType) lhs).range;
    }
  }

  public static BigInteger makeOnes(int bits) {
    BigInteger ret = BigInteger.ZERO;
    for (int i = 0; i < bits; i++) {
      ret = ret.shiftLeft(1);
      ret = ret.or(BigInteger.ONE);
    }
    return ret;
  }

  public static int getAsInt(BigInteger value, String errtext) {
    if (value.compareTo(BigInteger.valueOf(Integer.MAX_VALUE)) > 0) {
      RError.err(ErrorType.Error, "value to big, needs to be smaller than " + Integer.MAX_VALUE + " in " + errtext);
    }
    if (value.compareTo(BigInteger.valueOf(Integer.MIN_VALUE)) < 0) {
      RError.err(ErrorType.Error, "value to small, needs to be bigger than " + Integer.MIN_VALUE + " in " + errtext);
    }
    return value.intValue();
  }

  static public int bitCount(BigInteger value) {
    assert (value.compareTo(BigInteger.ZERO) >= 0);
    int bit = 0;
    while (value.compareTo(BigInteger.ZERO) != 0) {
      value = value.shiftRight(1);
      bit++;
    }
    return bit;
  }

  @Override
  protected Void visitReference(Reference obj, Void param) {
    ReferenceTypecheck.process(obj, kb);
    return null;
  }

  @Override
  protected Void visitNot(Not obj, Void param) {
    Type type = kt.get(obj.expr);
    if (type instanceof EnumType) {
      RError.err(ErrorType.Error, obj.getInfo(), "operation not possible on enumerator");
      return null;
    }

    if (!(type instanceof BooleanType)) {
      RError.err(ErrorType.Error, obj.getInfo(), "Need boolean type for not, got: " + type.name); // TODO
      // otherwise
      // it is a bit
      // not
      return null;
    }
    return null;
  }

  @Override
  protected Void visitUminus(Uminus obj, Void param) {
    Type type = kt.get(obj.expr);
    if (!(type instanceof RangeType)) {
      RError.err(ErrorType.Error, obj.getInfo(), "Need ordinal type for minus, got: " + type.name);
    }
    return null;
  }

  private void testForEqualComparable(Type lhs, Type rhs, ElementInfo info) {
    if (!kc.get(lhs, rhs)) {
      RError.err(ErrorType.Error, info, "Incompatible types: " + lhs.name + " <-> " + rhs.name);
    }
  }

  @Override
  protected Void visitEqual(Equal obj, Void param) {
    super.visitEqual(obj, param);
    Type lhs = kt.get(obj.left);
    Type rhs = kt.get(obj.right);
    testForEqualComparable(lhs, rhs, obj.getInfo());
    return null;
  }

  @Override
  protected Void visitNotequal(Notequal obj, Void param) {
    super.visitNotequal(obj, param);
    Type lhs = kt.get(obj.left);
    Type rhs = kt.get(obj.right);
    testForEqualComparable(lhs, rhs, obj.getInfo());
    return null;
  }

  @Override
  protected Void visitGreater(Greater obj, Void param) {
    super.visitGreater(obj, param);
    getRange(obj.left);
    getRange(obj.right);
    return null;
  }

  @Override
  protected Void visitGreaterequal(Greaterequal obj, Void param) {
    super.visitGreaterequal(obj, param);
    getRange(obj.left);
    getRange(obj.right);
    return null;
  }

  @Override
  protected Void visitLess(Less obj, Void param) {
    super.visitLess(obj, param);
    getRange(obj.left);
    getRange(obj.right);
    return null;
  }

  @Override
  protected Void visitLessequal(Lessequal obj, Void param) {
    super.visitLessequal(obj, param);
    getRange(obj.left);
    getRange(obj.right);
    return null;
  }

  @Override
  protected Void visitBitAnd(BitAnd obj, Void param) {
    super.visitBitAnd(obj, param);
    Type lhst = kt.get(obj.left);
    Type rhst = kt.get(obj.right);

    if (lhst instanceof RangeType) {
      if (!(rhst instanceof RangeType)) {
        RError.err(ErrorType.Fatal, rhst.getInfo(), "Expected range type");
      }
      Range lhs = getRange(obj.left);
      Range rhs = getRange(obj.right);
      checkPositive(obj.getInfo(), "and", lhs, rhs);
    } else if (lhst instanceof BooleanType) {
      // TODO we should not get here
      if (!(rhst instanceof BooleanType)) {
        RError.err(ErrorType.Fatal, rhst.getInfo(), "Expected boolean type");
      }
    } else {
      RError.err(ErrorType.Error, lhst.getInfo(), "Expected range or boolean type");
    }
    return null;
  }

  @Override
  protected Void visitLogicAnd(LogicAnd obj, Void param) {
    super.visitLogicAnd(obj, param);
    Type lhst = kt.get(obj.left);
    Type rhst = kt.get(obj.right);

    if (!(lhst instanceof BooleanType)) {
      RError.err(ErrorType.Error, lhst.getInfo(), "Expected boolean type at the left side");
    }
    if (!(rhst instanceof BooleanType)) {
      RError.err(ErrorType.Error, rhst.getInfo(), "Expected boolean type at the right side");
    }
    return null;
  }

  @Override
  protected Void visitBitOr(BitOr obj, Void param) {
    super.visitBitOr(obj, param);
    Type lhst = kt.get(obj.left);
    Type rhst = kt.get(obj.right);

    if (!(lhst instanceof RangeType)) {
      RError.err(ErrorType.Fatal, lhst.getInfo(), "Expected range type");
    }
    if (!(rhst instanceof RangeType)) {
      RError.err(ErrorType.Fatal, rhst.getInfo(), "Expected range type");
    }
    Range lhs = getRange(obj.left);
    Range rhs = getRange(obj.right);
    checkPositive(obj.getInfo(), "and", lhs, rhs);
    return null;
  }

  @Override
  protected Void visitLogicOr(LogicOr obj, Void param) {
    super.visitLogicOr(obj, param);
    throw new RuntimeException("not yet implemented");
  }

  @Override
  protected Void visitDiv(Div obj, Void param) {
    super.visitDiv(obj, param);
    getRange(obj.left);
    Range rhs = getRange(obj.right);
    if ((rhs.low.compareTo(BigInteger.ZERO) == 0) && (rhs.high.compareTo(BigInteger.ZERO) == 0)) {
      RError.err(ErrorType.Error, obj.getInfo(), "division by zero");
    }
    return null;
  }

  @Override
  protected Void visitMinus(Minus obj, Void param) {
    super.visitMinus(obj, param);
    getRange(obj.left);
    getRange(obj.right);
    return null;
  }

  @Override
  protected Void visitMod(Mod obj, Void param) {
    super.visitMod(obj, param);
    Range lhs = getRange(obj.left);
    Range rhs = getRange(obj.right);
    checkPositive(obj.getInfo(), "mod", lhs); // TODO implement mod correctly
    // (and not with 'urem'
    // instruction) and
    // remove this check
    if (rhs.low.compareTo(BigInteger.ZERO) <= 0) {
      RError.err(ErrorType.Error, obj.getInfo(), "right side of mod has to be greater than 0");
    }
    return null;
  }

  @Override
  protected Void visitMul(Mul obj, Void param) {
    super.visitMul(obj, param);
    getRange(obj.left);
    getRange(obj.right);
    return null;
  }

  @Override
  protected Void visitOr(Or obj, Void param) {
    super.visitOr(obj, param);
    Range lhs = getRange(obj.left);
    Range rhs = getRange(obj.right);
    checkPositive(obj.getInfo(), "or", lhs, rhs);
    return null;
  }

  @Override
  protected Void visitPlus(Plus obj, Void param) {
    super.visitPlus(obj, param);
    getRange(obj.left);
    getRange(obj.right);
    return null;
  }

  @Override
  protected Void visitShl(Shl obj, Void param) {
    super.visitShl(obj, param);
    Range lhs = getRange(obj.left);
    Range rhs = getRange(obj.right);
    checkPositive(obj.getInfo(), "shl", lhs, rhs);
    return null;
  }

  @Override
  protected Void visitShr(Shr obj, Void param) {
    super.visitShr(obj, param);
    Range lhs = getRange(obj.left);
    Range rhs = getRange(obj.right);
    checkPositive(obj.getInfo(), "shr", lhs, rhs);
    return null;
  }

  @Override
  protected Void visitTypeCast(TypeCast obj, Void param) {
    super.visitTypeCast(obj, param);
    getRange(obj.value);
    if (!(kt.get(obj.cast) instanceof RangeType)) {
      RError.err(ErrorType.Error, obj.getInfo(), "can only cast to range type");
    }
    return null;
  }

  @Override
  protected Void visitNumber(NumberValue obj, Void param) {
    return null;
  }

  @Override
  protected Void visitStringValue(StringValue obj, Void param) {
    return null;
  }

  @Override
  protected Void visitBoolValue(BoolValue obj, Void param) {
    return null;
  }

}
