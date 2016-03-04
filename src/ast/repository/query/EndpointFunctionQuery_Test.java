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

package ast.repository.query;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.Test;
import org.mockito.Mockito;

import ast.data.component.Component;
import ast.data.component.composition.ComponentUse;
import ast.data.component.composition.EndpointRaw;
import ast.data.component.composition.EndpointSelf;
import ast.data.component.composition.EndpointSub;
import ast.data.function.Function;
import ast.data.function.InterfaceFunction;
import ast.data.reference.Reference;
import ast.repository.query.Referencees.TargetResolver;

public class EndpointFunctionQuery_Test {
  final private TargetResolver referenceTargetResolver = Mockito.mock(TargetResolver.class);
  final private EndpointFunctionQuery testee = new EndpointFunctionQuery(referenceTargetResolver);

  @Test
  public void has_no_function_by_default() {
    assertNull(testee.getFunction());
  }

  @Test
  public void can_handle_EndpointSelf() {
    Reference reference = mock(Reference.class);
    EndpointSelf endpoint = mock(EndpointSelf.class);
    Function function = mock(Function.class);
    when(referenceTargetResolver.targetOf(reference, Function.class)).thenReturn(function);
    when(endpoint.getFuncRef()).thenReturn(reference);

    testee.visit(endpoint);

    assertEquals("", testee.getInstanceName());
    assertEquals(null, testee.getComponentType());
    assertEquals(function, testee.getFunction());
  }

  @Test
  public void can_handle_EndpointRaw() {
    Reference reference = mock(Reference.class);
    EndpointRaw endpoint = mock(EndpointRaw.class);
    Function function = mock(Function.class);
    when(referenceTargetResolver.targetOf(reference, Function.class)).thenReturn(function);
    when(endpoint.getRef()).thenReturn(reference);

    testee.visit(endpoint);

    assertEquals("", testee.getInstanceName());
    assertEquals(null, testee.getComponentType());
    assertEquals(function, testee.getFunction());
  }

  // TODO simplify
  @Test
  public void can_handle_EndpointSub() {
    ComponentUse componentUse = mock(ComponentUse.class);
    Reference componentUseRef = mock(Reference.class);
    Reference componentRef = mock(Reference.class);
    InterfaceFunction function = mock(InterfaceFunction.class);
    Component component = new Component("") {
    };
    component.iface.add(function);
    EndpointSub endpoint = mock(EndpointSub.class);
    when(referenceTargetResolver.targetOf(componentUseRef, ComponentUse.class)).thenReturn(componentUse);
    when(endpoint.getComponent()).thenReturn(componentUseRef);
    when(componentUse.getCompRef()).thenReturn(componentRef);
    when(componentUse.getName()).thenReturn("the component instance");
    when(referenceTargetResolver.targetOf(componentRef, Component.class)).thenReturn(component);
    when(endpoint.getFunction()).thenReturn("functionName");
    when(function.getName()).thenReturn("functionName");

    testee.visit(endpoint);

    assertEquals("the component instance", testee.getInstanceName());
    assertEquals(component, testee.getComponentType());
    assertEquals(function, testee.getFunction());
  }
}
