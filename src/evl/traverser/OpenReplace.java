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

package evl.traverser;

import java.math.BigInteger;
import java.util.ArrayList;
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
import evl.function.Function;
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

    List<Type> openTypes = new ArrayList<Type>();
    openTypes.addAll(kb.getRoot().getItems(IntegerType.class, false));
    openTypes.addAll(kb.getRoot().getItems(NaturalType.class, false));

    if (openTypes.isEmpty()) {
      // no open type used
      return;
    }

    List<RangeType> ranges = ClassGetter.get(RangeType.class, kb.getRoot());
    if (ranges.isEmpty()) {
      return;
    }

    OpenReplace replace = new OpenReplace(openTypes, kb);
    replace.traverse(evl, null);
    for (Variable var : replace.map.keySet()) {
      assert (openTypes.contains(var.getType().getLink()));
      RangeType range = replace.map.get(var);
      range = kbi.getNumsetType(range.getNumbers());
      var.getType().setLink(range);
    }
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
