package evl.traverser.typecheck.specific;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import util.NumberSet;
import util.NumberSetOperator;
import util.Range;

import common.ElementInfo;

import error.ErrorType;
import error.RError;
import evl.Evl;
import evl.NullTraverser;
import evl.expression.ArrayValue;
import evl.expression.BoolValue;
import evl.expression.Expression;
import evl.expression.Number;
import evl.expression.RangeValue;
import evl.expression.StringValue;
import evl.expression.binop.And;
import evl.expression.binop.BinaryExp;
import evl.expression.binop.Div;
import evl.expression.binop.Equal;
import evl.expression.binop.Greater;
import evl.expression.binop.Greaterequal;
import evl.expression.binop.Less;
import evl.expression.binop.Lessequal;
import evl.expression.binop.Minus;
import evl.expression.binop.Mod;
import evl.expression.binop.Mul;
import evl.expression.binop.Notequal;
import evl.expression.binop.Or;
import evl.expression.binop.Plus;
import evl.expression.binop.Shl;
import evl.expression.binop.Shr;
import evl.expression.reference.Reference;
import evl.expression.unop.Not;
import evl.expression.unop.Uminus;
import evl.knowledge.KnowBaseItem;
import evl.knowledge.KnowledgeBase;
import evl.traverser.typecheck.BiggerType;
import evl.type.Type;
import evl.type.TypeRef;
import evl.type.base.ArrayType;
import evl.type.base.BooleanType;
import evl.type.base.EnumType;
import evl.type.base.NumSet;
import evl.variable.SsaVariable;
import evl.variable.Variable;

public class ExpressionTypeChecker extends NullTraverser<Type, Void> {

  private KnowledgeBase kb;
  private KnowBaseItem kbi;
  private Map<SsaVariable, NumSet> map;

  public ExpressionTypeChecker(KnowledgeBase kb, Map<SsaVariable, NumSet> map) {
    super();
    this.map = map;
    this.kb = kb;
    kbi = kb.getEntry(KnowBaseItem.class);
  }

  static public Type process(Expression ast, KnowledgeBase kb) {
    ExpressionTypeChecker adder = new ExpressionTypeChecker(kb, new HashMap<SsaVariable, NumSet>());
    return adder.traverse(ast, null);
  }

  private void checkPositive(String op, NumSet lhs, NumSet rhs) {
    checkPositive(op, lhs);
    checkPositive(op, rhs);
  }

  private void checkPositive(String op, NumSet range) {
    if (range.getNumbers().getLow().compareTo(BigInteger.ZERO) < 0) {
      RError.err(ErrorType.Error, range.getInfo(), op + " only allowed for positive types");
    }
  }

  private NumSet getRange(Expression expr, Void param) {
    Type lhs = visit(expr, param);
    if (!(lhs instanceof NumSet)) {
      RError.err(ErrorType.Fatal, expr.getInfo(), "Expected range type");
    }
    NumSet lt = (NumSet) lhs;
    return lt;
  }

  private BigInteger makeOnes(int bits) {
    BigInteger ret = BigInteger.ZERO;
    for (int i = 0; i < bits; i++) {
      ret = ret.shiftLeft(1);
      ret = ret.or(BigInteger.ONE);
    }
    return ret;
  }

  private int getAsInt(BigInteger value, String text) {
    if (value.compareTo(BigInteger.valueOf(Integer.MAX_VALUE)) > 0) {
      RError.err(ErrorType.Error, "value to big, needs to be smaller than " + Integer.MAX_VALUE + " in " + text);
    }
    if (value.compareTo(BigInteger.valueOf(Integer.MIN_VALUE)) < 0) {
      RError.err(ErrorType.Error, "value to small, needs to be bigger than " + Integer.MIN_VALUE + " in " + text);
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

  /**
   * 
   * @param ast
   * @param map
   *          Constrainted type of variable
   * @param kb
   * @return
   */
  static public Type process(Expression ast, Map<SsaVariable, NumSet> map, KnowledgeBase kb) {
    ExpressionTypeChecker adder = new ExpressionTypeChecker(kb, map);
    return adder.traverse(ast, null);
  }

  @Override
  protected Type visitDefault(Evl obj, Void param) {
    throw new RuntimeException("not yet implemented: " + obj.getClass().getCanonicalName());
  }

  private static Variable getDerefVar(Expression left) {
    // throw new RuntimeException("not used");
    if (left instanceof Reference) {
      Reference ref = (Reference) left;
      if (ref.getOffset().isEmpty()) {
        if (ref.getLink() instanceof Variable) {
          return (Variable) ref.getLink();
        }
      }
    }
    return null;
  }

  @Override
  protected Type visitReference(Reference obj, Void param) {
    Variable rv = getDerefVar(obj);
    if ((rv != null) && map.containsKey(rv)) {
      return map.get(rv);
    } else {
      return RefTypeChecker.process(obj, kb);
    }
  }

  @Override
  protected Type visitNot(Not obj, Void param) {
    Type type = visit(obj.getExpr(), param);
    if (type instanceof EnumType) {
      RError.err(ErrorType.Error, obj.getInfo(), "operation not possible on enumerator");
      return null;
    }

    if (!(type instanceof BooleanType)) {
      RError.err(ErrorType.Error, obj.getInfo(), "Need boolean type for not, got: " + type.getName());
      return null;
    }
    return type;
  }

  @Override
  protected Type visitUminus(Uminus obj, Void param) {
    Type type = visit(obj.getExpr(), param);
    if (type instanceof EnumType) {
      RError.err(ErrorType.Error, obj.getInfo(), "operation not possible on enumerator");
      return null;
    }

    if (!(type instanceof NumSet)) {
      RError.err(ErrorType.Error, obj.getInfo(), "Need ordinal type for minus, got: " + type.getName());
      return null;
    }

    NumberSet ret = NumberSet.execOp(new NumberSetOperator() {
      @Override
      public Range op(Range left, Range right) {
        BigInteger low = left.getLow().subtract(right.getHigh());
        BigInteger high = left.getHigh().subtract(right.getLow());
        return new Range(low, high);
      }
    }, new NumberSet(BigInteger.ZERO, BigInteger.ZERO), ((NumSet) type).getNumbers());

    return kbi.getNumsetType(ret.getRanges());
  }

  private void testForSame(Type lhs, Type rhs, BinaryExp obj) {
    if (lhs.getClass() != rhs.getClass()) { // TODO make it better
      RError.err(ErrorType.Error, obj.getInfo(), "Incompatible types: " + lhs.getName() + " <-> " + rhs.getName());
    }
  }

  @Override
  protected Type visitEqual(Equal obj, Void param) {
    Type lhs = visit(obj.getLeft(), param);
    Type rhs = visit(obj.getRight(), param);
    testForSame(lhs, rhs, obj);
    return kbi.getBooleanType();
  }

  @Override
  protected Type visitNotequal(Notequal obj, Void param) {
    Type lhs = visit(obj.getLeft(), param);
    Type rhs = visit(obj.getRight(), param);
    testForSame(lhs, rhs, obj);
    return kbi.getBooleanType();
  }

  @Override
  protected Type visitGreater(Greater obj, Void param) {
    getRange(obj.getLeft(), param);
    getRange(obj.getRight(), param);
    return kbi.getBooleanType();
  }

  @Override
  protected Type visitGreaterequal(Greaterequal obj, Void param) {
    getRange(obj.getLeft(), param);
    getRange(obj.getRight(), param);
    return kbi.getBooleanType();
  }

  @Override
  protected Type visitLess(Less obj, Void param) {
    getRange(obj.getLeft(), param);
    getRange(obj.getRight(), param);
    return kbi.getBooleanType();
  }

  @Override
  protected Type visitLessequal(Lessequal obj, Void param) {
    getRange(obj.getLeft(), param);
    getRange(obj.getRight(), param);
    return kbi.getBooleanType();
  }

  @Override
  protected Type visitAnd(And obj, Void param) {
    Type lhst = visit(obj.getLeft(), param);
    Type rhst = visit(obj.getRight(), param);

    if (lhst instanceof NumSet) {
      if (!(rhst instanceof NumSet)) {
        RError.err(ErrorType.Fatal, rhst.getInfo(), "Expected range type");
      }
      NumSet lhs = getRange(obj.getLeft(), param);
      NumSet rhs = getRange(obj.getRight(), param);
      checkPositive("and", lhs, rhs);
      BigInteger high = lhs.getNumbers().getHigh().min(rhs.getNumbers().getHigh()); // TODO ok?
      return kbi.getRangeType(BigInteger.ZERO, high);
    } else if (lhst instanceof BooleanType) {
      return kbi.getBooleanType();
    } else {
      RError.err(ErrorType.Error, lhst.getInfo(), "Expected range or boolean type");
      return null;
    }
  }

  @Override
  protected Type visitDiv(Div obj, Void param) {
    NumSet lhs = getRange(obj.getLeft(), param);
    NumSet rhs = getRange(obj.getRight(), param);
    if (rhs.getNumbers().contains(BigInteger.ZERO)) {
      RError.err(ErrorType.Error, rhs.getInfo(), "division by 0");
    }
    if ((lhs.getNumbers().getLow().compareTo(BigInteger.ZERO) < 0) || (rhs.getNumbers().getLow().compareTo(BigInteger.ZERO) < 0)) {
      RError.err(ErrorType.Error, lhs.getInfo(), "sorry, I am too lazy to check for negative numbers");
    }

    NumberSet ret = NumberSet.execOp(new NumberSetOperator() {
      @Override
      public Range op(Range lhs, Range rhs) {
        BigInteger low = lhs.getLow().divide(rhs.getHigh());
        BigInteger high = lhs.getHigh().divide(rhs.getLow());
        return new Range(low, high);
      }
    }, lhs.getNumbers(), rhs.getNumbers());

    return kbi.getNumsetType(ret.getRanges());
  }

  @Override
  protected Type visitMinus(Minus obj, Void param) {
    NumSet lhs = getRange(obj.getLeft(), param);
    NumSet rhs = getRange(obj.getRight(), param);

    NumberSet ret = NumberSet.execOp(new NumberSetOperator() {
      @Override
      public Range op(Range left, Range right) {
        BigInteger low = left.getLow().subtract(right.getHigh());
        BigInteger high = left.getHigh().subtract(right.getLow());
        return new Range(low, high);
      }
    }, lhs.getNumbers(), rhs.getNumbers());

    return kbi.getNumsetType(ret.getRanges());
  }

  @Override
  protected Type visitMod(Mod obj, Void param) {
    NumSet lhs = getRange(obj.getLeft(), param);
    NumSet rhs = getRange(obj.getRight(), param);
    checkPositive("mod", lhs); // TODO implement mod correctly (and not with 'urem' instruction) and remove this check
    checkPositive("mod", rhs);

    NumberSet ret = NumberSet.execOp(new NumberSetOperator() {
      @Override
      public Range op(Range left, Range right) {
        BigInteger low = BigInteger.ZERO;
        BigInteger high = left.getHigh().min(right.getHigh().subtract(BigInteger.ONE));
        return new Range(low, high);
      }
    }, lhs.getNumbers(), rhs.getNumbers());

    return kbi.getNumsetType(ret.getRanges());
  }

  @Override
  protected Type visitMul(Mul obj, Void param) {
    NumSet lhs = getRange(obj.getLeft(), param);
    NumSet rhs = getRange(obj.getRight(), param);
    // FIXME correct when values are negative?

    NumberSet ret = NumberSet.execOp(new NumberSetOperator() {
      @Override
      public Range op(Range left, Range right) {
        BigInteger low = left.getLow().multiply(right.getLow());
        BigInteger high = left.getHigh().multiply(right.getHigh());
        return new Range(low, high);
      }
    }, lhs.getNumbers(), rhs.getNumbers());

    return kbi.getNumsetType(ret.getRanges());
  }

  @Override
  protected Type visitOr(Or obj, Void param) {
    NumSet lhs = getRange(obj.getLeft(), param);
    NumSet rhs = getRange(obj.getRight(), param);
    checkPositive("or", lhs, rhs);

    NumberSet ret = NumberSet.execOp(new NumberSetOperator() {
      @Override
      public Range op(Range lhs, Range rhs) {
        BigInteger bigger = lhs.getHigh().max(rhs.getHigh());
        BigInteger smaller = lhs.getHigh().min(rhs.getHigh());

        int bits = bitCount(smaller);
        BigInteger ones = makeOnes(bits);
        BigInteger high = bigger.or(ones);
        BigInteger low = lhs.getLow().max(rhs.getLow());

        return new Range(low, high);
      }
    }, lhs.getNumbers(), rhs.getNumbers());

    return kbi.getNumsetType(ret.getRanges());
  }

  @Override
  protected Type visitPlus(Plus obj, Void param) {
    NumSet lhs = getRange(obj.getLeft(), param);
    NumSet rhs = getRange(obj.getRight(), param);

    NumberSet ret = NumberSet.execOp(new NumberSetOperator() {
      @Override
      public Range op(Range left, Range right) {
        BigInteger low = left.getLow().add(right.getLow());
        BigInteger high = left.getHigh().add(right.getHigh());
        return new Range(low, high);
      }
    }, lhs.getNumbers(), rhs.getNumbers());

    return kbi.getNumsetType(ret.getRanges());
  }

  @Override
  protected Type visitShl(Shl obj, Void param) {
    NumSet lhs = getRange(obj.getLeft(), param);
    NumSet rhs = getRange(obj.getRight(), param);
    checkPositive("shl", lhs, rhs);

    NumberSet ret = NumberSet.execOp(new NumberSetOperator() {
      @Override
      public Range op(Range lhs, Range rhs) {
        BigInteger high = lhs.getHigh().shiftLeft(getAsInt(rhs.getHigh(), "shl"));
        BigInteger low = lhs.getLow().shiftLeft(getAsInt(rhs.getLow(), "shl"));
        return new Range(low, high);
      }
    }, lhs.getNumbers(), rhs.getNumbers());

    return kbi.getNumsetType(ret.getRanges());
  }

  @Override
  protected Type visitShr(Shr obj, Void param) {
    NumSet lhs = getRange(obj.getLeft(), param);
    NumSet rhs = getRange(obj.getRight(), param);
    checkPositive("shr", lhs, rhs);

    NumberSet ret = NumberSet.execOp(new NumberSetOperator() {
      @Override
      public Range op(Range lhs, Range rhs) {
        BigInteger high = lhs.getHigh().shiftRight(getAsInt(rhs.getLow(), "shr"));
        BigInteger low = lhs.getLow().shiftRight(getAsInt(rhs.getHigh(), "shr"));
        return new Range(low, high);
      }
    }, lhs.getNumbers(), rhs.getNumbers());

    return kbi.getNumsetType(ret.getRanges());
  }

  @Override
  protected Type visitNumber(Number obj, Void param) {
    BigInteger value = obj.getValue();
    return kbi.getRangeType(value, value);
  }

  @Override
  protected Type visitRangeValue(RangeValue obj, Void param) {
    return kbi.getRangeType(obj.getLow(), obj.getHigh());
  }

  @Override
  protected Type visitArrayValue(ArrayValue obj, Void param) {
    Iterator<Expression> itr = obj.getValue().iterator();
    assert (itr.hasNext());
    Type cont = visit(itr.next(), param);
    while (itr.hasNext()) {
      Type ntype = visit(itr.next(), param);
      cont = BiggerType.get(cont, ntype, obj.getInfo(), kb);
    }

    return new ArrayType(BigInteger.valueOf(obj.getValue().size()), new TypeRef(new ElementInfo(), cont));
  }

  @Override
  protected Type visitStringValue(StringValue obj, Void param) {
    return kbi.getStringType();
  }

  @Override
  protected Type visitBoolValue(BoolValue obj, Void param) {
    return kbi.getBooleanType();
  }

  @Override
  protected Type visitTypeRef(TypeRef obj, Void param) {
    return obj.getRef();
  }
}
