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

package ast.specification.visitor;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import org.junit.Test;
import org.mockito.Mockito;

import ast.data.Named;
import ast.data.reference.LinkedReferenceWithOffset_Implementation;
import ast.data.reference.UnlinkedReferenceWithOffset_Implementation;
import ast.data.statement.Block;
import ast.data.variable.FunctionVariable;
import ast.data.variable.StateVariable;
import ast.meta.MetaList;
import ast.visitor.VisitExecutor;
import error.ErrorType;
import error.RizzlyError;

public class IsStateVariable_Test {
  final private VisitExecutor executor = mock(VisitExecutor.class);
  final private RizzlyError error = mock(RizzlyError.class);
  final private IsStateVariable testee = new IsStateVariable(executor, error);

  final private StateVariable stateVariable = mock(StateVariable.class);
  final private FunctionVariable functionVariable = mock(FunctionVariable.class);
  final private Block block = mock(Block.class);
  final private LinkedReferenceWithOffset_Implementation linkedReference = mock(LinkedReferenceWithOffset_Implementation.class);
  final private UnlinkedReferenceWithOffset_Implementation unlinkedReference = mock(UnlinkedReferenceWithOffset_Implementation.class);

  @Test
  public void is_false_by_default() {
    assertFalse(testee.isState());
  }

  @Test
  public void returns_true_for_state_variables() {
    testee.visit(stateVariable);

    assertTrue(testee.isState());
  }

  @Test
  public void forwards_the_request_to_linked_for_linked_references() {
    Named link = mock(Named.class);
    Mockito.when(linkedReference.getLink()).thenReturn(link);

    testee.visit(linkedReference);

    verify(executor).visit(testee, link);
  }

  @Test
  public void does_not_support_unlinked_references() {
    testee.visit(unlinkedReference);

    verify(error).err(eq(ErrorType.Fatal), anyString(), any(MetaList.class));
  }
}
