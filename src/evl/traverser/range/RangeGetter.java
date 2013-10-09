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
import evl.knowledge.KnowWriter;
import evl.knowledge.KnowledgeBase;
import evl.solver.RelationEvaluator;
import evl.solver.Solver;
import evl.traverser.ExprBuilder;
import evl.traverser.ExprStaticBoolEval;
import evl.traverser.NormalizeBool;
import evl.traverser.typecheck.specific.ExpressionTypeChecker;
import evl.type.Type;
import evl.type.base.BooleanType;
import evl.type.base.NumSet;
import evl.variable.SsaVariable;
import evl.variable.Variable;

public class RangeGetter extends NullTraverser<Map<Variable, NumSet>, Boolean> {

  final private KnowledgeBase kb;
  final private KnowWriter kw;
  final private Map<Expression, Unsure> exprEval;

  public RangeGetter(Map<Expression, Unsure> exprEval, KnowledgeBase kb) {
    super();
    this.kb = kb;
    kw = kb.getEntry(KnowWriter.class);
    this.exprEval = exprEval;
  }

  public static <T extends Variable> Map<Variable, NumSet> getSmallerRangeFor(boolean evalTo, Expression boolExpr, KnowledgeBase kb) {
    Map<Expression, Unsure> eval = ExprStaticBoolEval.eval(boolExpr, kb);
    RangeGetter updater = new RangeGetter(eval, kb);
    Map<Variable, NumSet> varrange = updater.traverse(boolExpr, evalTo);
    assert (varrange != null);
    return varrange;
  }

  public static <T extends Variable> Map<T, NumSet> getSmallerRangeFor(boolean evalTo, Expression boolExpr, Class<T> type, KnowledgeBase kb) {
    Map<Variable, NumSet> varrange = getSmallerRangeFor(evalTo, boolExpr, kb);
    Map<T, NumSet> ret = new HashMap<T, NumSet>();
    for (Variable var : varrange.keySet()) {
      if (var.getClass() == type) {
        ret.put((T) var, varrange.get(var));
      }
    }
    return ret;
  }

  @Override
  protected Map<Variable, NumSet> visitDefault(Evl obj, Boolean param) {
    throw new RuntimeException("not yet implemented: " + obj.getClass().getCanonicalName());
  }

  @Override
  public Map<Variable, NumSet> traverse(Evl obj, Boolean param) {
    assert (param != null);
    return super.traverse(obj, param);
  }

  @Override
  protected Map<Variable, NumSet> visitReference(Reference obj, Boolean param) {
    if (!(obj.getLink() instanceof SsaVariable)) {
      throw new RuntimeException("not yet implemented");
      // return null;
    }
    assert (obj.getOffset().isEmpty());
    assert (obj.getLink() instanceof SsaVariable);
    Expression writer = kw.get((SsaVariable) obj.getLink());
    return visit(writer, param);
  }

  @Override
  protected Map<Variable, NumSet> visitRelation(Relation obj, Boolean param) {
    assert (param != null);

    Type st = ExpressionTypeChecker.process(obj.getLeft(), kb); // TODO replace with type getter

    if (st instanceof BooleanType) {
      return super.visitRelation(obj, param);
    } else {
      return evalRanges(obj, param);
    }
  }

  private Map<Variable, NumSet> evalRanges(Relation obj, boolean evalTo) {
    Map<Variable, NumSet> ret = new HashMap<Variable, NumSet>();
    Set<Variable> variables = new HashSet<Variable>();
    VarGetter vg = new VarGetter(kb);
    vg.traverse(obj, variables);
    for (Variable var : variables) {
      if ((var.getType().getRef() instanceof NumSet)) {
        Expression et = ExprBuilder.makeTree(obj, var, kb);
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
  protected Map<Variable, NumSet> visitBoolValue(BoolValue obj, Boolean param) {
    return new HashMap<Variable, NumSet>();
  }

  @Override
  protected Map<Variable, NumSet> visitNot(Not obj, Boolean param) {
    throw new RuntimeException("not yet implemented");
  }

  @Override
  protected Map<Variable, NumSet> visitAnd(And obj, Boolean param) {
    assert (param != null);
    Map<Variable, NumSet> left = visit(obj.getLeft(), param);
    Map<Variable, NumSet> right = visit(obj.getRight(), param);
    if (param) {
      return evalAnd(left, right);
    } else {
      return evalOr(left, right);
    }
  }

  @Override
  protected Map<Variable, NumSet> visitOr(Or obj, Boolean param) {
    assert (param != null);
    Map<Variable, NumSet> left = visit(obj.getLeft(), param);
    Map<Variable, NumSet> right = visit(obj.getRight(), param);
    if (param) {
      return evalOr(left, right);
    } else {
      return evalAnd(left, right);
    }
  }

  private Map<Variable, NumSet> evalAnd(Map<Variable, NumSet> left, Map<Variable, NumSet> right) {
    Map<Variable, NumSet> ret = new HashMap<Variable, NumSet>();
    ret.putAll(left);
    ret.putAll(right);

    Set<Variable> vars = new HashSet<Variable>(left.keySet());
    vars.retainAll(right.keySet());
    for (Variable var : vars) {
      NumSet lr = left.get(var);
      NumSet rr = right.get(var);
      NumSet rs = new NumSet(NumberSet.intersection(lr.getNumbers(), rr.getNumbers()));
      ret.put(var, rs);
    }
    return ret;
  }

  private Map<Variable, NumSet> evalOr(Map<Variable, NumSet> left, Map<Variable, NumSet> right) {
    Map<Variable, NumSet> ret = new HashMap<Variable, NumSet>();
    ret.putAll(left);
    ret.putAll(right);

    Set<Variable> vars = new HashSet<Variable>(left.keySet());
    vars.retainAll(right.keySet());
    for (Variable var : vars) {
      NumSet lr = left.get(var);
      NumSet rr = right.get(var);
      NumberSet rs = NumberSet.union(lr.getNumbers(), rr.getNumbers());
      ret.put(var, new NumSet(rs));
    }
    return ret;
  }

  @Override
  protected Map<Variable, NumSet> visitEqual(Equal obj, Boolean param) {
    Unsure le = exprEval.get(obj.getLeft());
    Unsure re = exprEval.get(obj.getRight());
    Map<Variable, NumSet> left = visit(obj.getLeft(), param);
    Map<Variable, NumSet> right = visit(obj.getRight(), param);
    throw new RuntimeException("not yet implemented");
  }

  @Override
  protected Map<Variable, NumSet> visitNotequal(Notequal obj, Boolean param) {
    throw new RuntimeException("not yet implemented");
  }

}

class VarGetter extends DefTraverser<Void, Set<Variable>> {
  private final KnowWriter kw;

  public VarGetter(KnowledgeBase kb) {
    kw = kb.getEntry(KnowWriter.class);
  }

  @Override
  protected Void visitReference(Reference obj, Set<Variable> param) {
    if (obj.getLink() instanceof Variable) {
      Variable var = (Variable) obj.getLink();
      param.add(var);
      if (var.getType().getRef() instanceof BooleanType) {
        assert (var instanceof SsaVariable);
        Expression writer = kw.get((SsaVariable) var);
        visit(writer, param);
      }
    }
    return super.visitReference(obj, param);
  }
}
