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
import evl.knowledge.KnowWriter;
import evl.knowledge.KnowledgeBase;
import evl.traverser.typecheck.specific.ExpressionTypeChecker;
import evl.type.Type;
import evl.type.base.Range;
import evl.variable.SsaVariable;
import evl.variable.StateVariable;
import evl.variable.Variable;

//TODO implement everything left
public class RangeGetter extends NullTraverser<Void, Void> {

  final private KnowledgeBase kb;
  final private KnowBaseItem kbi;
  final private KnowWriter kw;
  final private Map<SsaVariable, Range> ranges = new HashMap<SsaVariable, Range>();
  final private Map<StateVariable, Range> sranges = new HashMap<StateVariable, Range>();

  public RangeGetter(KnowledgeBase kb) {
    super();
    this.kb = kb;
    kbi = kb.getEntry(KnowBaseItem.class);
    kw = kb.getEntry(KnowWriter.class);
  }

  //TODO follow ssa variables
  public static Map<SsaVariable, Range> getRange(Expression condition, KnowledgeBase kb) {
    RangeGetter updater = new RangeGetter(kb);
    updater.traverse(condition, null);
    return updater.ranges;
  }

  public Map<SsaVariable, Range> getRanges() {
    return ranges;
  }

  public Map<StateVariable, Range> getSranges() {
    return sranges;
  }

  private Range getRange(Variable var) {
    if( ranges.containsKey(var) ) {
      return ranges.get(var);
    } else if( sranges.containsKey(var) ) {
      return sranges.get(var);
    } else {
      return (Range) var.getType().getRef();
    }
  }

  @Override
  protected Void visitDefault(Evl obj, Void param) {
    throw new RuntimeException("not yet implemented: " + obj.getClass().getCanonicalName());
  }

  public static Variable getDerefVar(Expression left) {
    if( left instanceof Reference ) {
      Reference ref = (Reference) left;
      if( ref.getOffset().isEmpty() ) {
        if( ref.getLink() instanceof Variable ) {
          return (Variable) ref.getLink();
        }
      }
    }
    return null;
  }

  @Override
  protected Void visitRelation(Relation obj, Void param) {
    {
      Variable lv = getDerefVar(obj.getLeft());
      if( lv != null ) {
        if( lv.getType().getRef() instanceof Range ) {
          Type rt = ExpressionTypeChecker.process(obj.getRight(), kb);
          if( rt instanceof Range ) {
            Range rr = (Range) rt;
            Range range = getRange(lv);
            range = adjustLeft(range, obj.getOp(), rr);
            if( lv instanceof SsaVariable ) {
              ranges.put((SsaVariable) lv, range);
            } else if( lv instanceof StateVariable ) {
              //TODO check that state variable was not written during test (by a called function)
              //TODO should not be possible since the only call comes from transition guard which is not allowed to change state
              sranges.put((StateVariable) lv, range);
            } else {
              throw new RuntimeException("not yet implemented: " + obj);
            }
            return null;
          }
        } else {
          // not range type, we do not care
          //TODO should we care about different types?
          return null;
        }
      }
    }
    throw new RuntimeException("not yet implemented: " + obj);
  }

  private Range adjustLeft(Range lr, RelOp op, Range rr) {
    BigInteger low;
    BigInteger high;
    switch( op ) {
      case LESS: {
        BigInteger max = rr.getHigh().subtract(BigInteger.ONE);
        low = lr.getLow().min(max);
        high = lr.getHigh().min(max);
        break;
      }
      case LESS_EQUAL:{
        BigInteger max = rr.getHigh();
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

  @Override
  protected Void visitReference(Reference obj, Void param) {
    if( !(obj.getLink() instanceof SsaVariable) ){
      return null;
    }
    assert ( obj.getOffset().isEmpty() );
    assert ( obj.getLink() instanceof SsaVariable );
    Expression writer = kw.get((SsaVariable) obj.getLink());
    visit(writer, null);
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
