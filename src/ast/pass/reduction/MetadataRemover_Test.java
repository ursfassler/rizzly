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

package ast.pass.reduction;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Test;
import org.mockito.Mockito;

import ast.data.Ast;
import ast.meta.MetaList;
import ast.visitor.VisitExecutorImplementation;

public class MetadataRemover_Test {
  final private MetadataRemover testee = new MetadataRemover();
  final private Ast item = Mockito.mock(Ast.class);
  final private MetaList metadata = Mockito.mock(MetaList.class);
  final private VisitExecutorImplementation executor = new VisitExecutorImplementation();

  @Test
  public void clears_the_metadata() {
    when(item.metadata()).thenReturn(metadata);

    testee.visit(item);

    verify(metadata).clear();
  }

  @Test
  public void is_a_visitor() {
    when(item.metadata()).thenReturn(metadata);

    executor.visit(testee, item);

    verify(metadata).clear();
  }
}
