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

package evl.copy;

import java.util.ArrayList;

import evl.data.Evl;
import evl.data.expression.AnyValue;
import evl.data.expression.ArrayValue;
import evl.data.expression.BoolValue;
import evl.data.expression.Expression;
import evl.data.expression.NamedElementsValue;
import evl.data.expression.NamedValue;
import evl.data.expression.Number;
import evl.data.expression.RecordValue;
import evl.data.expression.StringValue;
import evl.data.expression.TupleValue;
import evl.data.expression.TypeCast;
import evl.data.expression.UnionValue;
import evl.data.expression.UnsafeUnionValue;
import evl.data.expression.binop.And;
import evl.data.expression.binop.BitAnd;
import evl.data.expression.binop.BitOr;
import evl.data.expression.binop.BitXor;
import evl.data.expression.binop.Div;
import evl.data.expression.binop.Equal;
import evl.data.expression.binop.Greater;
import evl.data.expression.binop.Greaterequal;
import evl.data.expression.binop.Is;
import evl.data.expression.binop.Less;
import evl.data.expression.binop.Lessequal;
import evl.data.expression.binop.LogicAnd;
import evl.data.expression.binop.LogicOr;
import evl.data.expression.binop.Minus;
import evl.data.expression.binop.Mod;
import evl.data.expression.binop.Mul;
import evl.data.expression.binop.Notequal;
import evl.data.expression.binop.Or;
import evl.data.expression.binop.Plus;
import evl.data.expression.binop.Shl;
import evl.data.expression.binop.Shr;
import evl.data.expression.reference.Reference;
import evl.data.expression.reference.SimpleRef;
import evl.data.expression.unop.BitNot;
import evl.data.expression.unop.LogicNot;
import evl.data.expression.unop.Not;
import evl.data.expression.unop.Uminus;
import evl.traverser.NullTraverser;

public class CopyExpression extends NullTraverser<Expression, Void> {

  private CopyEvl cast;

  public CopyExpression(CopyEvl cast) {
    super();
    this.cast = cast;
  }

  @Override
  protected Expression visitDefault(Evl obj, Void param) {
    throw new RuntimeException("not yet implemented: " + obj.getClass().getCanonicalName());
  }

  @Override
  protected Expression visitSimpleRef(SimpleRef obj, Void param) {
    return new SimpleRef(obj.getInfo(), obj.link); // we keep link to old type
  }

  @Override
  protected Expression visitTypeCast(TypeCast obj, Void param) {
    return new TypeCast(obj.getInfo(), cast.copy(obj.cast), cast.copy(obj.value));
  }

  @Override
  protected Expression visitReference(Reference obj, Void param) {
    return new Reference(obj.getInfo(), obj.link, cast.copy(obj.offset));
  }

  @Override
  protected Expression visitBoolValue(BoolValue obj, Void param) {
    return new BoolValue(obj.getInfo(), obj.value);
  }

  @Override
  protected Expression visitAnyValue(AnyValue obj, Void param) {
    return new AnyValue(obj.getInfo());
  }

  @Override
  protected Expression visitNumber(Number obj, Void param) {
    return new Number(obj.getInfo(), obj.value);
  }

  @Override
  protected Expression visitStringValue(StringValue obj, Void param) {
    return new StringValue(obj.getInfo(), obj.value);
  }

  @Override
  protected Expression visitArrayValue(ArrayValue obj, Void param) {
    return new ArrayValue(obj.getInfo(), cast.copy(obj.value));
  }

  @Override
  protected Expression visitTupleValue(TupleValue obj, Void param) {
    return new TupleValue(obj.getInfo(), cast.copy(obj.value));
  }

  @Override
  protected Expression visitRecordValue(RecordValue obj, Void param) {
    return new RecordValue(obj.getInfo(), new ArrayList<NamedValue>(cast.copy(obj.value)), cast.copy(obj.type));
  }

  @Override
  protected Expression visitNamedElementsValue(NamedElementsValue obj, Void param) {
    return new NamedElementsValue(obj.getInfo(), cast.copy(obj.value));
  }

  @Override
  protected Expression visitUnsafeUnionValue(UnsafeUnionValue obj, Void param) {
    return new UnsafeUnionValue(obj.getInfo(), cast.copy(obj.contentValue), cast.copy(obj.type));
  }

  @Override
  protected Expression visitUnionValue(UnionValue obj, Void param) {
    return new UnionValue(obj.getInfo(), cast.copy(obj.tagValue), cast.copy(obj.contentValue), cast.copy(obj.type));
  }

  @Override
  protected Expression visitMinus(Minus obj, Void param) {
    return new Minus(obj.getInfo(), cast.copy(obj.left), cast.copy(obj.right));
  }

  @Override
  protected Expression visitAnd(And obj, Void param) {
    return new And(obj.getInfo(), cast.copy(obj.left), cast.copy(obj.right));
  }

  @Override
  protected Expression visitDiv(Div obj, Void param) {
    return new Div(obj.getInfo(), cast.copy(obj.left), cast.copy(obj.right));
  }

  @Override
  protected Expression visitEqual(Equal obj, Void param) {
    return new Equal(obj.getInfo(), cast.copy(obj.left), cast.copy(obj.right));
  }

  @Override
  protected Expression visitGreater(Greater obj, Void param) {
    return new Greater(obj.getInfo(), cast.copy(obj.left), cast.copy(obj.right));
  }

  @Override
  protected Expression visitLess(Less obj, Void param) {
    return new Less(obj.getInfo(), cast.copy(obj.left), cast.copy(obj.right));
  }

  @Override
  protected Expression visitGreaterequal(Greaterequal obj, Void param) {
    return new Greaterequal(obj.getInfo(), cast.copy(obj.left), cast.copy(obj.right));
  }

  @Override
  protected Expression visitLessequal(Lessequal obj, Void param) {
    return new Lessequal(obj.getInfo(), cast.copy(obj.left), cast.copy(obj.right));
  }

  @Override
  protected Expression visitIs(Is obj, Void param) {
    return new Is(obj.getInfo(), cast.copy(obj.left), cast.copy(obj.right));
  }

  @Override
  protected Expression visitMod(Mod obj, Void param) {
    return new Mod(obj.getInfo(), cast.copy(obj.left), cast.copy(obj.right));
  }

  @Override
  protected Expression visitMul(Mul obj, Void param) {
    return new Mul(obj.getInfo(), cast.copy(obj.left), cast.copy(obj.right));
  }

  @Override
  protected Expression visitNotequal(Notequal obj, Void param) {
    return new Notequal(obj.getInfo(), cast.copy(obj.left), cast.copy(obj.right));
  }

  @Override
  protected Expression visitOr(Or obj, Void param) {
    return new Or(obj.getInfo(), cast.copy(obj.left), cast.copy(obj.right));
  }

  @Override
  protected Expression visitPlus(Plus obj, Void param) {
    return new Plus(obj.getInfo(), cast.copy(obj.left), cast.copy(obj.right));
  }

  @Override
  protected Expression visitShl(Shl obj, Void param) {
    return new Shl(obj.getInfo(), cast.copy(obj.left), cast.copy(obj.right));
  }

  @Override
  protected Expression visitShr(Shr obj, Void param) {
    return new Shr(obj.getInfo(), cast.copy(obj.left), cast.copy(obj.right));
  }

  @Override
  protected Expression visitNot(Not obj, Void param) {
    return new Not(obj.getInfo(), cast.copy(obj.expr));
  }

  @Override
  protected Expression visitLogicNot(LogicNot obj, Void param) {
    return new LogicNot(obj.getInfo(), cast.copy(obj.expr));
  }

  @Override
  protected Expression visitBitNot(BitNot obj, Void param) {
    return new BitNot(obj.getInfo(), cast.copy(obj.expr));
  }

  @Override
  protected Expression visitUminus(Uminus obj, Void param) {
    return new Uminus(obj.getInfo(), cast.copy(obj.expr));
  }

  @Override
  protected Expression visitBitAnd(BitAnd obj, Void param) {
    return new BitAnd(obj.getInfo(), cast.copy(obj.left), cast.copy(obj.right));
  }

  @Override
  protected Expression visitBitOr(BitOr obj, Void param) {
    return new BitOr(obj.getInfo(), cast.copy(obj.left), cast.copy(obj.right));
  }

  @Override
  protected Expression visitBitXor(BitXor obj, Void param) {
    return new BitXor(obj.getInfo(), cast.copy(obj.left), cast.copy(obj.right));
  }

  @Override
  protected Expression visitLogicOr(LogicOr obj, Void param) {
    return new LogicOr(obj.getInfo(), cast.copy(obj.left), cast.copy(obj.right));
  }

  @Override
  protected Expression visitLogicAnd(LogicAnd obj, Void param) {
    return new LogicAnd(obj.getInfo(), cast.copy(obj.left), cast.copy(obj.right));
  }

}
