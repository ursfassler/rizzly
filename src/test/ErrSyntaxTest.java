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

import org.junit.Ignore;
import org.junit.Test;

public class ErrSyntaxTest extends ErrorTest {
  @Override
  protected String getRootdir() {
    return "error/syntax/";
  }

  @Test
  public void err1() {
    testForError("err1", "Err1", "Name not found: foo");
  }

  @Test
  public void err2() {
    testForError("err2", "Err2", "Wrong number of parameter, expected 1 got 0");
  }

  @Test
  public void err3() {
    testForError("err3", "Err3", "Expected name, got keyword R");
  }

  @Test
  public void err4() {
    testForError("err4", "Err4", "Name not found: C");
  }

  @Test
  public void err5() {
    testForError("err5", "Err5", "Expected value, got type");
  }

  @Test
  @Ignore
  public void err6() {
    testForError("err6", "Err6", "");
  }
}
