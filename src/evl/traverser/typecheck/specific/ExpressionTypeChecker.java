package evl.traverser.typecheck.specific;

import java.util.Iterator;

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
import evl.traverser.typecheck.BiggerType;
import evl.traverser.typecheck.LeftIsContainerOfRightTest;
import evl.type.Type;
import evl.type.base.Array;
import evl.type.base.BooleanType;
import evl.type.base.EnumType;
import evl.type.base.Unsigned;

public class ExpressionTypeChecker extends NullTraverser<Type, Void> {
  private KnowledgeBase kb;
  private KnowBaseItem kbi;

  public ExpressionTypeChecker(KnowledgeBase kb) {
    super();
    this.kb = kb;
    kbi = kb.getEntry(KnowBaseItem.class);
  }

  static public Type process(Expression ast, KnowledgeBase kb) {
    ExpressionTypeChecker adder = new ExpressionTypeChecker(kb);
    return adder.traverse(ast, null);
  }

  private boolean isOrd(Type type) {
    return type instanceof Unsigned; // TODO extend to all ord types
  }

  @Override
  protected Type visitDefault(Evl obj, Void param) {
    throw new RuntimeException("not yet implemented: " + obj.getClass().getCanonicalName());
  }

  @Override
  protected Type visitReference(Reference obj, Void param) {
    return RefTypeChecker.process(obj, kb);
  }

  @Override
  protected Type visitUnaryExpression(UnaryExpression obj, Void param) {
    Type expt = visit(obj.getExpr(), param);
    if (expt instanceof EnumType) {
      RError.err(ErrorType.Error, obj.getInfo(), "operation not possible on enumerator");
      return null;
    }

    switch (obj.getOp()) {
    case MINUS:
      if (!isOrd(expt)) {
        RError.err(ErrorType.Error, obj.getInfo(), "Need ordinal type for minus, got: " + expt.getName());
        return null;
      }
      break;
    case NOT:
      if (!(expt instanceof BooleanType)) {
        RError.err(ErrorType.Error, obj.getInfo(), "Need boolean type for not, got: " + expt.getName());
        return null;
      }
      break;
    default:
      RError.err(ErrorType.Fatal, "Unhandled relation operator: " + obj.getOp());
      return null;
    }
    return expt;
  }

  @Override
  protected Type visitRelation(Relation obj, Void sym) {
    Type lhs = visit(obj.getLeft(), sym);
    Type rhs = visit(obj.getRight(), sym);

    if (!(LeftIsContainerOfRightTest.process(lhs, rhs, kb) || LeftIsContainerOfRightTest.process(rhs, lhs, kb))) {
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
      if (!isOrd(lhs)) {
        RError.err(ErrorType.Error, lhs.getInfo(), "Expected ordinal type");
      }
      if (!isOrd(rhs)) {
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
    Type lhs = visit(obj.getLeft(), sym);
    Type rhs = visit(obj.getRight(), sym);
    switch (obj.getOp()) {
    case DIV:
    case MINUS:
    case MUL:
    case OR:
    case PLUS:
    case SHL:
    case SHR: {
      return BiggerType.get(lhs, rhs, obj.getInfo(), kb);
    }
    case MOD:
    case AND: {
      return BiggerType.getSmaller(lhs, rhs, obj.getInfo(), kb);
    }
    default: {
      RError.err(ErrorType.Fatal, obj.getInfo(), "Unhandled operation: " + obj.getOp());
      return null;
    }
    }
  }

  static private int getBits(int value) {
    int bit = 0;
    while (value != 0) {
      value >>= 1;
      bit++;
    }
    return bit;
  }

  @Override
  protected Type visitNumber(Number obj, Void param) {
    if (obj.getValue() >= 0) {
      int bits = getBits(obj.getValue());
      return kbi.getUnsignedType(bits);
    } else {
      throw new RuntimeException("not yet implemented");
    }
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
