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

package main;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import ast.Designator;
import ast.meta.MetaList;
import error.ErrorType;
import error.RizzlyError;

public class CommandLineParser_Test {
  private final RizzlyError error = mock(RizzlyError.class);
  private final CommandLineParser testee = new CommandLineParser(error);

  @Test
  public void has_help_option() {
    String[] args = { "--help" };

    Configuration configuration = testee.parse(args);

    Assert.assertNull(configuration);
    verify(error, never()).err(any(ErrorType.class), anyString(), any(MetaList.class));
  }

  @Test
  public void does_not_return_a_configuration_when_no_option_is_provided() {
    String[] args = new String[0];

    Configuration configuration = testee.parse(args);

    Assert.assertNull(configuration);
  }

  @Test
  public void does_print_an_error_message_when_no_option_is_provided() {
    String[] args = new String[0];

    testee.parse(args);

    verify(error).err(eq(ErrorType.Error), eq("Need a file"), any(MetaList.class));
  }

  @Test
  public void automatic_instantiation_needs_file_and_component() {
    String[] args = { "-c", "dummy" };

    testee.parse(args);

    verify(error).err(eq(ErrorType.Error), eq("Option 'component' needs file"), any(MetaList.class));
  }

  @Test
  public void provide_a_file_with_automatic_instantiation() {
    String[] args = { "test.rzy" };

    Configuration configuration = testee.parse(args);

    verify(error, never()).err(any(ErrorType.class), anyString(), any(MetaList.class));
    Assert.assertEquals(false, configuration.doDebugEvent());
    Assert.assertEquals(false, configuration.doLazyModelCheck());
    Assert.assertEquals(false, configuration.doDocOutput());
    Assert.assertEquals("test", configuration.getNamespace());
    Assert.assertEquals(new Designator("test", "Test"), configuration.getRootComp());
    Assert.assertEquals("./", configuration.getRootPath());
    Assert.assertEquals(null, configuration.passes());
  }

  @Test
  public void provide_a_rizzly_file_as_input() {
    String[] args = { "test.rzy" };

    Configuration configuration = testee.parse(args);

    Assert.assertEquals(PassBuilding.Automatic, configuration.passBuilding());
  }


  @Test
  public void raise_an_error_for_unknown_file_types() {
    String[] args = { "test.bla" };

    testee.parse(args);

    verify(error).err(eq(ErrorType.Error), eq("Unknown file type: test.bla"), any(MetaList.class));
  }

  @Test
  public void provide_a_file_in_a_relative_directory() {
    String[] args = { "dir/test.rzy" };

    Configuration configuration = testee.parse(args);

    Assert.assertEquals(new Designator("test", "Test"), configuration.getRootComp());
    Assert.assertEquals("dir/", configuration.getRootPath());
  }

  @Test
  public void provide_a_file_in_an_absolute_directory() {
    String[] args = { "/dir/test.rzy" };

    Configuration configuration = testee.parse(args);

    verify(error, never()).err(any(ErrorType.class), anyString(), any(MetaList.class));
    Assert.assertEquals(new Designator("test", "Test"), configuration.getRootComp());
    Assert.assertEquals("/dir/", configuration.getRootPath());
  }

  @Test
  public void provide_the_component_to_instantiate() {
    String[] args = { "dir/test.rzy", "-c", "TheComponent" };

    Configuration configuration = testee.parse(args);

    Assert.assertEquals(new Designator("TheComponent"), configuration.getRootComp());
    Assert.assertEquals("dir/", configuration.getRootPath());
  }

  @Test
  public void provide_documentation_flag() {
    String[] args = { "dir/test.rzy", "--doc" };

    Configuration configuration = testee.parse(args);

    Assert.assertTrue(configuration.doDocOutput());
  }

  @Test
  public void provide_debug_event_flag() {
    String[] args = { "dir/test.rzy", "--debugEvent" };

    Configuration configuration = testee.parse(args);

    Assert.assertTrue(configuration.doDebugEvent());
  }

  @Test
  public void provide_lazy_model_check_flag() {
    String[] args = { "dir/test.rzy", "--lazyModelCheck" };

    Configuration configuration = testee.parse(args);

    Assert.assertTrue(configuration.doLazyModelCheck());
  }

  @Test
  public void specify_passes() {
    String[] args = { "--passes", "a", "b", "c('the text')", "d" };

    Configuration configuration = testee.parse(args);

    Assert.assertEquals(PassBuilding.Specified, configuration.passBuilding());
    Assert.assertEquals(list("a", "b", "c('the text')", "d"), configuration.passes());
  }

  @Test
  public void no_other_option_is_allowed_when_passes_are_specified() {
    String[] args = { "--passes", "a", "--lazyModelCheck", "--doc" };

    Configuration configuration = testee.parse(args);

    Assert.assertNull(configuration);
    verify(error).err(eq(ErrorType.Error), eq("Invalid option found beside passes: --lazyModelCheck"), any(MetaList.class));
    verify(error).err(eq(ErrorType.Error), eq("Invalid option found beside passes: --doc"), any(MetaList.class));
  }

  private List<String> list(String string1, String string2, String string3, String string4) {
    List<String> list = new ArrayList<String>();

    list.add(string1);
    list.add(string2);
    list.add(string3);
    list.add(string4);

    return list;
  }
}
