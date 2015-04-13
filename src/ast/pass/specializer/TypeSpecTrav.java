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
import ast.data.expression.reference.DummyLinkTarget;
import ast.data.expression.reference.Reference;
import ast.data.template.ActualTemplateArgument;
import ast.data.type.Type;
import ast.data.variable.TemplateParameter;
import ast.traverser.other.ExprReplacer;

/**
 * Replaces a reference to a CompfuncParameter with the value of it
 *
 * @author urs
 *
 */
public class TypeSpecTrav extends ExprReplacer<Map<TemplateParameter, ActualTemplateArgument>> {

  @Override
  protected ast.data.expression.Expression visitReference(Reference obj, Map<TemplateParameter, ActualTemplateArgument> param) {
    assert (!(obj.link instanceof DummyLinkTarget));
    super.visitReference(obj, param);

    if (param.containsKey(obj.link)) {
      ActualTemplateArgument repl = param.get(obj.link);
      if (repl instanceof Type) {
        return new Reference(obj.getInfo(), (ast.data.type.Type) repl);
      } else {
        return Copy.copy((Expression) repl);
      }
    } else {
      assert (!(obj.link instanceof TemplateParameter));
      return obj;
    }
  }

}
