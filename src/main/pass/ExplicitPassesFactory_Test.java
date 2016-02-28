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

package main.pass;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.LinkedList;

import org.junit.Assert;
import org.junit.Test;

import ast.meta.MetaList;
import ast.pass.AstPass;
import ast.pass.input.xml.XmlParserPass;
import ast.pass.linker.Linker;
import error.ErrorType;
import error.RizzlyError;

public class ExplicitPassesFactory_Test {
  private final PassArgumentParser argumentParser = mock(PassArgumentParser.class);
  private final RizzlyError error = mock(RizzlyError.class);
  private final ExplicitPassesFactory testee = new ExplicitPassesFactory(argumentParser, error);

  @Test(expected = RuntimeException.class)
  public void raises_exception_for_unknown_pass() {
    testee.produce("not a pass");
  }

  @Test
  public void reports_an_error_for_unknown_pass() {
    when(argumentParser.parse("not a pass")).thenReturn(list("not a pass"));

    try {
      testee.produce("not a pass");
    } catch (Exception e) {
    }

    verify(error).err(eq(ErrorType.Error), eq("pass not found: not a pass"), any(MetaList.class));
  }

  @Test
  public void create_the_linker_pass() {
    when(argumentParser.parse("linker")).thenReturn(list("linker"));

    AstPass pass = testee.produce("linker");

    Assert.assertNotNull(pass);
    Assert.assertTrue(pass instanceof Linker);
  }

  @Test
  public void create_the_xmlreader_pass() {
    when(argumentParser.parse("xmlreader('the filename')")).thenReturn(list("xmlreader", "the filename"));

    AstPass pass = testee.produce("xmlreader('the filename')");

    Assert.assertNotNull(pass);
    Assert.assertTrue(pass instanceof XmlParserPass);
    Assert.assertEquals("the filename", ((XmlParserPass) pass).getFilename());
  }

  private LinkedList<String> list(String arg0) {
    LinkedList<String> list = new LinkedList<String>();
    list.add(arg0);
    return list;
  }

  private LinkedList<String> list(String arg0, String arg1) {
    LinkedList<String> list = list(arg0);
    list.add(arg1);
    return list;
  }
}
