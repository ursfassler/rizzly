package evl.traverser.range;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;

import common.ElementInfo;

import error.ErrorType;
import error.RError;
import evl.Evl;
import evl.NullTraverser;
import evl.expression.BoolValue;
import evl.expression.Expression;
import evl.expression.binop.Equal;
import evl.expression.binop.Greater;
import evl.expression.binop.Greaterequal;
import evl.expression.binop.Less;
import evl.expression.binop.Lessequal;
import evl.expression.binop.Notequal;
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

  private Variable getVar(Expression expr) {
    Variable lv = getDerefVar(expr);
    if( lv != null ) {
      if( lv.getType().getRef() instanceof Range ) {
        return lv;
      }
    }
    return null;
  }

  private Range getRangeType(Expression right) {
    Type rt = ExpressionTypeChecker.process(right, kb);
    if( rt instanceof Range ) {
      return (Range) rt;
    }
    return null;
  }

  private void putRange(Variable lv, Range range, ElementInfo info) throws RuntimeException {
    if( lv instanceof SsaVariable ) {
      ranges.put((SsaVariable) lv, range);
    } else if( lv instanceof StateVariable ) {
      //TODO check that state variable was not written during test (by a called function)
      //TODO should not be possible since the only call comes from transition guard which is not allowed to change state
      sranges.put((StateVariable) lv, range);
    } else {
      RError.err(ErrorType.Fatal, info, "not yet implemented: " + lv.getClass().getCanonicalName());
    }
  }

  @Override
  protected Void visitEqual(Equal obj, Void param) {
    Variable lv = getVar(obj.getLeft());
    Range rr = getRangeType(obj.getRight());
    if( ( lv != null ) && ( rr != null ) ) {
      Range range = getRange(lv);
      BigInteger low = rr.getLow().max(range.getLow());
      BigInteger high = rr.getHigh().min(range.getHigh());
      range = kbi.getRangeType(low, high);
      putRange(lv, range, obj.getInfo());
    }
    return null;
  }

  @Override
  protected Void visitNotequal(Notequal obj, Void param) {
    throw new RuntimeException("not yet implemented: " + obj.getClass().getCanonicalName());
  }

  @Override
  protected Void visitGreater(Greater obj, Void param) {
    Variable lv = getVar(obj.getLeft());
    Range rr = getRangeType(obj.getRight());
    if( ( lv != null ) && ( rr != null ) ) {
      Range range = getRange(lv);
      BigInteger min = rr.getLow().add(BigInteger.ONE);
      BigInteger low = range.getLow().max(min);
      BigInteger high = range.getHigh().max(min);
      range = kbi.getRangeType(low, high);
      putRange(lv, range, obj.getInfo());
    }
    return null;
  }

  @Override
  protected Void visitGreaterequal(Greaterequal obj, Void param) {
    throw new RuntimeException("not yet implemented: " + obj.getClass().getCanonicalName());
  }

  @Override
  protected Void visitLess(Less obj, Void param) {
    Variable lv = getVar(obj.getLeft());
    Range rr = getRangeType(obj.getRight());
    if( ( lv != null ) && ( rr != null ) ) {
      Range range = getRange(lv);
      BigInteger max = rr.getHigh().subtract(BigInteger.ONE);
      BigInteger low = range.getLow().min(max);
      BigInteger high = range.getHigh().min(max);
      range = kbi.getRangeType(low, high);
      putRange(lv, range, obj.getInfo());
    }
    return null;
  }

  @Override
  protected Void visitLessequall(Lessequal obj, Void param) {
    Variable lv = getVar(obj.getLeft());
    Range rr = getRangeType(obj.getRight());
    if( ( lv != null ) && ( rr != null ) ) {
      Range range = getRange(lv);
      BigInteger max = rr.getHigh();
      BigInteger low = range.getLow().min(max);
      BigInteger high = range.getHigh().min(max);
      range = kbi.getRangeType(low, high);
      putRange(lv, range, obj.getInfo());
    }
    return null;
  }

  @Override
  protected Void visitReference(Reference obj, Void param) {
    if( !( obj.getLink() instanceof SsaVariable ) ) {
      return null;
    }
    assert ( obj.getOffset().isEmpty() );
    assert ( obj.getLink() instanceof SsaVariable );
    Expression writer = kw.get((SsaVariable) obj.getLink());
    visit(writer, null);
    return null;
  }

  @Override
  protected Void visitBoolValue(BoolValue obj, Void param) {
    return null;
  }
}
