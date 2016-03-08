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
import static org.mockito.Matchers.anyString;
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
import ast.pass.others.DefaultVisitorPass;
import ast.pass.others.FileLoader;
import ast.pass.others.InternsAdder;
import ast.pass.output.xml.XmlWriterPass;
import ast.pass.reduction.MetadataRemover;
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

  @Test(expected = RuntimeException.class)
  public void raises_exception_for_wrong_number_of_arguments() {
    when(argumentParser.parse("xmlreader()")).thenReturn(list("xmlreader"));

    testee.produce("xmlreader()");
  }

  @Test
  public void reports_an_error_for_wrong_number_of_arguments() {
    when(argumentParser.parse("xmlreader()")).thenReturn(list("xmlreader"));

    try {
      testee.produce("xmlreader()");
    } catch (Exception e) {
    }

    verify(error).err(eq(ErrorType.Error), eq("pass xmlreader expected 1 argument but 0 provided"), any(MetaList.class));
  }

  @Test(expected = RuntimeException.class)
  public void raises_exception_when_no_pass_name_is_returned() {
    when(argumentParser.parse(anyString())).thenReturn(list());

    testee.produce("xmlreader()");
  }

  @Test
  public void reports_an_error_when_no_pass_name_is_returned() {
    when(argumentParser.parse(anyString())).thenReturn(list());

    try {
      testee.produce("xmlreader()");
    } catch (Exception e) {
    }

    verify(error).err(eq(ErrorType.Error), eq("could not parse pass definition: xmlreader()"), any(MetaList.class));
  }

  @Test
  public void has_a_pass_xmlreader() {
    when(argumentParser.parse("xmlreader('filename.xml')")).thenReturn(list("xmlreader", "filename.xml"));

    Assert.assertTrue(testee.produce("xmlreader('filename.xml')") instanceof XmlParserPass);
  }

  @Test
  public void has_a_pass_xmlwriter() {
    when(argumentParser.parse("xmlwriter('filename.xml')")).thenReturn(list("xmlwriter", "filename.xml"));

    Assert.assertTrue(testee.produce("xmlwriter('filename.xml')") instanceof XmlWriterPass);
  }

  @Test
  public void has_a_pass_internsAdder() {
    when(argumentParser.parse("internsAdder")).thenReturn(list("internsAdder"));

    Assert.assertTrue(testee.produce("internsAdder") instanceof InternsAdder);
  }

  @Test
  public void has_a_pass_linker() {
    when(argumentParser.parse("linker")).thenReturn(list("linker"));

    Assert.assertTrue(testee.produce("linker") instanceof Linker);
  }

  @Test
  public void has_a_pass_rzyreader() {
    when(argumentParser.parse("rzyreader('path/to/project', 'root.module')")).thenReturn(list("rzyreader", "path/to/project", "root.module"));

    Assert.assertTrue(testee.produce("rzyreader('path/to/project', 'root.module')") instanceof FileLoader);
  }

  @Test
  public void has_a_pass_metadataremover() {
    when(argumentParser.parse("metadataremover")).thenReturn(list("metadataremover"));

    AstPass pass = testee.produce("metadataremover");

    Assert.assertTrue(pass instanceof DefaultVisitorPass);
    Assert.assertTrue(((DefaultVisitorPass) pass).getVisitor() instanceof MetadataRemover);
  }

  private LinkedList<String> list() {
    return new LinkedList<String>();
  }

  private LinkedList<String> list(String arg0) {
    LinkedList<String> list = list();
    list.add(arg0);
    return list;
  }

  private LinkedList<String> list(String arg0, String arg1) {
    LinkedList<String> list = list(arg0);
    list.add(arg1);
    return list;
  }

  private LinkedList<String> list(String arg0, String arg1, String arg2) {
    LinkedList<String> list = list(arg0, arg1);
    list.add(arg2);
    return list;
  }
}
