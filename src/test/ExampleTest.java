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

package test;

import org.junit.Test;

public class ExampleTest extends BaseTest {

  public ExampleTest() {
    super();
    setStrict(false);
  }

  @Override
  protected String getRootdir() {
    return "../example/";
  }

  @Test
  public void motor() {
    compileExample("motor/", "main.Controller");
  }

  @Test
  public void alarmclock() {
    compileExample("alarmclock/", "clock.Clock");
  }

  @Test
  public void helloWorld() {
    // compileExample("helloWorld/", "helloWorld.HelloWorld");
    compileExample("helloWorld/", "world.World");
  }

}