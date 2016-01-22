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

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import main.Configuration;

import org.junit.Test;
import org.mockito.Mockito;

import ast.data.AstList;
import ast.data.Namespace;
import ast.data.file.RizzlyFile;
import ast.data.function.FunctionProperty;
import ast.data.function.header.FuncFunction;
import ast.data.function.ret.FuncReturnNone;
import ast.data.statement.Block;
import ast.data.variable.FunctionVariable;

public class MainFileFunctionPublisher_Test {
  final private Configuration configuration = mock(Configuration.class);
  final private MainFileFunctionPublisher testee = new MainFileFunctionPublisher(configuration);

  @Test
  public void changes_the_functions_to_public() {
    Mockito.when(configuration.getNamespace()).thenReturn("the main file");

    FuncFunction functionInMainFile = new FuncFunction("", new AstList<FunctionVariable>(), new FuncReturnNone(), new Block());
    functionInMainFile.property = FunctionProperty.Private;
    FuncFunction functionInOtherFile = new FuncFunction("", new AstList<FunctionVariable>(), new FuncReturnNone(), new Block());
    functionInOtherFile.property = FunctionProperty.Private;

    RizzlyFile mainFile = new RizzlyFile("the main file");
    mainFile.objects.add(functionInMainFile);
    RizzlyFile otherFile = new RizzlyFile("some other file");
    otherFile.objects.add(functionInOtherFile);

    Namespace namespace = new Namespace("top");
    namespace.children.add(mainFile);
    namespace.children.add(otherFile);

    testee.process(namespace, null);

    assertEquals(FunctionProperty.Public, functionInMainFile.property);
    assertEquals(FunctionProperty.Private, functionInOtherFile.property);
  }

}
