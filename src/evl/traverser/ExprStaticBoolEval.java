package evl.traverser;

import java.util.HashMap;
import java.util.Map;

import util.NumberSet;
import util.Range;
import util.Unsure;
import evl.Evl;
import evl.NullTraverser;
import evl.expression.BoolValue;
import evl.expression.Expression;
import evl.expression.Number;
import evl.expression.RangeValue;
import evl.expression.binop.And;
import evl.expression.binop.Equal;
import evl.expression.binop.Greater;
import evl.expression.binop.Greaterequal;
import evl.expression.binop.Less;
import evl.expression.binop.Lessequal;
import evl.expression.binop.Notequal;
import evl.expression.reference.Reference;
import evl.function.FunctionHeader;
import evl.knowledge.KnowWriter;
import evl.knowledge.KnowledgeBase;
import evl.traverser.typecheck.specific.ExpressionTypeChecker;
import evl.type.Type;
import evl.type.base.BooleanType;
import evl.type.base.NumSet;
import evl.variable.SsaVariable;

public class ExprStaticBoolEval extends NullTraverser<Unsure, Map<Expression, Unsure>> {
  final private ExprStaticRangeEval reval;
  final private KnowWriter kw;
  final private KnowledgeBase kb;

  public ExprStaticBoolEval(KnowledgeBase kb) {
    super();
    kw = kb.getEntry(KnowWriter.class);
    reval = new ExprStaticRangeEval(kb);
    this.kb = kb;
  }

  public static Map<Expression, Unsure> eval(Expression expr, KnowledgeBase kb) {
    ExprStaticBoolEval eval = new ExprStaticBoolEval(kb);
    Map<Expression, Unsure> map = new HashMap<Expression, Unsure>();
    eval.traverse(expr, map);
    return map;
  }

  private boolean isBool(Expression expr) {
    Type type = ExpressionTypeChecker.process(expr, kb); // TODO replace with a type getter function (not so bloated)
    return type instanceof BooleanType;
  }

  private boolean isRange(Expression expr) {
    Type type = ExpressionTypeChecker.process(expr, kb); // TODO replace with a type getter function (not so bloated)
    return type instanceof NumSet;
  }

  @Override
  protected Unsure visitDefault(Evl obj, Map<Expression, Unsure> param) {
    throw new RuntimeException("not yet implemented: " + obj.getClass().getCanonicalName());
  }

  @Override
  protected Unsure visitExpression(Expression obj, Map<Expression, Unsure> param) {
    Unsure ret = super.visitExpression(obj, param);
    assert (ret != null);
    param.put(obj, ret);
    return ret;
  }

  @Override
  protected Unsure visitReference(Reference obj, Map<Expression, Unsure> param) {
    if (obj.getLink() instanceof SsaVariable) {
      assert (obj.getOffset().isEmpty());
      Expression def = kw.get((SsaVariable) obj.getLink());
      return visit(def, param);
    } else if (obj.getLink() instanceof FunctionHeader) {
      return Unsure.DontKnow;
    } else {
      throw new RuntimeException("not yet implemented: " + obj.getLink().getClass().getCanonicalName());
    }
  }

  @Override
  protected Unsure visitAnd(And obj, Map<Expression, Unsure> param) {
    if (isBool(obj.getLeft())) {
      assert (isBool(obj.getRight()));
      Unsure left = visit(obj.getLeft(), param);
      Unsure right = visit(obj.getRight(), param);
      return Unsure.and(left, right);
    } else {
      assert (!isBool(obj.getRight()));
      return Unsure.DontKnow;
    }
  }

  @Override
  protected Unsure visitEqual(Equal obj, Map<Expression, Unsure> param) {
    if (isBool(obj.getLeft())) {
      assert (isBool(obj.getRight()));
      Unsure left = visit(obj.getLeft(), param);
      Unsure right = visit(obj.getRight(), param);
      return Unsure.equal(left, right);
    } else if (isRange(obj.getLeft())) {
      assert (isRange(obj.getRight()));
      RangeValue left = reval.traverse(obj.getLeft(), null);
      RangeValue right = reval.traverse(obj.getRight(), null);
      if (left.getValues().equals(right.getValues())) {
        return Unsure.True;
      } else if (!NumberSet.intersection(left.getValues(), right.getValues()).isEmpty()) {
        return Unsure.DontKnow; // they overlap
      } else {
        return Unsure.False;
      }
    } else {
      return Unsure.DontKnow; // TODO make it smarter
    }
  }

  @Override
  protected Unsure visitNotequal(Notequal obj, Map<Expression, Unsure> param) {
    if (isBool(obj.getLeft())) {
      assert (isBool(obj.getRight()));
      Unsure left = visit(obj.getLeft(), param);
      Unsure right = visit(obj.getRight(), param);
      return Unsure.notequal(left, right);
    } else if (isRange(obj.getLeft())) {
      assert (isRange(obj.getRight()));
      RangeValue left = reval.traverse(obj.getLeft(), null);
      RangeValue right = reval.traverse(obj.getRight(), null);
      if (left.getValues().equals(right.getValues())) {
        return Unsure.False;
      } else if (!NumberSet.intersection(left.getValues(), right.getValues()).isEmpty()) {
        return Unsure.DontKnow; // they overlap
      } else {
        return Unsure.True;
      }
    } else {
      return Unsure.DontKnow; // TODO make it smarter
    }
  }

  @Override
  protected Unsure visitBoolValue(BoolValue obj, Map<Expression, Unsure> param) {
    return Unsure.fromBool(obj.isValue());
  }

  private Unsure evalGreater(RangeValue left, RangeValue right) {
    if (left.getValues().getHigh().compareTo(right.getValues().getLow()) <= 0) {
      return Unsure.False; // TODO check borders
    }
    if (left.getValues().getLow().compareTo(right.getValues().getHigh()) > 0) {
      return Unsure.True; // TODO check borders
    }
    return Unsure.DontKnow;
  }

  private Unsure evalGreaterEqual(RangeValue left, RangeValue right) {
    if (left.getValues().getHigh().compareTo(right.getValues().getLow()) < 0) {
      return Unsure.False; // TODO check borders
    }
    if (left.getValues().getLow().compareTo(right.getValues().getHigh()) >= 0) {
      return Unsure.True; // TODO check borders
    }
    return Unsure.DontKnow;
  }

  @Override
  protected Unsure visitGreater(Greater obj, Map<Expression, Unsure> param) {
    RangeValue left = reval.traverse(obj.getLeft(), null);
    RangeValue right = reval.traverse(obj.getRight(), null);
    return evalGreater(left, right);
  }

  @Override
  protected Unsure visitGreaterequal(Greaterequal obj, Map<Expression, Unsure> param) {
    RangeValue left = reval.traverse(obj.getLeft(), null);
    RangeValue right = reval.traverse(obj.getRight(), null);
    return evalGreaterEqual(left, right);
  }

  @Override
  protected Unsure visitLess(Less obj, Map<Expression, Unsure> param) {
    RangeValue left = reval.traverse(obj.getLeft(), null);
    RangeValue right = reval.traverse(obj.getRight(), null);
    return evalGreater(right, left); // TODO ok?
  }

  @Override
  protected Unsure visitLessequal(Lessequal obj, Map<Expression, Unsure> param) {
    RangeValue left = reval.traverse(obj.getLeft(), null);
    RangeValue right = reval.traverse(obj.getRight(), null);
    return evalGreaterEqual(right, left); // TODO ok?
  }

}

class ExprStaticRangeEval extends NullTraverser<RangeValue, Void> {
  final private KnowWriter kw;
  final private KnowledgeBase kb;

  public ExprStaticRangeEval(KnowledgeBase kb) {
    super();
    kw = kb.getEntry(KnowWriter.class);
    this.kb = kb;
  }

  @Override
  protected RangeValue visitDefault(Evl obj, Void param) {
    throw new RuntimeException("not yet implemented: " + obj.getClass().getCanonicalName());
  }

  private Type getType(Reference obj) {
    Type type = ExpressionTypeChecker.process(obj, kb); // TODO replace with a type getter function (not so bloated)
    return type;
  }

  @Override
  protected RangeValue visitReference(Reference obj, Void param) {
    Type type = getType(obj);
    assert (type instanceof NumSet);
    NumSet ns = (NumSet) type;
    return new RangeValue(obj.getInfo(), ns.getNumbers());
  }

  @Override
  protected RangeValue visitNumber(Number obj, Void param) {
    return new RangeValue(obj.getInfo(), new NumberSet(new Range(obj.getValue(), obj.getValue())));
  }

}
