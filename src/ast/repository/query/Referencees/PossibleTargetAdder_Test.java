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

import ast.data.Ast;

public class PossibleTargetAdder_Test {
  final private ReferenceesWriter referencees = mock(ReferenceesWriter.class);
  final private PossibleTargetAdder testee = new PossibleTargetAdder(referencees);

  @Test
  public void adds_visited_item() {
    Ast item = mock(Ast.class);

    testee.visit(item);

    verify(referencees).addTarget(item);
  }

}
