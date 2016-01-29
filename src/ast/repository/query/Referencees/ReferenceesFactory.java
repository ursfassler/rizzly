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

package ast.repository.query.Referencees;

import ast.data.Ast;
import ast.visitor.DeepFirstTraverser;
import ast.visitor.VisitExecutorImplementation;

public class ReferenceesFactory {

  public ReferenceesReader produce(Ast root) {
    Referencees referencees = new Referencees();

    DeepFirstTraverser referenceCollector = new DeepFirstTraverser();
    referenceCollector.addPreorderVisitor(new ReferenceesAdder(referencees));
    referenceCollector.addPreorderVisitor(new PossibleTargetAdder(referencees));

    VisitExecutorImplementation executor = new VisitExecutorImplementation();
    executor.visit(referenceCollector, root);

    return referencees;
  }
}
