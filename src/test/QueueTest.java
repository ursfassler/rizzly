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

public class QueueTest extends BaseTest {

  public QueueTest() {
    super();
    setStrict(false);
  }

  @Override
  protected String getRootdir() {
    return "queued/";
  }

  @Test
  public void composition1() {
    compile("composition1", TestSteps.EXECUTE, true, false);
  }

  @Test
  public void simple() {
    compile("simple", TestSteps.COMPILE_TO_LIB, false, false);
  }

  @Test
  public void simple2() {
    compile("simple2", TestSteps.COMPILE_TO_LIB, false, false);
  }

  @Test
  public void simple3() {
    compile("simple3", TestSteps.EXECUTE, false, false);
  }
}
