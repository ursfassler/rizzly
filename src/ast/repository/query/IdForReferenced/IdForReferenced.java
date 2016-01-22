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

package ast.repository.query.IdForReferenced;

import java.util.HashMap;
import java.util.Map;

import ast.data.Ast;
import ast.pass.output.xml.IdReader;
import ast.repository.query.Referencees.ReferenceesReader;
import ast.visitor.DefaultHandler;

public class IdForReferenced implements DefaultHandler, IdReader {
  final private ReferenceesReader referenceesReader;
  final private IdGenerator idGenerator;
  final private Map<Ast, String> ids = new HashMap<Ast, String>();

  public IdForReferenced(ReferenceesReader referenceesReader, IdGenerator idGenerator) {
    this.referenceesReader = referenceesReader;
    this.idGenerator = idGenerator;
  }

  @Override
  public void visit(Ast item) {
    assert (!hasId(item));

    if (hasReferencees(item)) {
      String id = idGenerator.newId(item);
      ids.put(item, id);
    }
  }

  private boolean hasReferencees(Ast item) {
    return !referenceesReader.getReferencees(item).isEmpty();
  }

  @Override
  public boolean hasId(Ast item) {
    return ids.containsKey(item);
  }

  @Override
  public String getId(Ast item) {
    assert (hasId(item));

    return ids.get(item);
  }

}
