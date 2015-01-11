/**
 *  This file is part of Rizzly.
 *
 *  Rizzly is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Rizzly is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with Rizzly.  If not, see <http://www.gnu.org/licenses/>.
 */

package evl.pass;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import pass.EvlPass;
import util.Range;
import evl.DefTraverser;
import evl.Evl;
import evl.expression.Expression;
import evl.expression.reference.RefCall;
import evl.expression.reference.Reference;
import evl.function.Function;
import evl.knowledge.KnowBaseItem;
import evl.knowledge.KnowType;
import evl.knowledge.KnowledgeBase;
import evl.other.Namespace;
import evl.traverser.ClassGetter;
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

public class OpenReplace extends EvlPass {

  {
    addDependency(IntroduceConvert.class);
  }

  @Override
  public void process(Namespace evl, KnowledgeBase kb) {
    OpenReplaceWorker worker = new OpenReplaceWorker(kb);
    worker.traverse(evl, null);
  }
}

class OpenReplaceWorker extends DefTraverser<Void, Void> {
  private final KnowType kt;
  private final KnowBaseItem kbi;
  private final Set<Type> openTypes = new HashSet<Type>();
  private final Map<Variable, RangeType> map = new HashMap<Variable, RangeType>();

  public OpenReplaceWorker(KnowledgeBase kb) {
    super();
    this.kt = kb.getEntry(KnowType.class);
    this.kbi = kb.getEntry(KnowBaseItem.class);
  }

  @Override
  public Void traverse(Evl obj, Void param) {
    map.clear();

    List<Type> openTypes = new ArrayList<Type>();
    openTypes.addAll(ClassGetter.get(IntegerType.class, obj));
    openTypes.addAll(ClassGetter.get(NaturalType.class, obj));

    if (openTypes.isEmpty()) {
      // no open type used
      return null;
    }

    List<RangeType> ranges = ClassGetter.get(RangeType.class, obj);
    if (ranges.isEmpty()) {
      return null;
    }

    super.traverse(obj, null);
    for (Variable var : map.keySet()) {
      assert (openTypes.contains(var.getType().getLink()));
      RangeType range = map.get(var);
      range = kbi.getNumsetType(range.getNumbers());
      var.getType().setLink(range);
    }

    return null;
  }

  @Override
  protected Void visitReference(Reference obj, Void param) {
    if ((obj.getLink() instanceof Function) && !obj.getOffset().isEmpty()) {
      Function func = (Function) obj.getLink();
      RefCall call = (RefCall) obj.getOffset().get(0);

      List<FuncVariable> arg = func.getParam();
      List<Expression> acarg = call.getActualParameter();

      assert (arg.size() == acarg.size());

      for (int i = 0; i < arg.size(); i++) {
        Variable iarg = arg.get(i);
        Expression iaca = acarg.get(i);

        if (openTypes.contains(iarg.getType().getLink())) {
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
