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

package ast.knowledge;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import ast.Designator;
import ast.data.Ast;
import ast.data.AstList;
import ast.data.Range;
import ast.data.component.ComponentReference;
import ast.data.component.Component;
import ast.data.component.composition.ComponentUse;
import ast.data.component.composition.Direction;
import ast.data.component.hfsm.StateRef;
import ast.data.expression.Expression;
import ast.data.expression.ReferenceExpression;
import ast.data.expression.TypeCast;
import ast.data.expression.binop.And;
import ast.data.expression.binop.BitAnd;
import ast.data.expression.binop.BitOr;
import ast.data.expression.binop.BitXor;
import ast.data.expression.binop.Division;
import ast.data.expression.binop.LogicAnd;
import ast.data.expression.binop.LogicOr;
import ast.data.expression.binop.Minus;
import ast.data.expression.binop.Modulo;
import ast.data.expression.binop.Multiplication;
import ast.data.expression.binop.Or;
import ast.data.expression.binop.Plus;
import ast.data.expression.binop.Relation;
import ast.data.expression.binop.Shl;
import ast.data.expression.binop.Shr;
import ast.data.expression.unop.BitNot;
import ast.data.expression.unop.LogicNot;
import ast.data.expression.unop.Uminus;
import ast.data.expression.value.AnyValue;
import ast.data.expression.value.ArrayValue;
import ast.data.expression.value.BooleanValue;
import ast.data.expression.value.NumberValue;
import ast.data.expression.value.RecordValue;
import ast.data.expression.value.StringValue;
import ast.data.expression.value.TupleValue;
import ast.data.expression.value.UnionValue;
import ast.data.expression.value.UnsafeUnionValue;
import ast.data.function.FunctionReference;
import ast.data.function.Function;
import ast.data.function.InterfaceFunction;
import ast.data.function.ret.FuncReturnNone;
import ast.data.function.ret.FuncReturnTuple;
import ast.data.function.ret.FunctionReturnType;
import ast.data.reference.RefItem;
import ast.data.reference.LinkedReferenceWithOffset_Implementation;
import ast.data.type.Type;
import ast.data.type.TypeRefFactory;
import ast.data.type.TypeReference;
import ast.data.type.base.ArrayTypeFactory;
import ast.data.type.base.BooleanType;
import ast.data.type.base.EnumElement;
import ast.data.type.base.EnumType;
import ast.data.type.base.FunctionType;
import ast.data.type.base.RangeType;
import ast.data.type.base.TupleType;
import ast.data.type.composed.NamedElement;
import ast.data.type.composed.RecordType;
import ast.data.type.special.ComponentType;
import ast.data.variable.FunctionVariable;
import ast.data.variable.Variable;
import ast.dispatcher.NullDispatcher;
import ast.dispatcher.other.RefTypeGetter;
import ast.meta.MetaList;
import ast.pass.check.type.ExpressionTypecheck;
import ast.repository.manipulator.TypeRepo;
import error.ErrorType;
import error.RError;

public class KnowType extends KnowledgeEntry {
  private KnowTypeTraverser ktt;

  @Override
  public void init(KnowledgeBase base) {
    ktt = new KnowTypeTraverser(this, base);
  }

  public Type get(Ast ast) {
    return ktt.traverse(ast, null);
  }

}

class KnowTypeTraverser extends NullDispatcher<Type, Void> {
  private final Map<Ast, Type> cache = new HashMap<Ast, Type>();
  final private TypeRepo kbi;
  final private RefTypeGetter rtg;
  final private KnowParent kp;

  public KnowTypeTraverser(KnowType kt, KnowledgeBase kb) {
    super();
    kbi = new TypeRepo(kb);
    kp = kb.getEntry(KnowParent.class);
    rtg = new RefTypeGetter(kt, kb);
  }

  private void checkPositive(MetaList info, String op, Range lhs, Range rhs) {
    checkPositive(info, op, lhs);
    checkPositive(info, op, rhs);
  }

  private void checkPositive(MetaList info, String op, Range range) {
    if (range.low.compareTo(BigInteger.ZERO) < 0) {
      RError.err(ErrorType.Error, op + " only allowed for positive types", info);
    }
  }

  @Override
  protected Type visitDefault(Ast obj, Void param) {
    throw new RuntimeException("not yet implemented: " + obj.getClass().getCanonicalName());
  }

  @Override
  protected Type visit(Ast obj, Void param) {
    Type type = cache.get(obj);
    if (type == null) {
      type = super.visit(obj, param);
      assert (type != null);
      cache.put(obj, type);
    }
    return type;
  }

  @Override
  protected Type visitTypeRef(TypeReference obj, Void param) {
    return visit(obj.ref, param);
  }

  @Override
  protected Type visitStateRef(StateRef obj, Void param) {
    return visit(obj.ref, param);
  }

  @Override
  protected Type visitCompRef(ComponentReference obj, Void param) {
    return visit(obj.ref, param);
  }

  @Override
  protected Type visitFuncRef(FunctionReference obj, Void param) {
    return visit(obj.ref, param);
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
    AstList<TypeReference> arg = new AstList<TypeReference>();
    for (FunctionVariable var : obj.param) {
      arg.add(TypeRefFactory.create(visit(var.type, null)));
    }
    return new FunctionType(obj.metadata(), obj.getName(), arg, TypeRefFactory.create(visit(obj.ret, param)));
  }

  @Override
  protected Type visitComponent(Component obj, Void param) {
    ComponentType ct = new ComponentType(obj.metadata(), Designator.NAME_SEP + "T" + Designator.NAME_SEP + obj.getName());
    makeFuncTypes(ct.input, obj.getIface(Direction.in));
    makeFuncTypes(ct.output, obj.getIface(Direction.out));
    return ct;
  }

  private void makeFuncTypes(AstList<NamedElement> flist, AstList<InterfaceFunction> astList) {
    for (InterfaceFunction itr : astList) {
      NamedElement ne = new NamedElement(itr.metadata(), itr.getName(), TypeRefFactory.create(itr.metadata(), visit(itr, null)));
      flist.add(ne);
    }
  }

  @Override
  protected Type visitAnyValue(AnyValue obj, Void param) {
    return kbi.getAnyType();
  }

  @Override
  protected Type visitBoolValue(BooleanValue obj, Void param) {
    return kbi.getBooleanType();
  }

  @Override
  protected Type visitNumber(NumberValue obj, Void param) {
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
      AstList<TypeReference> elem = new AstList<TypeReference>();
      for (Expression expr : obj.value) {
        TypeReference ref = TypeRefFactory.create(expr.metadata(), visit(expr, null));
        elem.add(ref);
      }
      return new TupleType(obj.metadata(), "", elem);
    }
  }

  @Override
  protected Type visitUnionValue(UnionValue obj, Void param) {
    throw new RuntimeException("not yet implemented");
  }

  @Override
  protected Type visitEnumElement(EnumElement obj, Void param) {
    Ast parent = kp.get(obj);
    assert (parent instanceof EnumType);
    return (EnumType) parent;
  }

  @Override
  protected Type visitRefExpr(ReferenceExpression obj, Void param) {
    return visit(obj.reference, param);
  }

  @Override
  protected Type visitReference(LinkedReferenceWithOffset_Implementation obj, Void param) {
    Type base = visit(obj.getLink(), param);
    for (RefItem itm : obj.getOffset()) {
      base = rtg.traverse(itm, base);
    }
    return base;
  }

  @Override
  protected Type visitVariable(Variable obj, Void param) {
    return visit(obj.type, param);
  }

  @Override
  protected Type visitCompUse(ComponentUse obj, Void param) {
    return visit(obj.compRef, param);
  }

  @Override
  protected Type visitFuncReturnTuple(FuncReturnTuple obj, Void param) {
    AstList<NamedElement> types = new AstList<NamedElement>();
    for (FunctionVariable var : obj.param) {
      types.add(new NamedElement(var.getName(), var.type));
    }
    return new RecordType(obj.metadata(), "", types);
  }

  @Override
  protected Type visitFuncReturnType(FunctionReturnType obj, Void param) {
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

    BigInteger high = lr.high.shiftRight(ExpressionTypecheck.getAsInt(rr.low, "shl"));
    BigInteger low = lr.low.shiftRight(ExpressionTypecheck.getAsInt(rr.high, "shl"));

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
    return bitOr(obj.metadata(), lhs, rhs);
  }

  @Override
  protected Type visitOr(Or obj, Void param) {
    Type lhst = visit(obj.left, param);
    Type rhst = visit(obj.right, param);

    if (lhst instanceof RangeType) {
      Range lhs = ((RangeType) lhst).range;
      Range rhs = ((RangeType) rhst).range;
      return bitOr(obj.metadata(), lhs, rhs);
    } else if (lhst instanceof BooleanType) {
      assert (rhst instanceof BooleanType);
      return lhst;
    } else {
      RError.err(ErrorType.Error, "Expected range or boolean type", lhst.metadata());
      return null;
    }
  }

  private Type bitOr(MetaList info, Range lhs, Range rhs) {
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

      checkPositive(obj.metadata(), "xor", lhs, rhs);

      BigInteger bigger = lhs.high.max(rhs.high);

      int bits = ExpressionTypecheck.bitCount(bigger);
      BigInteger ones = ExpressionTypecheck.makeOnes(bits);
      BigInteger high = bigger.or(ones);

      return kbi.getRangeType(new Range(BigInteger.ZERO, high));
    } else if (lhst instanceof BooleanType) {
      assert (rhst instanceof BooleanType);
      return lhst;
    } else {
      RError.err(ErrorType.Error, "Expected range or boolean type", lhst.metadata());
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
    return bitAnd(obj.metadata(), lhs, rhs);
  }

  private Type bitAnd(MetaList info, Range lhs, Range rhs) {
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
      return bitAnd(obj.metadata(), lhs, rhs);
    } else if (lhst instanceof BooleanType) {
      assert (rhst instanceof BooleanType);
      return lhst;
    } else {
      RError.err(ErrorType.Error, "Expected range or boolean type", lhst.metadata());
      return null;
    }
  }

  @Override
  protected Type visitMod(Modulo obj, Void param) {
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
  protected Type visitMul(Multiplication obj, Void param) {
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
  protected Type visitDiv(Division obj, Void param) {
    Type lhs = visit(obj.left, param);
    Type rhs = visit(obj.right, param);

    assert (lhs instanceof RangeType);
    assert (rhs instanceof RangeType);

    Range lr = ((RangeType) lhs).range;
    Range rr = ((RangeType) rhs).range;

    if ((lr.low.compareTo(BigInteger.ZERO) < 0) || (rr.low.compareTo(BigInteger.ZERO) < 0)) {
      RError.err(ErrorType.Fatal, "sorry, I am too lazy to check for negative numbers", obj.metadata());
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
    Type type = visit(obj.expression, param);
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
    Type lhs = visit(obj.expression, param);
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

    return ArrayTypeFactory.create(obj.value.size(), et);
  }

  @Override
  protected Type visitStringValue(StringValue obj, Void param) {
    return kbi.getStringType();
  }
}
