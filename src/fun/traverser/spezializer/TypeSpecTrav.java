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

package fun.traverser.spezializer;

import java.util.Map;

import fun.expression.Expression;
import fun.expression.reference.DummyLinkTarget;
import fun.expression.reference.Reference;
import fun.other.ActualTemplateArgument;
import fun.traverser.ExprReplacer;
import fun.type.Type;
import fun.variable.TemplateParameter;

/**
 * Replaces a reference to a CompfuncParameter with the value of it
 *
 * @author urs
 *
 */
public class TypeSpecTrav extends ExprReplacer<Map<TemplateParameter, ActualTemplateArgument>> {

  @Override
  protected Expression visitReference(Reference obj, Map<TemplateParameter, ActualTemplateArgument> param) {
    assert (!(obj.getLink() instanceof DummyLinkTarget));
    super.visitReference(obj, param);

    if (param.containsKey(obj.getLink())) {
      ActualTemplateArgument repl = param.get(obj.getLink());
      if (repl instanceof Type) {
        return new Reference(obj.getInfo(), (Type) repl);
      } else {
        return (Expression) repl;
      }
    } else {
      assert (!(obj.getLink() instanceof TemplateParameter));
      return obj;
    }
  }

}
