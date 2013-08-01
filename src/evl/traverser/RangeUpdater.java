package evl.traverser;

import java.math.BigInteger;
import java.util.HashMap;

import evl.Evl;
import evl.NullTraverser;
import evl.expression.Expression;
import evl.expression.RelOp;
import evl.expression.Relation;
import evl.expression.reference.Reference;
import evl.knowledge.KnowBaseItem;
import evl.knowledge.KnowledgeBase;
import evl.traverser.typecheck.specific.ExpressionTypeChecker;
import evl.type.Type;
import evl.type.base.Range;
import evl.variable.Variable;

//TODO implement everything left
public class RangeUpdater extends NullTraverser<Void, HashMap<Variable, Range>> {
  private KnowledgeBase kb;
  private KnowBaseItem kbi;

  public RangeUpdater(KnowledgeBase kb) {
    super();
    this.kb = kb;
    kbi = kb.getEntry(KnowBaseItem.class);
  }

  @Override
  protected Void visitDefault(Evl obj, HashMap<Variable, Range> param) {
    throw new RuntimeException("not yet implemented: " + obj.getClass().getCanonicalName());
  }

  public static void process(Expression condition, HashMap<Variable, Range> varRange, KnowledgeBase kb) {
    RangeUpdater updater = new RangeUpdater(kb);
    updater.traverse(condition, varRange);
  }

  public static Variable getDerefVar(Expression left) {
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
  protected Void visitRelation(Relation obj, HashMap<Variable, Range> param) {
    {
      Variable lv = getDerefVar(obj.getLeft());
      if (lv != null) {
        if (lv.getType() instanceof Range) {
          Type rt = ExpressionTypeChecker.process(obj.getRight(), kb);
          if (rt instanceof Range) {
            Range rr = (Range) rt;
            Range range = getRange(lv, param);
            range = adjustLeft(range, obj.getOp(), rr);
            param.put(lv, range);
            return null;
          }
        }
      }
    }
    throw new RuntimeException("not yet implemented");
  }

  private Range adjustLeft(Range lr, RelOp op, Range rr) {
    BigInteger low;
    BigInteger high;
    switch (op) {
    case LESS: {
      BigInteger max = rr.getHigh().subtract(BigInteger.ONE);
      low = lr.getLow().min(max);
      high = lr.getHigh().min(max);
      break;
    }
    case GREATER: {
      BigInteger min = rr.getLow().add(BigInteger.ONE);
      low = lr.getLow().max(min);
      high = lr.getHigh().max(min);
      break;
    }
    default:
      throw new RuntimeException("not yet implemented: " + op);
    }
    return kbi.getRangeType(low,high);
  }

  private Range getRange(Variable var, HashMap<Variable, Range> map) {
    if (map.containsKey(var)) {
      return map.get(var);
    } else {
      return (Range) var.getType();
    }
  }
}
