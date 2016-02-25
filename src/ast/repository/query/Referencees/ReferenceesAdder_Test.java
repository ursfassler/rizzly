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

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import org.junit.Test;

import ast.data.reference.LinkedAnchor;
import ast.visitor.VisitExecutorImplementation;

public class ReferenceesAdder_Test {
  final private ReferenceesWriter referencees = mock(ReferenceesWriter.class);
  final private ReferenceesAdder testee = new ReferenceesAdder(referencees);

  @Test
  public void adds_visited_LinkedAnchor() {
    LinkedAnchor anchor = mock(LinkedAnchor.class);

    testee.visit(anchor);

    verify(referencees).addReferencee(anchor);
  }

  @Test
  public void works_with_a_visitor() {
    VisitExecutorImplementation executor = new VisitExecutorImplementation();
    LinkedAnchor anchor = mock(LinkedAnchor.class);

    executor.visit(testee, anchor);

    verify(referencees).addReferencee(anchor);
  }
}
