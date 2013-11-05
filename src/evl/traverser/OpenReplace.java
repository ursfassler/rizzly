package evl.traverser;

import java.math.BigInteger;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import util.Range;
import evl.DefTraverser;
import evl.Evl;
import evl.expression.Expression;
import evl.expression.reference.RefCall;
import evl.expression.reference.Reference;
import evl.function.FunctionHeader;
import evl.knowledge.KnowBaseItem;
import evl.knowledge.KnowType;
import evl.knowledge.KnowledgeBase;
import evl.type.Type;
import evl.type.base.RangeType;
import evl.type.special.IntegerType;
import evl.type.special.NaturalType;
import evl.variable.FuncVariable;
import evl.variable.Variable;

/**
 * Replaces open types (like Integer or Natural) of function arguments with appropriate range types.
 * 
 */
public class OpenReplace extends DefTraverser<Void, Void> {
  private final KnowType kt;
  private final Set<Type> openTypes;
  private final Map<Variable, RangeType> map = new HashMap<Variable, RangeType>();

  public OpenReplace(Collection<Type> openTypes, KnowledgeBase kb) {
    super();
    this.kt = kb.getEntry(KnowType.class);
    this.openTypes = new HashSet<Type>(openTypes);
  }

  public static void process(Evl evl, KnowledgeBase kb) {
    KnowBaseItem kbi = kb.getEntry(KnowBaseItem.class);

    if ((kbi.findItem(IntegerType.NAME) == null) && (kbi.findItem(NaturalType.NAME) == null)) {
      // no open type used
      return;
    }

    Set<Type> openTypes = new HashSet<Type>();
    openTypes.add(kbi.getIntegerType());
    openTypes.add(kbi.getNaturalType());

    List<RangeType> ranges = ClassGetter.get(RangeType.class, kb.getRoot());
    if (ranges.isEmpty()) {
      return;
    }

    OpenReplace replace = new OpenReplace(openTypes, kb);
    replace.traverse(evl, null);
    for (Variable var : replace.map.keySet()) {
      assert (openTypes.contains(var.getType().getRef()));
      RangeType range = replace.map.get(var);
      range = kbi.getNumsetType(range.getNumbers());
      var.getType().setRef(range);
    }
  }

  @Override
  protected Void visitReference(Reference obj, Void param) {
    if ((obj.getLink() instanceof FunctionHeader) && !obj.getOffset().isEmpty()) {
      FunctionHeader func = (FunctionHeader) obj.getLink();
      RefCall call = (RefCall) obj.getOffset().get(0);

      List<FuncVariable> arg = func.getParam().getList();
      List<Expression> acarg = call.getActualParameter();

      assert (arg.size() == acarg.size());

      for (int i = 0; i < arg.size(); i++) {
        Variable iarg = arg.get(i);
        Expression iaca = acarg.get(i);

        if (openTypes.contains(iarg.getType().getRef())) {
          Type provtype = kt.get(iaca);
          updateVar(iarg, provtype);
        }
      }
    }
    return super.visitReference(obj, param);
  }

  private void updateVar(Variable var, Type type) {
    assert (type instanceof RangeType);
    RangeType range = (RangeType) type;

    if (map.containsKey(var)) {
      RangeType old = map.get(var);
      BigInteger low = range.getNumbers().getLow().min(old.getNumbers().getLow());
      BigInteger high = range.getNumbers().getHigh().max(old.getNumbers().getHigh());
      range = new RangeType(new Range(low, high));
    }

    map.put(var, range);
  }
}
