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

public class ArrayTest extends BaseTest {

  @Override
  protected String getRootdir() {
    return "array/";
  }

  @Test
  public void arrayTest4() {
    compile("arrayTest4", TestSteps.EXECUTE, false, false);
  }

  @Test
  public void arrayTest5() {
    compile("arrayTest5", TestSteps.EXECUTE, false, false);
  }

  @Test
  public void arrayTest6() {
    compile("arrayTest6", TestSteps.EXECUTE, false, false);
  }

  @Test
  public void arrayTest7() {
    compile("arrayTest7", TestSteps.COMPILE_TO_LIB, false, false);
  }

  @Test
  public void reverse() {
    compile("reverse", TestSteps.EXECUTE, false, false);
  }

}
