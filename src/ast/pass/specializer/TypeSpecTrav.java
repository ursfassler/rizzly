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

import ast.data.reference.LinkTarget;
import ast.data.reference.LinkedReferenceWithOffset_Implementation;
import ast.data.reference.RefFactory;
import ast.data.type.Type;
import ast.data.variable.TemplateParameter;
import ast.dispatcher.other.RefReplacer;

public class TypeSpecTrav extends RefReplacer<Void> {
  final private Map<TemplateParameter, Type> types;

  public TypeSpecTrav(Map<TemplateParameter, Type> types) {
    super();
    this.types = types;
  }

  @Override
  protected LinkedReferenceWithOffset_Implementation visitReference(LinkedReferenceWithOffset_Implementation obj, Void param) {
    assert (!(obj.getLink() instanceof LinkTarget));
    super.visitReference(obj, param);

    if (types.containsKey(obj.getLink())) {
      assert (obj.getOffset().isEmpty());
      Type repl = types.get(obj.getLink());
      return RefFactory.oldFull(obj.metadata(), repl);
    }

    return obj;
  }

}
