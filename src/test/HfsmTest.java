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

public class HfsmTest extends BaseTest {

  @Override
  protected String getRootdir() {
    return "hfsm/";
  }

  @Test
  public void hfsm1() {
    compile("hfsm1", TestSteps.EXECUTE, false, false);
  }

  @Test
  public void transOrder() {
    compile("transOrder", TestSteps.EXECUTE, false, false);
  }

  @Test
  public void query() {
    compile("query", TestSteps.EXECUTE, false, false);
  }

  @Test
  public void query2() {
    compile("query2", TestSteps.COMPILE_TO_LIB, false, false);
  }

  @Test
  public void query3() {
    compile("query3", TestSteps.EXECUTE, false, false);
  }

  @Test
  public void query4() {
    compile("query4", TestSteps.EXECUTE, false, false);
  }

  @Test
  public void unusedState() {
    compile("unusedState", TestSteps.COMPILE_TO_C, false, false); // TODO check
                                                                  // for
                                                                  // warnings
  }

  @Test
  public void stateVariable2() {
    compile("stateVariable2", TestSteps.COMPILE_TO_LIB, false, false);
  }

  @Test
  public void transScope() {
    compile("transScope", TestSteps.EXECUTE, false, false);
  }

  @Test
  public void stateNames1() {
    compile("stateNames1", TestSteps.COMPILE_TO_EVL, false, false);
  }
}
