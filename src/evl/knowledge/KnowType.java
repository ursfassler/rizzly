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
import evl.data.Evl;
import evl.data.EvlList;
import evl.data.component.Component;
import evl.data.component.composition.CompUse;
import evl.data.expression.AnyValue;
import evl.data.expression.ArrayValue;
import evl.data.expression.BoolValue;
import evl.data.expression.Expression;
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
import evl.data.expression.binop.LogicAnd;
import evl.data.expression.binop.LogicOr;
import evl.data.expression.binop.Minus;
import evl.data.expression.binop.Mod;
import evl.data.expression.binop.Mul;
import evl.data.expression.binop.Or;
import evl.data.expression.binop.Plus;
import evl.data.expression.binop.Relation;
import evl.data.expression.binop.Shl;
import evl.data.expression.binop.Shr;
import evl.data.expression.reference.RefItem;
import evl.data.expression.reference.Reference;
import evl.data.expression.reference.SimpleRef;
import evl.data.expression.reference.TypeRef;
import evl.data.expression.unop.BitNot;
import evl.data.expression.unop.LogicNot;
import evl.data.expression.unop.Uminus;
import evl.data.function.Function;
import evl.data.function.InterfaceFunction;
import evl.data.function.ret.FuncReturnNone;
import evl.data.function.ret.FuncReturnTuple;
import evl.data.function.ret.FuncReturnType;
import evl.data.type.Type;
import evl.data.type.base.ArrayType;
import evl.data.type.base.BooleanType;
import evl.data.type.base.EnumElement;
import evl.data.type.base.EnumType;
import evl.data.type.base.FunctionType;
import evl.data.type.base.RangeType;
import evl.data.type.base.TupleType;
import evl.data.type.composed.NamedElement;
import evl.data.type.composed.RecordType;
import evl.data.type.special.ComponentType;
import evl.data.variable.FuncVariable;
import evl.data.variable.Variable;
import evl.pass.typecheck.ExpressionTypecheck;
import evl.traverser.NullTraverser;
import evl.traverser.other.RefTypeGetter;

public class KnowType extends KnowledgeEntry {
  private KnowTypeTraverser ktt;

  @Override
  public void init(KnowledgeBase base) {
    ktt = new KnowTypeTraverser(this, base);
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

  public KnowTypeTraverser(KnowType kt, KnowledgeBase kb) {
    super();
    kbi = kb.getEntry(KnowBaseItem.class);
    kp = kb.getEntry(KnowParent.class);
    rtg = new RefTypeGetter(kt, kb);
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
    return visit(obj.typeref, param);
  }

  @Override
  protected Type visitTypeCast(TypeCast obj, Void param) {
    return visit(obj.cast, param);
  }

  @Override
  protected Type visitFunction(Function obj, Void param) {
    EvlList<TypeRef> arg = new EvlList<TypeRef>();
    for (FuncVariable var : obj.param) {
      arg.add(new SimpleRef<Type>(ElementInfo.NO, visit(var.type, null)));
    }
    return new FunctionType(obj.getInfo(), obj.name, arg, new SimpleRef<Type>(ElementInfo.NO, visit(obj.ret, param)));
  }

  @Override
  protected Type visitComponent(Component obj, Void param) {
    ComponentType ct = new ComponentType(obj.getInfo(), Designator.NAME_SEP + "T" + Designator.NAME_SEP + obj.name);
    makeFuncTypes(ct.input, obj.getIface(Direction.in));
    makeFuncTypes(ct.output, obj.getIface(Direction.out));
    return ct;
  }

  private void makeFuncTypes(EvlList<NamedElement> flist, EvlList<InterfaceFunction> evlList) {
    for (InterfaceFunction itr : evlList) {
      NamedElement ne = new NamedElement(itr.getInfo(), itr.name, new SimpleRef<Type>(itr.getInfo(), visit(itr, null)));
      flist.add(ne);
    }
  }

  @Override
  protected Type visitAnyValue(AnyValue obj, Void param) {
    return kbi.getAnyType();
  }

  @Override
  protected Type visitBoolValue(BoolValue obj, Void param) {
    return kbi.getBooleanType();
  }

  @Override
  protected Type visitNumber(Number obj, Void param) {
    return kbi.getRangeType(new Range(obj.value, obj.value));
  }

  @Override
  protected Type visitUnsafeUnionValue(UnsafeUnionValue obj, Void param) {
    throw new RuntimeException("not yet implemented");
  }

  @Override
  protected Type visitRecordValue(RecordValue obj, Void param) {
    return visit(obj.type, param);
  }

  @Override
  protected Type visitTupleValue(TupleValue obj, Void param) {
    if (obj.value.size() == 1) {
      return visit(obj.value.get(0), param);
    } else {
      EvlList<TypeRef> elem = new EvlList<TypeRef>();
      for (Expression expr : obj.value) {
        TypeRef ref = new SimpleRef<Type>(expr.getInfo(), visit(expr, null));
        elem.add(ref);
      }
      return new TupleType(obj.getInfo(), "", elem);
    }
  }

  @Override
  protected Type visitUnionValue(UnionValue obj, Void param) {
    throw new RuntimeException("not yet implemented");
  }

  @Override
  protected Type visitEnumElement(EnumElement obj, Void param) {
    Evl parent = kp.get(obj);
    assert (parent instanceof EnumType);
    return (EnumType) parent;
  }

  @Override
  protected Type visitReference(Reference obj, Void param) {
    Type base = visit(obj.link, param);
    for (RefItem itm : obj.offset) {
      base = rtg.traverse(itm, base);
    }
    return base;
  }

  @Override
  protected Type visitVariable(Variable obj, Void param) {
    return visit(obj.type, param);
  }

  @Override
  protected Type visitSimpleRef(SimpleRef obj, Void param) {
    return visit(obj.link, param);
  }

  @Override
  protected Type visitCompUse(CompUse obj, Void param) {
    return visit(obj.compRef, param);
  }

  @Override
  protected Type visitFuncReturnTuple(FuncReturnTuple obj, Void param) {
    EvlList<NamedElement> types = new EvlList<NamedElement>();
    for (FuncVariable var : obj.param) {
      types.add(new NamedElement(ElementInfo.NO, var.name, var.type));
    }
    return new RecordType(obj.getInfo(), "", types);
  }

  @Override
  protected Type visitFuncReturnType(FuncReturnType obj, Void param) {
    return visit(obj.type, param);
  }

  @Override
  protected Type visitFuncReturnNone(FuncReturnNone obj, Void param) {
    return kbi.getVoidType();
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
    Type lhs = visit(obj.left, param);
    Type rhs = visit(obj.right, param);

    assert (lhs instanceof RangeType);
    assert (rhs instanceof RangeType);

    Range lr = ((RangeType) lhs).range;
    Range rr = ((RangeType) rhs).range;

    BigInteger high = lr.high.shiftLeft(ExpressionTypecheck.getAsInt(rr.high, "shl"));
    BigInteger low = lr.low.shiftLeft(ExpressionTypecheck.getAsInt(rr.low, "shl"));

    return kbi.getRangeType(new Range(low, high));
  }

  @Override
  protected Type visitShr(Shr obj, Void param) {
    Type lhs = visit(obj.left, param);
    Type rhs = visit(obj.right, param);

    assert (lhs instanceof RangeType);
    assert (rhs instanceof RangeType);

    Range lr = ((RangeType) lhs).range;
    Range rr = ((RangeType) rhs).range;

    BigInteger high = lr.high.shiftRight(ExpressionTypecheck.getAsInt(rr.high, "shl"));
    BigInteger low = lr.low.shiftRight(ExpressionTypecheck.getAsInt(rr.low, "shl"));

    return kbi.getRangeType(new Range(low, high));
  }

  @Override
  protected Type visitPlus(Plus obj, Void param) {
    Type lhs = visit(obj.left, param);
    Type rhs = visit(obj.right, param);

    assert (lhs instanceof RangeType);
    assert (rhs instanceof RangeType);

    Range lr = ((RangeType) lhs).range;
    Range rr = ((RangeType) rhs).range;

    BigInteger low = lr.low.add(rr.low);
    BigInteger high = lr.high.add(rr.high);

    return kbi.getRangeType(new Range(low, high));
  }

  @Override
  protected Type visitMinus(Minus obj, Void param) {
    Type lhs = visit(obj.left, param);
    Type rhs = visit(obj.right, param);

    assert (lhs instanceof RangeType);
    assert (rhs instanceof RangeType);

    Range lr = ((RangeType) lhs).range;
    Range rr = ((RangeType) rhs).range;

    BigInteger low = lr.low.subtract(rr.high);
    BigInteger high = lr.high.subtract(rr.low);

    return kbi.getRangeType(new Range(low, high));

  }

  @Override
  protected Type visitLogicOr(LogicOr obj, Void param) {
    return kbi.getBooleanType();
  }

  @Override
  protected Type visitBitOr(BitOr obj, Void param) {
    Type lhst = visit(obj.left, param);
    Type rhst = visit(obj.right, param);
    assert (lhst instanceof RangeType);
    assert (rhst instanceof RangeType);
    Range lhs = ((RangeType) lhst).range;
    Range rhs = ((RangeType) rhst).range;
    return bitOr(obj.getInfo(), lhs, rhs);
  }

  @Override
  protected Type visitOr(Or obj, Void param) {
    Type lhst = visit(obj.left, param);
    Type rhst = visit(obj.right, param);

    if (lhst instanceof RangeType) {
      Range lhs = ((RangeType) lhst).range;
      Range rhs = ((RangeType) rhst).range;
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

    BigInteger bigger = lhs.high.max(rhs.high);
    BigInteger smaller = lhs.high.min(rhs.high);

    int bits = ExpressionTypecheck.bitCount(smaller);
    BigInteger ones = ExpressionTypecheck.makeOnes(bits);
    BigInteger high = bigger.or(ones);
    BigInteger low = lhs.low.max(rhs.low);

    return kbi.getRangeType(new Range(low, high));
  }

  @Override
  protected Type visitBitXor(BitXor obj, Void param) {
    Type lhst = visit(obj.left, param);
    Type rhst = visit(obj.right, param);

    if (lhst instanceof RangeType) {
      assert (rhst instanceof RangeType);
      Range lhs = ((RangeType) lhst).range;
      Range rhs = ((RangeType) rhst).range;

      checkPositive(obj.getInfo(), "xor", lhs, rhs);

      BigInteger bigger = lhs.high.max(rhs.high);

      int bits = ExpressionTypecheck.bitCount(bigger);
      BigInteger ones = ExpressionTypecheck.makeOnes(bits);
      BigInteger high = bigger.or(ones);

      return kbi.getRangeType(new Range(BigInteger.ZERO, high));
    } else if (lhst instanceof BooleanType) {
      assert (rhst instanceof BooleanType);
      return lhst;
    } else {
      RError.err(ErrorType.Error, lhst.getInfo(), "Expected range or boolean type");
      return null;
    }
  }

  @Override
  protected Type visitLogicAnd(LogicAnd obj, Void param) {
    return kbi.getBooleanType();
  }

  @Override
  protected Type visitBitAnd(BitAnd obj, Void param) {
    Type lhst = visit(obj.left, param);
    Type rhst = visit(obj.right, param);

    assert (lhst instanceof RangeType);
    assert (rhst instanceof RangeType);
    Range lhs = ((RangeType) lhst).range;
    Range rhs = ((RangeType) rhst).range;
    return bitAnd(obj.getInfo(), lhs, rhs);
  }

  private Type bitAnd(ElementInfo info, Range lhs, Range rhs) {
    checkPositive(info, "and", lhs, rhs);
    BigInteger high = lhs.high.min(rhs.high); // TODO ok?
    return kbi.getRangeType(new Range(BigInteger.ZERO, high));
  }

  @Override
  protected Type visitAnd(And obj, Void param) {
    Type lhst = visit(obj.left, param);
    Type rhst = visit(obj.right, param);

    if (lhst instanceof RangeType) {
      assert (rhst instanceof RangeType);
      Range lhs = ((RangeType) lhst).range;
      Range rhs = ((RangeType) rhst).range;
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
    Type lhs = visit(obj.left, param);
    Type rhs = visit(obj.right, param);

    assert (lhs instanceof RangeType);
    assert (rhs instanceof RangeType);

    Range lr = ((RangeType) lhs).range;
    Range rr = ((RangeType) rhs).range;

    assert (lr.low.compareTo(BigInteger.ZERO) >= 0); // TODO implement mod
    // correctly for
    // negative numbers
    assert (rr.low.compareTo(BigInteger.ZERO) > 0); // type checker has to
    // find this

    BigInteger low = BigInteger.ZERO;
    BigInteger high = lr.high.min(rr.high.subtract(BigInteger.ONE));

    return kbi.getRangeType(new Range(low, high));
  }

  @Override
  protected Type visitMul(Mul obj, Void param) {
    Type lhs = visit(obj.left, param);
    Type rhs = visit(obj.right, param);

    assert (lhs instanceof RangeType);
    assert (rhs instanceof RangeType);

    Range lr = ((RangeType) lhs).range;
    Range rr = ((RangeType) rhs).range;

    // FIXME correct when values are negative?

    BigInteger low = lr.low.multiply(rr.low);
    BigInteger high = lr.high.multiply(rr.high);

    return kbi.getRangeType(new Range(low, high));
  }

  @Override
  protected Type visitDiv(Div obj, Void param) {
    Type lhs = visit(obj.left, param);
    Type rhs = visit(obj.right, param);

    assert (lhs instanceof RangeType);
    assert (rhs instanceof RangeType);

    Range lr = ((RangeType) lhs).range;
    Range rr = ((RangeType) rhs).range;

    if ((lr.low.compareTo(BigInteger.ZERO) < 0) || (rr.low.compareTo(BigInteger.ZERO) < 0)) {
      RError.err(ErrorType.Fatal, obj.getInfo(), "sorry, I am too lazy to check for negative numbers");
    }

    BigInteger rhigh = rr.high;
    BigInteger rlow = rr.low;

    assert ((rlow.compareTo(BigInteger.ZERO) != 0) || (rhigh.compareTo(BigInteger.ZERO) != 0));

    // division by zero is cought during runtime
    if (rhigh.compareTo(BigInteger.ZERO) == 0) {
      rhigh = rhigh.subtract(BigInteger.ONE);
    }
    if (rlow.compareTo(BigInteger.ZERO) == 0) {
      rlow = rlow.add(BigInteger.ONE);
    }

    BigInteger low = lr.low.divide(rhigh);
    BigInteger high = lr.high.divide(rlow);

    return kbi.getRangeType(new Range(low, high));
  }

  @Override
  protected Type visitLogicNot(LogicNot obj, Void param) {
    return kbi.getBooleanType();
  }

  @Override
  protected Type visitBitNot(BitNot obj, Void param) {
    Type type = visit(obj.expr, param);
    assert (type instanceof RangeType);

    Range range = ((RangeType) type).range;
    assert (range.low.equals(BigInteger.ZERO));
    int bits = range.high.bitCount();
    BigInteger exp = BigInteger.valueOf(2).pow(bits).subtract(BigInteger.ONE);
    assert (exp.equals(range.high));

    return type;
  }

  @Override
  protected Type visitUminus(Uminus obj, Void param) {
    Type lhs = visit(obj.expr, param);
    assert (lhs instanceof RangeType);
    Range lr = ((RangeType) lhs).range;
    BigInteger low = BigInteger.ZERO.subtract(lr.high);
    BigInteger high = BigInteger.ZERO.subtract(lr.low);
    return kbi.getRangeType(new Range(low, high));
  }

  @Override
  protected Type visitArrayValue(ArrayValue obj, Void param) {
    Iterator<Expression> itr = obj.value.iterator();
    assert (itr.hasNext());
    Range cont = ((RangeType) visit(itr.next(), param)).range;
    while (itr.hasNext()) {
      Range ntype = ((RangeType) visit(itr.next(), param)).range;
      cont = Range.grow(cont, ntype);
    }

    RangeType et = kbi.getRangeType(cont);

    return new ArrayType(BigInteger.valueOf(obj.value.size()), new SimpleRef<Type>(ElementInfo.NO, et));
  }

  @Override
  protected Type visitStringValue(StringValue obj, Void param) {
    return kbi.getStringType();
  }
}
