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

import ast.data.Ast;
import ast.pass.output.xml.IdReader;
import ast.repository.query.Referencees.ReferenceesFactory;
import ast.repository.query.Referencees.ReferenceesReader;
import ast.visitor.EveryVisitor;

public class IdReaderFactory {

  public IdReader produce(Ast root) {
    ReferenceesReader referenceesReader = (new ReferenceesFactory()).produce(root);
    IdGenerator idGenerator = new IncreasingIdGenerator();

    IdForReferenced idCollector = new IdForReferenced(referenceesReader, idGenerator);
    EveryVisitor.visitEverything(idCollector, root);

    return idCollector;
  }

}
