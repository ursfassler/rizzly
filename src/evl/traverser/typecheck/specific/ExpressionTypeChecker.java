package evl.traverser.typecheck.specific;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import common.ElementInfo;

import error.ErrorType;
import error.RError;
import evl.Evl;
import evl.NullTraverser;
import evl.expression.ArithmeticOp;
import evl.expression.ArrayValue;
import evl.expression.BoolValue;
import evl.expression.Expression;
import evl.expression.Number;
import evl.expression.Relation;
import evl.expression.StringValue;
import evl.expression.UnaryExpression;
import evl.expression.reference.Reference;
import evl.knowledge.KnowBaseItem;
import evl.knowledge.KnowledgeBase;
import evl.traverser.range.RangeGetter;
import evl.traverser.typecheck.BiggerType;
import evl.type.Type;
import evl.type.base.Array;
import evl.type.base.BooleanType;
import evl.type.base.EnumType;
import evl.type.base.Range;
import evl.variable.Variable;

public class ExpressionTypeChecker extends NullTraverser<Type, Void> {
  private KnowledgeBase kb;
  private KnowBaseItem kbi;
  private Map<Variable, Range> map;

  public ExpressionTypeChecker(KnowledgeBase kb, Map<Variable, Range> map) {
    super();
    this.map = map;
    this.kb = kb;
    kbi = kb.getEntry(KnowBaseItem.class);
  }

  static public Type process(Expression ast, KnowledgeBase kb) {
    ExpressionTypeChecker adder = new ExpressionTypeChecker(kb, new HashMap<Variable, Range>());
    return adder.traverse(ast, null);
  }

  /**
   *
   * @param ast
   * @param map
   *          Constrainted type of variable
   * @param kb
   * @return
   */
  static public Type process(Expression ast, Map<Variable, Range> map, KnowledgeBase kb) {
    ExpressionTypeChecker adder = new ExpressionTypeChecker(kb, map);
    return adder.traverse(ast, null);
  }

  @Override
  protected Type visitDefault(Evl obj, Void param) {
    throw new RuntimeException("not yet implemented: " + obj.getClass().getCanonicalName());
  }

  @Override
  protected Type visitReference(Reference obj, Void param) {
    Variable rv = RangeGetter.getDerefVar(obj);
    if ((rv != null) && map.containsKey(rv)) {
      return map.get(rv);
    } else {
      return RefTypeChecker.process(obj, kb);
    }
  }

  @Override
  protected Type visitUnaryExpression(UnaryExpression obj, Void param) {
    Type type = visit(obj.getExpr(), param);
    if (type instanceof EnumType) {
      RError.err(ErrorType.Error, obj.getInfo(), "operation not possible on enumerator");
      return null;
    }

    switch (obj.getOp()) {
    case MINUS:
      if (!(type instanceof Range)) {
        RError.err(ErrorType.Error, obj.getInfo(), "Need ordinal type for minus, got: " + type.getName());
        return null;
      }
      BigInteger low = ((Range) type).getLow();
      BigInteger high = ((Range) type).getHigh();
      low = BigInteger.ZERO.subtract(low);
      high = BigInteger.ZERO.subtract(high);
      return kbi.getRangeType(high, low);
    case NOT:
      if (!(type instanceof BooleanType)) {
        RError.err(ErrorType.Error, obj.getInfo(), "Need boolean type for not, got: " + type.getName());
        return null;
      }
      return type;
    default:
      RError.err(ErrorType.Fatal, "Unhandled relation operator: " + obj.getOp());
      return null;
    }
  }

  @Override
  protected Type visitRelation(Relation obj, Void sym) {
    Type lhs = visit(obj.getLeft(), sym);
    Type rhs = visit(obj.getRight(), sym);

    // if (!(LeftIsContainerOfRightTest.process(lhs, rhs, kb) || LeftIsContainerOfRightTest.process(rhs, lhs, kb))) {
    if (lhs.getClass() != rhs.getClass()) { // TODO make it better
      RError.err(ErrorType.Error, obj.getInfo(), "Incompatible types: " + lhs.getName() + " <-> " + rhs.getName());
    }

    switch (obj.getOp()) {
    case EQUAL:
    case NOT_EQUAL: {
      break;
    }
    case GREATER:
    case GREATER_EQUEAL:
    case LESS:
    case LESS_EQUAL: {
      if (!(lhs instanceof Range)) {
        RError.err(ErrorType.Error, lhs.getInfo(), "Expected ordinal type");
      }
      if (!(rhs instanceof Range)) {
        RError.err(ErrorType.Error, rhs.getInfo(), "Expected ordinal type");
      }
      break;
    }
    default: {
      RError.err(ErrorType.Fatal, "Unhandled relation operator: " + obj.getOp());
    }
    }
    return kbi.getBooleanType();
  }

  @Override
  protected Type visitArithmeticOp(ArithmeticOp obj, Void sym) {
    Range lhs = (Range) visit(obj.getLeft(), sym);
    Range rhs = (Range) visit(obj.getRight(), sym);

    switch (obj.getOp()) {
    case PLUS: {
      BigInteger low = lhs.getLow().add(rhs.getLow());
      BigInteger high = lhs.getHigh().add(rhs.getHigh());
      return kbi.getRangeType(low, high);
    }
    case MINUS: {
      BigInteger low = lhs.getLow().subtract(rhs.getLow());
      BigInteger high = lhs.getHigh().subtract(rhs.getHigh());
      return kbi.getRangeType(low, high);
    }
    case MUL: { //FIXME correct when values are negative?
      BigInteger low = lhs.getLow().multiply(rhs.getLow());
      BigInteger high = lhs.getHigh().multiply(rhs.getHigh());
      return kbi.getRangeType(low, high);
    }
    case AND: {
      if (lhs.getLow().compareTo(BigInteger.ZERO) < 0) {
        RError.err(ErrorType.Error, lhs.getInfo(), "and only allowed for positive types");
      }
      if (rhs.getLow().compareTo(BigInteger.ZERO) < 0) {
        RError.err(ErrorType.Error, rhs.getInfo(), "and only allowed for positive types");
      }
      BigInteger high = lhs.getHigh().min(rhs.getHigh()); // TODO ok?
      return kbi.getRangeType(BigInteger.ZERO, high);
    }
    case MOD: {
      if (lhs.getLow().compareTo(BigInteger.ZERO) < 0) {
        RError.err(ErrorType.Error, lhs.getInfo(), "mod only allowed for positive types");
      }
      if (rhs.getLow().compareTo(BigInteger.ZERO) < 0) {
        RError.err(ErrorType.Error, rhs.getInfo(), "mod only allowed for positive types");
      }
      BigInteger high = lhs.getHigh().min(rhs.getHigh().subtract(BigInteger.ONE));
      return kbi.getRangeType(BigInteger.ZERO, high);
    }
    case DIV: {
      if ((rhs.getLow().compareTo(BigInteger.ZERO) < 0) && (rhs.getHigh().compareTo(BigInteger.ZERO) > 0)) {
        RError.err(ErrorType.Warning, rhs.getInfo(), "potential division by 0");
      }
      if ((lhs.getLow().compareTo(BigInteger.ZERO) < 0) || (lhs.getHigh().compareTo(BigInteger.ZERO) < 0) || (rhs.getLow().compareTo(BigInteger.ZERO) < 0) || (rhs.getHigh().compareTo(BigInteger.ZERO) < 0)) {
        RError.err(ErrorType.Error, lhs.getInfo(), "sorry, I am too lazy to check for negative numbers");
      }
      BigInteger low = lhs.getLow().divide(rhs.getHigh());
      BigInteger high = lhs.getHigh().divide(rhs.getLow());
      return kbi.getRangeType(low, high);
    }

    case SHR:
    case OR:
    case SHL:
    default: {
      RError.err(ErrorType.Fatal, obj.getInfo(), "Unhandled operation: " + obj.getOp());
      return null;
    }
    }
  }

  @Override
  protected Type visitNumber(Number obj, Void param) {
    BigInteger value = BigInteger.valueOf(obj.getValue());
    return kbi.getRangeType(value, value);
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

    return new Array(obj.getValue().size(), new Reference(new ElementInfo(), cont));
  }

  @Override
  protected Type visitStringValue(StringValue obj, Void param) {
    return kbi.getStringType();
  }

  @Override
  protected Type visitBoolValue(BoolValue obj, Void param) {
    return kbi.getBooleanType();
  }

}