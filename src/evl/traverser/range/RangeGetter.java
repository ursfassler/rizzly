package evl.traverser.range;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;

import evl.Evl;
import evl.NullTraverser;
import evl.expression.BoolValue;
import evl.expression.Expression;
import evl.expression.RelOp;
import evl.expression.Relation;
import evl.expression.UnaryExpression;
import evl.expression.reference.Reference;
import evl.knowledge.KnowBaseItem;
import evl.knowledge.KnowledgeBase;
import evl.traverser.typecheck.specific.ExpressionTypeChecker;
import evl.type.Type;
import evl.type.base.Range;
import evl.variable.SsaVariable;
import evl.variable.Variable;

//TODO implement everything left
public class RangeGetter extends NullTraverser<Void, Void> {
  private KnowledgeBase kb;
  private KnowBaseItem kbi;
  private Map<SsaVariable, Range> ranges = new HashMap<SsaVariable, Range>();

  public RangeGetter(KnowledgeBase kb) {
    super();
    this.kb = kb;
    kbi = kb.getEntry(KnowBaseItem.class);
  }

  //TODO follow ssa variables
  public static Map<SsaVariable, Range> getRange(Expression condition, KnowledgeBase kb) {
    RangeGetter updater = new RangeGetter(kb);
    updater.traverse(condition, null);
    return updater.ranges;
  }

  @Override
  protected Void visitDefault(Evl obj, Void param) {
    throw new RuntimeException("not yet implemented: " + obj.getClass().getCanonicalName());
  }

  public static SsaVariable getDerefVar(Expression left) {
    if (left instanceof Reference) {
      Reference ref = (Reference) left;
      if (ref.getOffset().isEmpty()) {
        if (ref.getLink() instanceof SsaVariable) {
          return (SsaVariable) ref.getLink();
        }
      }
    }
    return null;
  }

  @Override
  protected Void visitRelation(Relation obj, Void param) {
    {
      SsaVariable lv = getDerefVar(obj.getLeft());
      if (lv != null) {
        if (lv.getType().getRef() instanceof Range) {
          Type rt = ExpressionTypeChecker.process(obj.getRight(), kb);
          if (rt instanceof Range) {
            Range rr = (Range) rt;
            Range range = getRange(lv);
            range = adjustLeft(range, obj.getOp(), rr);
            ranges.put(lv, range);
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
    case EQUAL: {
      low = rr.getLow().max(lr.getLow());
      high = rr.getHigh().min(lr.getHigh());
      break;
    }
    default:
      throw new RuntimeException("not yet implemented: " + op);
    }
    return kbi.getRangeType(low, high);
  }

  private Range getRange(Variable var) {
    if (ranges.containsKey(var)) {
      return ranges.get(var);
    } else {
      return (Range) var.getType().getRef();
    }
  }

  @Override
  protected Void visitReference(Reference obj, Void param) {
    return null;
  }

  @Override
  protected Void visitUnaryExpression(UnaryExpression obj, Void param) {
    return null; // TODO should we support it?
  }

  @Override
  protected Void visitBoolValue(BoolValue obj, Void param) {
    return null;
  }

}
