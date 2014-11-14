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

public class ErrRtcTest extends ErrorTest {
  @Override
  protected String getRootdir() {
    return "error/rtc/";
  }

  @Test
  public void con1() {
    testForError("con1", "Con1", "Violation of run to completion detected");
  }

  @Test
  public void con2() {
    testForError("con2", "Con2", "Violation of run to completion detected");
  }

}
