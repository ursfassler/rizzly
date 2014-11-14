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

package evl.knowledge;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import util.Range;

import common.Designator;
import common.Direction;
import common.ElementInfo;

import error.ErrorType;
import error.RError;
import evl.Evl;
import evl.NullTraverser;
import evl.expression.ArrayValue;
import evl.expression.BoolValue;
import evl.expression.Expression;
import evl.expression.Number;
import evl.expression.RecordValue;
import evl.expression.StringValue;
import evl.expression.TypeCast;
import evl.expression.UnionValue;
import evl.expression.UnsafeUnionValue;
import evl.expression.binop.And;
import evl.expression.binop.BitAnd;
import evl.expression.binop.BitOr;
import evl.expression.binop.Div;
import evl.expression.binop.LogicAnd;
import evl.expression.binop.LogicOr;
import evl.expression.binop.Minus;
import evl.expression.binop.Mod;
import evl.expression.binop.Mul;
import evl.expression.binop.Or;
import evl.expression.binop.Plus;
import evl.expression.binop.Relation;
import evl.expression.binop.Shl;
import evl.expression.binop.Shr;
import evl.expression.reference.RefItem;
import evl.expression.reference.Reference;
import evl.expression.reference.SimpleRef;
import evl.expression.unop.BitNot;
import evl.expression.unop.LogicNot;
import evl.expression.unop.Uminus;
import evl.function.Function;
import evl.function.InterfaceFunction;
import evl.other.CompUse;
import evl.other.Component;
import evl.other.EvlList;
import evl.traverser.RefTypeGetter;
import evl.traverser.typecheck.specific.ExpressionTypeChecker;
import evl.type.Type;
import evl.type.base.ArrayType;
import evl.type.base.BooleanType;
import evl.type.base.EnumElement;
import evl.type.base.EnumType;
import evl.type.base.FunctionType;
import evl.type.base.RangeType;
import evl.type.composed.NamedElement;
import evl.type.special.ComponentType;
import evl.variable.FuncVariable;
import evl.variable.Variable;

public class KnowType extends KnowledgeEntry {
  private KnowTypeTraverser ktt;

  @Override
  public void init(KnowledgeBase base) {
    ktt = new KnowTypeTraverser(base);
  }

  public Type get(Evl evl) {
    return ktt.traverse(evl, null);
  }

}

class KnowTypeTraverser extends NullTraverser<Type, Void> {
  private final Map<Evl, Type> cache = new HashMap<Evl, Type>();
  final private KnowBaseItem kbi;
  final private RefTypeGetter rtg;
  final private KnowParent kp;

  public KnowTypeTraverser(KnowledgeBase kb) {
    super();
    kbi = kb.getEntry(KnowBaseItem.class);
    kp = kb.getEntry(KnowParent.class);
    rtg = new RefTypeGetter(kb);
  }

  private void checkPositive(ElementInfo info, String op, Range lhs, Range rhs) {
    checkPositive(info, op, lhs);
    checkPositive(info, op, rhs);
  }

  private void checkPositive(ElementInfo info, String op, Range range) {
    if (range.getLow().compareTo(BigInteger.ZERO) < 0) {
      RError.err(ErrorType.Error, info, op + " only allowed for positive types");
    }
  }

  @Override
  protected Type visitDefault(Evl obj, Void param) {
    throw new RuntimeException("not yet implemented: " + obj.getClass().getCanonicalName());
  }

  @Override
  protected Type visit(Evl obj, Void param) {
    Type type = cache.get(obj);
    if (type == null) {
      type = super.visit(obj, param);
      assert (type != null);
      cache.put(obj, type);
    }
    return type;
  }

  @Override
  protected Type visitNamedElement(NamedElement obj, Void param) {
    return visit(obj.getRef(), param);
  }

  @Override
  protected Type visitTypeCast(TypeCast obj, Void param) {
    return visit(obj.getCast(), param);
  }

  @Override
  protected Type visitFunctionImpl(Function obj, Void param) {
    EvlList<SimpleRef<Type>> arg = new EvlList<SimpleRef<Type>>();
    for (FuncVariable var : obj.getParam()) {
      arg.add(new SimpleRef<Type>(ElementInfo.NO, var.getType().getLink()));
    }
    return new FunctionType(obj.getInfo(), obj.getName(), arg, new SimpleRef<Type>(ElementInfo.NO, obj.getRet().getLink()));
  }

  @Override
  protected Type visitComponent(Component obj, Void param) {
    ComponentType ct = new ComponentType(obj.getInfo(), Designator.NAME_SEP + "T" + Designator.NAME_SEP + obj.getName());
    makeFuncTypes(ct.getInput(), obj.getIface(Direction.in));
    makeFuncTypes(ct.getOutput(), obj.getIface(Direction.out));
    return ct;
  }

  private void makeFuncTypes(EvlList<NamedElement> flist, EvlList<InterfaceFunction> evlList) {
    for (InterfaceFunction itr : evlList) {
      NamedElement ne = new NamedElement(itr.getInfo(), itr.getName(), new SimpleRef<Type>(itr.getInfo(), visit(itr, null)));
      flist.add(ne);
    }
  }

  @Override
  protected Type visitBoolValue(BoolValue obj, Void param) {
    return kbi.getBooleanType();
  }

  @Override
  protected Type visitNumber(Number obj, Void param) {
    return kbi.getNumsetType(new Range(obj.getValue(), obj.getValue()));
  }

  @Override
  protected Type visitUnsafeUnionValue(UnsafeUnionValue obj, Void param) {
    throw new RuntimeException("not yet implemented");
  }

  @Override
  protected Type visitRecordValue(RecordValue obj, Void param) {
    return visit(obj.getType(), param);
  }

  @Override
  protected Type visitUnionValue(UnionValue obj, Void param) {
    throw new RuntimeException("not yet implemented");
  }

  @Override
  protected Type visitEnumElement(EnumElement obj, Void param) {
    Evl parent = kp.getParent(obj);
    assert (parent instanceof EnumType);
    return (EnumType) parent;
  }

  @Override
  protected Type visitReference(Reference obj, Void param) {
    Type base = visit(obj.getLink(), param);
    for (RefItem itm : obj.getOffset()) {
      base = rtg.traverse(itm, base);
    }
    return base;
  }

  @Override
  protected Type visitVariable(Variable obj, Void param) {
    return visit(obj.getType(), param);
  }

  @Override
  protected Type visitTypeRef(SimpleRef obj, Void param) {
    return visit(obj.getLink(), param);
  }

  @Override
  protected Type visitCompUse(CompUse obj, Void param) {
    return visit(obj.getLink(), param);
  }

  @Override
  protected Type visitType(Type obj, Void param) {
    return obj;
  }

  @Override
  protected Type visitRelation(Relation obj, Void param) {
    return kbi.getBooleanType();
  }

  @Override
  protected Type visitShl(Shl obj, Void param) {
    Type lhs = visit(obj.getLeft(), param);
    Type rhs = visit(obj.getRight(), param);

    assert (lhs instanceof RangeType);
    assert (rhs instanceof RangeType);

    Range lr = ((RangeType) lhs).getNumbers();
    Range rr = ((RangeType) rhs).getNumbers();

    BigInteger high = lr.getHigh().shiftLeft(ExpressionTypeChecker.getAsInt(rr.getHigh(), "shl"));
    BigInteger low = lr.getLow().shiftLeft(ExpressionTypeChecker.getAsInt(rr.getLow(), "shl"));

    return kbi.getNumsetType(new Range(low, high));
  }

  @Override
  protected Type visitShr(Shr obj, Void param) {
    Type lhs = visit(obj.getLeft(), param);
    Type rhs = visit(obj.getRight(), param);

    assert (lhs instanceof RangeType);
    assert (rhs instanceof RangeType);

    Range lr = ((RangeType) lhs).getNumbers();
    Range rr = ((RangeType) rhs).getNumbers();

    BigInteger high = lr.getHigh().shiftRight(ExpressionTypeChecker.getAsInt(rr.getHigh(), "shl"));
    BigInteger low = lr.getLow().shiftRight(ExpressionTypeChecker.getAsInt(rr.getLow(), "shl"));

    return kbi.getNumsetType(new Range(low, high));
  }

  @Override
  protected Type visitPlus(Plus obj, Void param) {
    Type lhs = visit(obj.getLeft(), param);
    Type rhs = visit(obj.getRight(), param);

    assert (lhs instanceof RangeType);
    assert (rhs instanceof RangeType);

    Range lr = ((RangeType) lhs).getNumbers();
    Range rr = ((RangeType) rhs).getNumbers();

    BigInteger low = lr.getLow().add(rr.getLow());
    BigInteger high = lr.getHigh().add(rr.getHigh());

    return kbi.getNumsetType(new Range(low, high));
  }

  @Override
  protected Type visitMinus(Minus obj, Void param) {
    Type lhs = visit(obj.getLeft(), param);
    Type rhs = visit(obj.getRight(), param);

    assert (lhs instanceof RangeType);
    assert (rhs instanceof RangeType);

    Range lr = ((RangeType) lhs).getNumbers();
    Range rr = ((RangeType) rhs).getNumbers();

    BigInteger low = lr.getLow().subtract(rr.getHigh());
    BigInteger high = lr.getHigh().subtract(rr.getLow());

    return kbi.getNumsetType(new Range(low, high));

  }

  @Override
  protected Type visitLogicOr(LogicOr obj, Void param) {
    return kbi.getBooleanType();
  }

  @Override
  protected Type visitBitOr(BitOr obj, Void param) {
    Type lhst = visit(obj.getLeft(), param);
    Type rhst = visit(obj.getRight(), param);
    assert (lhst instanceof RangeType);
    assert (rhst instanceof RangeType);
    Range lhs = ((RangeType) lhst).getNumbers();
    Range rhs = ((RangeType) rhst).getNumbers();
    return bitOr(obj.getInfo(), lhs, rhs);
  }

  @Override
  protected Type visitOr(Or obj, Void param) {
    Type lhst = visit(obj.getLeft(), param);
    Type rhst = visit(obj.getRight(), param);

    if (lhst instanceof RangeType) {
      Range lhs = ((RangeType) lhst).getNumbers();
      Range rhs = ((RangeType) rhst).getNumbers();
      return bitOr(obj.getInfo(), lhs, rhs);
    } else if (lhst instanceof BooleanType) {
      assert (rhst instanceof BooleanType);
      return lhst;
    } else {
      RError.err(ErrorType.Error, lhst.getInfo(), "Expected range or boolean type");
      return null;
    }
  }

  private Type bitOr(ElementInfo info, Range lhs, Range rhs) {
    checkPositive(info, "or", lhs, rhs);

    BigInteger bigger = lhs.getHigh().max(rhs.getHigh());
    BigInteger smaller = lhs.getHigh().min(rhs.getHigh());

    int bits = ExpressionTypeChecker.bitCount(smaller);
    BigInteger ones = ExpressionTypeChecker.makeOnes(bits);
    BigInteger high = bigger.or(ones);
    BigInteger low = lhs.getLow().max(rhs.getLow());

    return kbi.getNumsetType(new Range(low, high));
  }

  @Override
  protected Type visitLogicAnd(LogicAnd obj, Void param) {
    return kbi.getBooleanType();
  }

  @Override
  protected Type visitBitAnd(BitAnd obj, Void param) {
    Type lhst = visit(obj.getLeft(), param);
    Type rhst = visit(obj.getRight(), param);

    assert (lhst instanceof RangeType);
    assert (rhst instanceof RangeType);
    Range lhs = ((RangeType) lhst).getNumbers();
    Range rhs = ((RangeType) rhst).getNumbers();
    return bitAnd(obj.getInfo(), lhs, rhs);
  }

  private Type bitAnd(ElementInfo info, Range lhs, Range rhs) {
    checkPositive(info, "and", lhs, rhs);
    BigInteger high = lhs.getHigh().min(rhs.getHigh()); // TODO ok?
    return kbi.getNumsetType(new Range(BigInteger.ZERO, high));
  }

  @Override
  protected Type visitAnd(And obj, Void param) {
    Type lhst = visit(obj.getLeft(), param);
    Type rhst = visit(obj.getRight(), param);

    if (lhst instanceof RangeType) {
      assert (rhst instanceof RangeType);
      Range lhs = ((RangeType) lhst).getNumbers();
      Range rhs = ((RangeType) rhst).getNumbers();
      return bitAnd(obj.getInfo(), lhs, rhs);
    } else if (lhst instanceof BooleanType) {
      assert (rhst instanceof BooleanType);
      return lhst;
    } else {
      RError.err(ErrorType.Error, lhst.getInfo(), "Expected range or boolean type");
      return null;
    }
  }

  @Override
  protected Type visitMod(Mod obj, Void param) {
    Type lhs = visit(obj.getLeft(), param);
    Type rhs = visit(obj.getRight(), param);

    assert (lhs instanceof RangeType);
    assert (rhs instanceof RangeType);

    Range lr = ((RangeType) lhs).getNumbers();
    Range rr = ((RangeType) rhs).getNumbers();

    assert (lr.getLow().compareTo(BigInteger.ZERO) >= 0); // TODO implement mod correctly for negative numbers
    assert (rr.getLow().compareTo(BigInteger.ZERO) > 0); // type checker has to find this

    BigInteger low = BigInteger.ZERO;
    BigInteger high = lr.getHigh().min(rr.getHigh().subtract(BigInteger.ONE));

    return kbi.getNumsetType(new Range(low, high));
  }

  @Override
  protected Type visitMul(Mul obj, Void param) {
    Type lhs = visit(obj.getLeft(), param);
    Type rhs = visit(obj.getRight(), param);

    assert (lhs instanceof RangeType);
    assert (rhs instanceof RangeType);

    Range lr = ((RangeType) lhs).getNumbers();
    Range rr = ((RangeType) rhs).getNumbers();

    // FIXME correct when values are negative?

    BigInteger low = lr.getLow().multiply(rr.getLow());
    BigInteger high = lr.getHigh().multiply(rr.getHigh());

    return kbi.getNumsetType(new Range(low, high));
  }

  @Override
  protected Type visitDiv(Div obj, Void param) {
    Type lhs = visit(obj.getLeft(), param);
    Type rhs = visit(obj.getRight(), param);

    assert (lhs instanceof RangeType);
    assert (rhs instanceof RangeType);

    Range lr = ((RangeType) lhs).getNumbers();
    Range rr = ((RangeType) rhs).getNumbers();

    if ((lr.getLow().compareTo(BigInteger.ZERO) < 0) || (rr.getLow().compareTo(BigInteger.ZERO) < 0)) {
      RError.err(ErrorType.Fatal, obj.getInfo(), "sorry, I am too lazy to check for negative numbers");
    }

    BigInteger rhigh = rr.getHigh();
    BigInteger rlow = rr.getLow();

    assert ((rlow.compareTo(BigInteger.ZERO) != 0) || (rhigh.compareTo(BigInteger.ZERO) != 0));

    // division by zero is cought during runtime
    if (rhigh.compareTo(BigInteger.ZERO) == 0) {
      rhigh = rhigh.subtract(BigInteger.ONE);
    }
    if (rlow.compareTo(BigInteger.ZERO) == 0) {
      rlow = rlow.add(BigInteger.ONE);
    }

    BigInteger low = lr.getLow().divide(rhigh);
    BigInteger high = lr.getHigh().divide(rlow);

    return kbi.getNumsetType(new Range(low, high));
  }

  @Override
  protected Type visitLogicNot(LogicNot obj, Void param) {
    return kbi.getBooleanType();
  }

  @Override
  protected Type visitBitNot(BitNot obj, Void param) {
    Type type = visit(obj.getExpr(), param);
    assert (type instanceof RangeType);

    Range range = ((RangeType) type).getNumbers();
    assert (range.getLow().equals(BigInteger.ZERO));
    int bits = range.getHigh().bitCount();
    BigInteger exp = BigInteger.valueOf(2).pow(bits).subtract(BigInteger.ONE);
    assert (exp.equals(range.getHigh()));

    return type;
  }

  @Override
  protected Type visitUminus(Uminus obj, Void param) {
    Type lhs = visit(obj.getExpr(), param);
    assert (lhs instanceof RangeType);
    Range lr = ((RangeType) lhs).getNumbers();
    BigInteger low = BigInteger.ZERO.subtract(lr.getHigh());
    BigInteger high = BigInteger.ZERO.subtract(lr.getLow());
    return kbi.getNumsetType(new Range(low, high));
  }

  @Override
  protected Type visitArrayValue(ArrayValue obj, Void param) {
    Iterator<Expression> itr = obj.getValue().iterator();
    assert (itr.hasNext());
    Range cont = ((RangeType) visit(itr.next(), param)).getNumbers();
    while (itr.hasNext()) {
      Range ntype = ((RangeType) visit(itr.next(), param)).getNumbers();
      cont = Range.grow(cont, ntype);
    }

    RangeType et = kbi.getNumsetType(cont);

    return new ArrayType(BigInteger.valueOf(obj.getValue().size()), new SimpleRef<Type>(ElementInfo.NO, et));
  }

  @Override
  protected Type visitStringValue(StringValue obj, Void param) {
    return kbi.getStringType();
  }
}
