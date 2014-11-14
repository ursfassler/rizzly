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

public class ErrIfaceTest extends ErrorTest {
  @Override
  protected String getRootdir() {
    return "error/iface/";
  }

  @Test
  public void err2() {
    testForError("err2", "Err2", "can not connect from input");
  }

  @Test
  public void err3() {
    testForError("err3", "Err3", "Interface funcA not connected");
  }

  @Test
  public void err4() {
    testForError("err4", "Err4", "Interface not found: a.funcC");
  }

  @Test
  public void err7() {
    testForError("err7", "Err7", "Interface a.funcA not connected");
  }

  @Test
  public void errHfsm2() {
    testForError("errHfsm2", "ErrHfsm2", "Name not found: funcC");
  }

  @Test
  public void errHfsm3() {
    testForError("errHfsm3", "ErrHfsm3", "transition can only be triggered by slot");
  }

}
