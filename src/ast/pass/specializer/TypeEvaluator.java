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

import ast.data.expression.reference.RefTemplCall;
import ast.data.expression.reference.Reference;
import ast.data.expression.reference.SimpleRef;
import ast.data.expression.reference.TypeRef;
import ast.data.template.Template;
import ast.data.type.Type;
import ast.interpreter.Memory;
import ast.knowledge.KnowledgeBase;

public class TypeEvaluator {

  public static Type evaluate(TypeRef obj, Memory memory, InstanceRepo ir, KnowledgeBase kb) {
    if (obj instanceof Reference) {
      Reference ref = (Reference) obj;

      if (ref.link instanceof Template) {
        Template template = (Template) ref.link;
        assert (ref.offset.size() == 1);
        assert (ref.offset.get(0) instanceof RefTemplCall);
        RefTemplCall call = (RefTemplCall) ref.offset.get(0);

        return (Type) Specializer.process(template, call.actualParameter, ir, kb);
      } else if (ref.link instanceof Type) {
        assert (ref.offset.size() == 0);
        return (Type) ref.link;
      } else {
        throw new RuntimeException("bo");
      }
    } else {
      assert (obj instanceof SimpleRef);
      return ((SimpleRef<Type>) obj).link;
    }
  }

}
