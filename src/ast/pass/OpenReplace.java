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

package ast.pass;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;

import pass.AstPass;
import util.Range;
import ast.data.Ast;
import ast.data.AstList;
import ast.data.Namespace;
import ast.data.expression.Expression;
import ast.data.expression.reference.RefCall;
import ast.data.expression.reference.Reference;
import ast.data.expression.reference.SimpleRef;
import ast.data.function.Function;
import ast.data.type.Type;
import ast.data.type.base.RangeType;
import ast.data.variable.FuncVariable;
import ast.data.variable.Variable;
import ast.knowledge.KnowBaseItem;
import ast.knowledge.KnowType;
import ast.knowledge.KnowledgeBase;
import ast.traverser.DefTraverser;

import common.ElementInfo;

/**
 * Replaces open types (like Integer or Natural) of function arguments with appropriate range types.
 *
 */

public class OpenReplace extends AstPass {
  @Override
  public void process(Namespace ast, KnowledgeBase kb) {
    OpenReplaceWorker worker = new OpenReplaceWorker(kb);
    worker.traverse(ast, null);
  }
}

class OpenReplaceWorker extends DefTraverser<Void, Void> {
  private final KnowType kt;
  private final KnowBaseItem kbi;
  private final Map<Variable, RangeType> map = new HashMap<Variable, RangeType>();

  public OpenReplaceWorker(KnowledgeBase kb) {
    super();
    this.kt = kb.getEntry(KnowType.class);
    this.kbi = kb.getEntry(KnowBaseItem.class);
  }

  @Override
  public Void traverse(Ast obj, Void param) {
    map.clear();

    super.traverse(obj, null);
    for (Variable var : map.keySet()) {
      RangeType range = map.get(var);
      range = kbi.getRangeType(range.range);
      var.type = new SimpleRef<Type>(ElementInfo.NO, range);
    }

    return null;
  }

  @Override
  protected Void visitReference(Reference obj, Void param) {
    if ((obj.link instanceof Function) && !obj.offset.isEmpty()) {
      Function func = (Function) obj.link;
      RefCall call = (RefCall) obj.offset.get(0);

      AstList<FuncVariable> arg = func.param;
      AstList<Expression> acarg = call.actualParameter.value;

      assert (arg.size() == acarg.size());

      for (int i = 0; i < arg.size(); i++) {
        Variable iarg = arg.get(i);
        Expression iaca = acarg.get(i);

        Type provtype = kt.get(iaca);
        updateVar(iarg, provtype);
      }
    }
    return super.visitReference(obj, param);
  }

  private void updateVar(Variable var, Type type) {
    if (type instanceof RangeType) {
      RangeType range = (RangeType) type;

      if (map.containsKey(var)) {
        RangeType old = map.get(var);
        BigInteger low = range.range.low.min(old.range.low);
        BigInteger high = range.range.high.max(old.range.high);
        range = new RangeType(new Range(low, high));
      }

      map.put(var, range);
    }
  }
}
