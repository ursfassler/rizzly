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

import ast.data.reference.DummyLinkTarget;
import ast.data.reference.RefFactory;
import ast.data.reference.Reference;
import ast.data.type.Type;
import ast.data.variable.TemplateParameter;
import ast.traverser.other.RefReplacer;

public class TypeSpecTrav extends RefReplacer<Void> {
  final private Map<TemplateParameter, Type> types;

  public TypeSpecTrav(Map<TemplateParameter, Type> types) {
    super();
    this.types = types;
  }

  @Override
  protected Reference visitReference(Reference obj, Void param) {
    assert (!(obj.link instanceof DummyLinkTarget));
    super.visitReference(obj, param);

    if (types.containsKey(obj.link)) {
      assert (obj.offset.isEmpty());
      Type repl = types.get(obj.link);
      return RefFactory.full(obj.getInfo(), repl);
    }

    return obj;
  }

}
