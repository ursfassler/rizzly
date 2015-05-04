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

public class ErrIoTest extends ErrorTest {
  @Override
  protected String getRootdir() {
    return "error/iocheck/";
  }

  @Test
  public void err1() {
    testForError("err1", "Err1", "response (what) is not allowed to change state");
  }

  @Test
  public void err2() {
    testForError("err2", "Err2", "response (what) is not allowed to change state");
  }

  @Test
  public void err3() {
    testForError("err3", "Err3", "response (what) is not allowed to change state");
  }

  @Test
  public void err4() {
    testForError("err4", "Err4", "response (what) is not allowed to change state");
  }

  @Test
  public void err5() {
    testForError("err5", "Err5", "response (what) is not allowed to change state");
  }

  @Test
  public void err6() {
    testForError("err6", "Err6", "response (what) is not allowed to change state");
  }

  @Test
  public void err7() {
    testForError("err7", "Err7", "function (foo) is not allowed to change state");
  }

  @Test
  public void err8() {
    testForError("err8", "Err8", "function (foo) is not allowed to change state");
  }

}
