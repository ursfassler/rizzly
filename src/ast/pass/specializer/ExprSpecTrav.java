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
import ast.data.expression.ReferenceExpression;
import ast.data.expression.value.ValueExpr;
import ast.data.reference.LinkedAnchor;
import ast.data.variable.TemplateParameter;
import ast.dispatcher.other.ExprReplacer;

public class ExprSpecTrav extends ExprReplacer<Void> {
  final private Map<TemplateParameter, ValueExpr> values;

  public ExprSpecTrav(Map<TemplateParameter, ValueExpr> values) {
    super();
    this.values = values;
  }

  @Override
  protected Expression visitRefExpr(ReferenceExpression obj, Void param) {
    obj = (ReferenceExpression) super.visitRefExpr(obj, param);

    LinkedAnchor anchor = (LinkedAnchor) obj.reference.getAnchor();

    if (values.containsKey(anchor.getLink())) {
      Expression repl = values.get(anchor.getLink());
      return Copy.copy(repl);
    }

    return obj;
  }

}
