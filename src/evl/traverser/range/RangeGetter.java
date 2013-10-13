package evl.traverser.range;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import util.NumberSet;
import util.Unsure;
import evl.DefTraverser;
import evl.Evl;
import evl.NullTraverser;
import evl.copy.Copy;
import evl.expression.BoolValue;
import evl.expression.Expression;
import evl.expression.RangeValue;
import evl.expression.binop.And;
import evl.expression.binop.Equal;
import evl.expression.binop.Notequal;
import evl.expression.binop.Or;
import evl.expression.binop.Relation;
import evl.expression.reference.Reference;
import evl.expression.unop.Not;
import evl.function.FunctionHeader;
import evl.knowledge.KnowWriter;
import evl.knowledge.KnowledgeBase;
import evl.solver.RelationEvaluator;
import evl.solver.Solver;
import evl.traverser.ExprBuilder;
import evl.traverser.ExprReplacer;
import evl.traverser.ExprStaticBoolEval;
import evl.traverser.NormalizeBool;
import evl.traverser.typecheck.specific.ExpressionTypeChecker;
import evl.type.Type;
import evl.type.base.BooleanType;
import evl.type.base.NumSet;
import evl.variable.SsaVariable;

public class RangeGetter extends NullTraverser<Map<SsaVariable, NumSet>, Boolean> {

  final private KnowledgeBase kb;
  final private KnowWriter kw;
  final private Map<Expression, Unsure> exprEval;

  public RangeGetter(Map<Expression, Unsure> exprEval, KnowledgeBase kb) {
    super();
    this.kb = kb;
    kw = kb.getEntry(KnowWriter.class);
    this.exprEval = exprEval;
  }

  public static Map<SsaVariable, NumSet> getSmallerRangeFor(boolean evalTo, Expression boolExpr, KnowledgeBase kb) {
    boolExpr = ExprBuilder.makeTree(boolExpr, kb);
    boolExpr = NormalizeBool.process(boolExpr, kb);
    Map<Expression, Unsure> eval = ExprStaticBoolEval.eval(boolExpr, kb);
    RangeGetter updater = new RangeGetter(eval, kb);
    Map<SsaVariable, NumSet> varrange = updater.traverse(boolExpr, evalTo);
    assert (varrange != null);
    return varrange;
  }

  @Override
  protected Map<SsaVariable, NumSet> visitDefault(Evl obj, Boolean param) {
    throw new RuntimeException("not yet implemented: " + obj.getClass().getCanonicalName());
  }

  @Override
  public Map<SsaVariable, NumSet> traverse(Evl obj, Boolean param) {
    assert (param != null);
    return super.traverse(obj, param);
  }

  @Override
  protected Map<SsaVariable, NumSet> visitReference(Reference obj, Boolean param) {
    if (obj.getLink() instanceof SsaVariable) {
      assert (obj.getOffset().isEmpty());
      assert (obj.getLink() instanceof SsaVariable);
      Expression writer = kw.get((SsaVariable) obj.getLink());
      return visit(writer, param);
    } else if (obj.getLink() instanceof FunctionHeader) {
      return new HashMap<SsaVariable, NumSet>();
    } else {
      throw new RuntimeException("not yet implemented: " + obj.getLink().getClass().getCanonicalName());
    }
  }

  @Override
  protected Map<SsaVariable, NumSet> visitRelation(Relation obj, Boolean param) {
    assert (param != null);

    Type st = ExpressionTypeChecker.process(obj.getLeft(), kb); // TODO replace with type getter

    if (st instanceof BooleanType) {
      return super.visitRelation(obj, param);
    } else {
      return evalRanges(obj, param);
    }
  }

  private Map<SsaVariable, NumSet> evalRanges(Relation obj, boolean evalTo) {
    Map<SsaVariable, NumSet> ret = new HashMap<SsaVariable, NumSet>();
    Set<SsaVariable> variables = new HashSet<SsaVariable>();
    VarGetter vg = new VarGetter(kb);
    vg.traverse(obj, variables);
    for (SsaVariable var : variables) {
      if ((var.getType().getRef() instanceof NumSet)) {
        Expression et = Copy.copy(obj);
        VarRefByRangeReplacer.INSTANCE.traverse(et, var);
        if (!evalTo) {
          et = new Not(et.getInfo(), et);
        }
        et = NormalizeBool.process(et, kb);

        assert (et instanceof Relation);
        et = Solver.solve(var, (Relation) et);
        assert (et instanceof Relation);
        assert (((Relation) et).getLeft() instanceof Reference);
        assert (((Reference) ((Relation) et).getLeft()).getLink() == var);
        assert (((Relation) et).getRight() instanceof RangeValue);

        NumberSet retset = RelationEvaluator.eval((Relation) et, var);

        NumberSet varType = ((NumSet) var.getType().getRef()).getNumbers();

        int cmp = retset.getNumberCount().compareTo(varType.getNumberCount());
        assert (cmp <= 0); // because of intersection
        if (cmp < 0) {
          ret.put(var, new NumSet(retset.getRanges()));
        }

      }
    }
    return ret;
  }

  @Override
  protected Map<SsaVariable, NumSet> visitBoolValue(BoolValue obj, Boolean param) {
    return new HashMap<SsaVariable, NumSet>();
  }

  @Override
  protected Map<SsaVariable, NumSet> visitNot(Not obj, Boolean param) {
    throw new RuntimeException("not yet implemented");
  }

  @Override
  protected Map<SsaVariable, NumSet> visitAnd(And obj, Boolean param) {
    assert (param != null);
    Map<SsaVariable, NumSet> left = visit(obj.getLeft(), param);
    Map<SsaVariable, NumSet> right = visit(obj.getRight(), param);
    if (param) {
      return evalAnd(left, right);
    } else {
      return evalOr(left, right);
    }
  }

  @Override
  protected Map<SsaVariable, NumSet> visitOr(Or obj, Boolean param) {
    assert (param != null);
    Map<SsaVariable, NumSet> left = visit(obj.getLeft(), param);
    Map<SsaVariable, NumSet> right = visit(obj.getRight(), param);
    if (param) {
      return evalOr(left, right);
    } else {
      return evalAnd(left, right);
    }
  }

  private Map<SsaVariable, NumSet> evalAnd(Map<SsaVariable, NumSet> left, Map<SsaVariable, NumSet> right) {
    Map<SsaVariable, NumSet> ret = new HashMap<SsaVariable, NumSet>();
    ret.putAll(left);
    ret.putAll(right);

    Set<SsaVariable> vars = new HashSet<SsaVariable>(left.keySet());
    vars.retainAll(right.keySet());
    for (SsaVariable var : vars) {
      NumSet lr = left.get(var);
      NumSet rr = right.get(var);
      NumSet rs = new NumSet(NumberSet.intersection(lr.getNumbers(), rr.getNumbers()));
      ret.put(var, rs);
    }
    return ret;
  }

  private Map<SsaVariable, NumSet> evalOr(Map<SsaVariable, NumSet> left, Map<SsaVariable, NumSet> right) {
    Map<SsaVariable, NumSet> ret = new HashMap<SsaVariable, NumSet>();
    ret.putAll(left);
    ret.putAll(right);

    Set<SsaVariable> vars = new HashSet<SsaVariable>(left.keySet());
    vars.retainAll(right.keySet());
    for (SsaVariable var : vars) {
      NumSet lr = left.get(var);
      NumSet rr = right.get(var);
      NumberSet rs = NumberSet.union(lr.getNumbers(), rr.getNumbers());
      ret.put(var, new NumSet(rs));
    }
    return ret;
  }

  @Override
  protected Map<SsaVariable, NumSet> visitEqual(Equal obj, Boolean param) {
    Unsure le = exprEval.get(obj.getLeft());
    Unsure re = exprEval.get(obj.getRight());
    Map<SsaVariable, NumSet> left = visit(obj.getLeft(), param);
    Map<SsaVariable, NumSet> right = visit(obj.getRight(), param);
    throw new RuntimeException("not yet implemented");
  }

  @Override
  protected Map<SsaVariable, NumSet> visitNotequal(Notequal obj, Boolean param) {
    throw new RuntimeException("not yet implemented");
  }

}

class VarGetter extends DefTraverser<Void, Set<SsaVariable>> {
  private final KnowWriter kw;

  public VarGetter(KnowledgeBase kb) {
    kw = kb.getEntry(KnowWriter.class);
  }

  @Override
  protected Void visitReference(Reference obj, Set<SsaVariable> param) {
    if (obj.getLink() instanceof SsaVariable) {
      SsaVariable var = (SsaVariable) obj.getLink();
      param.add(var);
      if (var.getType().getRef() instanceof BooleanType) {
        Expression writer = kw.get(var);
        visit(writer, param);
      }
    }
    return super.visitReference(obj, param);
  }
}

class VarRefByRangeReplacer extends ExprReplacer<SsaVariable> {
  static final public VarRefByRangeReplacer INSTANCE = new VarRefByRangeReplacer();

  @Override
  protected Expression visitReference(Reference obj, SsaVariable param) {
    if (obj.getLink() instanceof SsaVariable) {
      assert (obj.getOffset().isEmpty());
      SsaVariable var = (SsaVariable) obj.getLink();
      if (var == param) {
        return obj;
      }
      NumSet ns = (NumSet) var.getType().getRef();
      return new RangeValue(obj.getInfo(), ns.getNumbers());
    } else {
      throw new RuntimeException("not yet implemented");
    }
  }

}
