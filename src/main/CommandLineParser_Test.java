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

import org.junit.Assert;
import org.junit.Test;

import ast.Designator;
import ast.ElementInfo;
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
    verify(error, never()).err(any(ErrorType.class), any(ElementInfo.class), anyString());
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

    verify(error).err(eq(ErrorType.Error), eq(ElementInfo.NO), eq("Need a file"));
  }

  @Test
  public void automatic_instantiation_needs_file_and_component() {
    String[] args = { "-c", "dummy" };

    testee.parse(args);

    verify(error).err(eq(ErrorType.Error), eq(ElementInfo.NO), eq("Option 'component' needs file"));
  }

  @Test
  public void provide_a_file_with_automatic_instantiation() {
    String[] args = { "test.rzy" };

    Configuration configuration = testee.parse(args);

    verify(error, never()).err(any(ErrorType.class), any(ElementInfo.class), anyString());
    Assert.assertEquals(false, configuration.doDebugEvent());
    Assert.assertEquals(false, configuration.doLazyModelCheck());
    Assert.assertEquals(false, configuration.doDocOutput());
    Assert.assertEquals(new Designator("test", "Test"), configuration.getRootComp());
    Assert.assertEquals("./", configuration.getRootPath());
    Assert.assertEquals(".rzy", configuration.getExtension());
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

    verify(error, never()).err(any(ErrorType.class), any(ElementInfo.class), anyString());
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
}
