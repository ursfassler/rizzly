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

package ast.pass.specializer;

import java.util.Map;

import ast.copy.Copy;
import ast.data.expression.Expression;
import ast.data.expression.RefExp;
import ast.data.reference.DummyLinkTarget;
import ast.data.reference.Reference;
import ast.data.variable.TemplateParameter;
import ast.traverser.other.ExprReplacer;

public class ExprSpecTrav extends ExprReplacer<Void> {
  final private Map<TemplateParameter, Expression> values;

  public ExprSpecTrav(Map<TemplateParameter, Expression> values) {
    super();
    this.values = values;
  }

  @Override
  protected Expression visitRefExpr(RefExp obj, Void param) {
    obj = (RefExp) super.visitRefExpr(obj, param);

    if (values.containsKey(obj.ref.link)) {
      Reference ref = (Reference) obj.ref;

      assert (!(ref.link instanceof DummyLinkTarget));
      assert (ref.offset.isEmpty());

      Expression repl = values.get(ref.link);
      return Copy.copy(repl);
    }

    return obj;
  }

}
