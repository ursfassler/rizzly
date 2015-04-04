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

package fun.toevl;

import error.ErrorType;
import error.RError;
import evl.data.Evl;
import evl.data.EvlList;
import evl.data.Named;
import evl.data.expression.Expression;
import evl.data.expression.binop.And;
import evl.data.expression.binop.BitXor;
import evl.data.expression.binop.Div;
import evl.data.expression.binop.Equal;
import evl.data.expression.binop.Greater;
import evl.data.expression.binop.Greaterequal;
import evl.data.expression.binop.Is;
import evl.data.expression.binop.Less;
import evl.data.expression.binop.Lessequal;
import evl.data.expression.binop.Minus;
import evl.data.expression.binop.Mod;
import evl.data.expression.binop.Mul;
import evl.data.expression.binop.Notequal;
import evl.data.expression.binop.Or;
import evl.data.expression.binop.Plus;
import evl.data.expression.binop.Shl;
import evl.data.expression.binop.Shr;
import fun.Fun;
import fun.NullTraverser;
import fun.expression.AnyValue;
import fun.expression.ArithmeticOp;
import fun.expression.BoolValue;
import fun.expression.NamedElementsValue;
import fun.expression.Number;
import fun.expression.Relation;
import fun.expression.StringValue;
import fun.expression.TupleValue;
import fun.expression.UnaryExpression;
import fun.expression.reference.RefItem;
import fun.expression.reference.Reference;
import fun.expression.reference.SimpleRef;

public class FunToEvlExpr extends NullTraverser<Evl, Void> {
  private FunToEvl fta;

  public FunToEvlExpr(FunToEvl fta) {
    super();
    this.fta = fta;
  }

  @Override
  protected Evl visitDefault(Fun obj, Void param) {
    throw new RuntimeException("not yet implemented: " + obj.getClass().getCanonicalName());
  }

  // ----------------------------------------------------------------------------
  @Override
  protected Expression visitReference(Reference obj, Void param) {
    EvlList<evl.data.expression.reference.RefItem> ofs = new EvlList<evl.data.expression.reference.RefItem>();
    for (RefItem item : obj.getOffset()) {
      ofs.add((evl.data.expression.reference.RefItem) fta.traverse(item, null));
    }
    Named ref = (Named) fta.traverse(obj.getLink(), null);
    evl.data.expression.reference.Reference ret = new evl.data.expression.reference.Reference(obj.getInfo(), ref, ofs);
    return ret;
  }

  @Override
  protected Evl visitSimpleRef(SimpleRef obj, Void param) {
    Evl ref = fta.traverse(obj.getLink(), null);
    return new evl.data.expression.reference.SimpleRef(obj.getInfo(), (Named) ref);
  }

  @Override
  protected Expression visitNumber(Number obj, Void param) {
    return new evl.data.expression.Number(obj.getInfo(), obj.getValue());
  }

  @Override
  protected Expression visitStringValue(StringValue obj, Void param) {
    return new evl.data.expression.StringValue(obj.getInfo(), obj.getValue());
  }

  @Override
  protected Evl visitTupleValue(TupleValue obj, Void param) {
    EvlList<evl.data.expression.Expression> value = new EvlList<evl.data.expression.Expression>();
    for (fun.expression.Expression item : obj.getValue()) {
      value.add((evl.data.expression.Expression) fta.traverse(item, null));
    }
    return new evl.data.expression.TupleValue(obj.getInfo(), value);
  }

  @Override
  protected Evl visitNamedElementsValue(NamedElementsValue obj, Void param) {
    EvlList<evl.data.expression.NamedValue> value = new EvlList<evl.data.expression.NamedValue>();
    for (fun.expression.NamedValue item : obj.getValue()) {
      value.add((evl.data.expression.NamedValue) fta.traverse(item, null));
    }
    return new evl.data.expression.NamedElementsValue(obj.getInfo(), value);
  }

  @Override
  protected Expression visitBoolValue(BoolValue obj, Void param) {
    return new evl.data.expression.BoolValue(obj.getInfo(), obj.isValue());
  }

  @Override
  protected Evl visitAnyValue(AnyValue obj, Void param) {
    return new evl.data.expression.AnyValue(obj.getInfo());
  }

  @Override
  protected Expression visitArithmeticOp(ArithmeticOp obj, Void param) {
    switch (obj.getOp()) {
      case PLUS:
        return new Plus(obj.getInfo(), (Expression) fta.traverse(obj.getLeft(), null), (Expression) fta.traverse(obj.getRight(), null));
      case MINUS:
        return new Minus(obj.getInfo(), (Expression) fta.traverse(obj.getLeft(), null), (Expression) fta.traverse(obj.getRight(), null));
      case MUL:
        return new Mul(obj.getInfo(), (Expression) fta.traverse(obj.getLeft(), null), (Expression) fta.traverse(obj.getRight(), null));
      case DIV:
        return new Div(obj.getInfo(), (Expression) fta.traverse(obj.getLeft(), null), (Expression) fta.traverse(obj.getRight(), null));
      case MOD:
        return new Mod(obj.getInfo(), (Expression) fta.traverse(obj.getLeft(), null), (Expression) fta.traverse(obj.getRight(), null));
      case AND:
        return new And(obj.getInfo(), (Expression) fta.traverse(obj.getLeft(), null), (Expression) fta.traverse(obj.getRight(), null));
      case OR:
        return new Or(obj.getInfo(), (Expression) fta.traverse(obj.getLeft(), null), (Expression) fta.traverse(obj.getRight(), null));
      case SHL:
        return new Shl(obj.getInfo(), (Expression) fta.traverse(obj.getLeft(), null), (Expression) fta.traverse(obj.getRight(), null));
      case SHR:
        return new Shr(obj.getInfo(), (Expression) fta.traverse(obj.getLeft(), null), (Expression) fta.traverse(obj.getRight(), null));
      case XOR:
        return new BitXor(obj.getInfo(), (Expression) fta.traverse(obj.getLeft(), null), (Expression) fta.traverse(obj.getRight(), null));
      default:
        RError.err(ErrorType.Fatal, obj.getInfo(), "Unhandled case: " + obj.getOp());
    }
    return null;
  }

  @Override
  protected Expression visitRelation(Relation obj, Void param) {
    switch (obj.getOp()) {
      case EQUAL:
        return new Equal(obj.getInfo(), (Expression) fta.traverse(obj.getLeft(), null), (Expression) fta.traverse(obj.getRight(), null));
      case NOT_EQUAL:
        return new Notequal(obj.getInfo(), (Expression) fta.traverse(obj.getLeft(), null), (Expression) fta.traverse(obj.getRight(), null));
      case GREATER:
        return new Greater(obj.getInfo(), (Expression) fta.traverse(obj.getLeft(), null), (Expression) fta.traverse(obj.getRight(), null));
      case GREATER_EQUEAL:
        return new Greaterequal(obj.getInfo(), (Expression) fta.traverse(obj.getLeft(), null), (Expression) fta.traverse(obj.getRight(), null));
      case LESS:
        return new Less(obj.getInfo(), (Expression) fta.traverse(obj.getLeft(), null), (Expression) fta.traverse(obj.getRight(), null));
      case LESS_EQUAL:
        return new Lessequal(obj.getInfo(), (Expression) fta.traverse(obj.getLeft(), null), (Expression) fta.traverse(obj.getRight(), null));
      case IS:
        return new Is(obj.getInfo(), (Expression) fta.traverse(obj.getLeft(), null), (Expression) fta.traverse(obj.getRight(), null));
      default:
        RError.err(ErrorType.Fatal, obj.getInfo(), "Unhandled case: " + obj.getOp());
    }
    return null;
  }

  @Override
  protected Expression visitUnaryExpression(UnaryExpression obj, Void param) {
    switch (obj.getOp()) {
      case MINUS:
        return new evl.data.expression.unop.Uminus(obj.getInfo(), (Expression) fta.traverse(obj.getExpr(), null));
      case NOT:
        return new evl.data.expression.unop.Not(obj.getInfo(), (Expression) fta.traverse(obj.getExpr(), null));
      default:
        RError.err(ErrorType.Fatal, obj.getInfo(), "Unhandled case: " + obj.getOp());
    }
    return null;
  }
}
