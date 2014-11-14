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

package fun.traverser;

import fun.DefTraverser;
import fun.Fun;
import fun.expression.Expression;
import fun.knowledge.KnowledgeBase;
import fun.other.ActualTemplateArgument;
import fun.traverser.spezializer.ExprEvaluator;
import fun.variable.Constant;

public class ConstEval extends DefTraverser<Void, Void> {
  private final KnowledgeBase kb;

  public ConstEval(KnowledgeBase kb) {
    super();
    this.kb = kb;
  }

  public static void process(Fun classes, KnowledgeBase kb) {
    ConstEval eval = new ConstEval(kb);
    eval.traverse(classes, null);
  }

  @Override
  protected Void visitConstant(Constant obj, Void param) {
    ActualTemplateArgument value = ExprEvaluator.evaluate(obj.getDef(), new Memory(), kb);
    obj.setDef((Expression) value);
    return null;
  }

}
