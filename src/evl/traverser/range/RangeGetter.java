package evl.traverser.range;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import evl.DefTraverser;
import evl.Evl;
import evl.NullTraverser;
import evl.expression.BoolValue;
import evl.expression.Expression;
import evl.expression.RangeValue;
import evl.expression.binop.Relation;
import evl.expression.reference.Reference;
import evl.expression.unop.Not;
import evl.knowledge.KnowWriter;
import evl.knowledge.KnowledgeBase;
import evl.solver.RelationEvaluator;
import evl.traverser.typecheck.specific.ExpressionTypeChecker;
import evl.type.Type;
import evl.type.base.BooleanType;
import evl.type.base.Range;
import evl.variable.SsaVariable;
import evl.variable.Variable;

public class RangeGetter extends NullTraverser<Map<Variable, Range>, Boolean> {

  final private KnowledgeBase kb;
  final private KnowWriter kw;

  public RangeGetter(KnowledgeBase kb) {
    super();
    this.kb = kb;
    kw = kb.getEntry(KnowWriter.class);
  }

  public static <T extends Variable> Map<Variable, Range> getSmallerRangeFor(boolean evalTo, Expression boolExpr, KnowledgeBase kb) {
    RangeGetter updater = new RangeGetter(kb);
    Map<Variable, Range> varrange = updater.traverse(boolExpr, evalTo);
    assert (varrange != null);
    return varrange;
  }

  public static <T extends Variable> Map<T, Range> getSmallerRangeFor(boolean evalTo, Expression boolExpr, Class<T> type, KnowledgeBase kb) {
    Map<Variable, Range> varrange = getSmallerRangeFor(evalTo, boolExpr, kb);
    Map<T, Range> ret = new HashMap<T, Range>();
    for (Variable var : varrange.keySet()) {
      if (var.getClass() == type) {
        ret.put((T) var, varrange.get(var));
      }
    }
    return ret;
  }

  @Override
  protected Map<Variable, Range> visitDefault(Evl obj, Boolean param) {
    throw new RuntimeException("not yet implemented: " + obj.getClass().getCanonicalName());
  }

  @Override
  public Map<Variable, Range> traverse(Evl obj, Boolean param) {
    assert (param != null);
    return super.traverse(obj, param);
  }

  @Override
  protected Map<Variable, Range> visitReference(Reference obj, Boolean param) {
    if (!(obj.getLink() instanceof SsaVariable)) {
      throw new RuntimeException("not yet implemented");
//      return null;
    }
    assert (obj.getOffset().isEmpty());
    assert (obj.getLink() instanceof SsaVariable);
    Expression writer = kw.get((SsaVariable) obj.getLink());
    return visit(writer, param);
  }

  @Override
  protected Map<Variable, Range> visitRelation(Relation obj, Boolean param) {
    assert (param != null);

    Type st = ExpressionTypeChecker.process(obj.getLeft(), kb); // TODO replace with type getter

    if (st instanceof BooleanType) {
      return super.visitRelation(obj, param);
    } else {
      return evalRanges(obj, param);
    }
  }

  private Map<Variable, Range> evalRanges(Relation obj, boolean evalTo) {
    Map<Variable, Range> ret = new HashMap<Variable, Range>();
    Set<Variable> variables = new HashSet<Variable>();
    VarGetter vg = new VarGetter(kb);
    vg.traverse(obj, variables);
    for (Variable var : variables) {
      if ((var.getType().getRef() instanceof Range)) {
        Expression expr = RelationEvaluator.eval(obj, var);
        if (expr instanceof RangeValue) {
          RangeValue rv = (RangeValue) expr;

          if (!evalTo) {
            // TODO invert range
            throw new RuntimeException("not yet implemented");
          }

          Range newType = new Range(rv.getLow(), rv.getHigh());
          Range varType = (Range) var.getType().getRef();
          if (Range.leftIsSmallerEqual(newType, varType) && !Range.isEqual(newType, varType)) {
            ret.put(var, newType);
          }
        }
      }
    }
    return ret;
  }

  @Override
  protected Map<Variable, Range> visitBoolValue(BoolValue obj, Boolean param) {
    return new HashMap<Variable, Range>();
  }

  @Override
  protected Map<Variable, Range> visitNot(Not obj, Boolean param) {
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
