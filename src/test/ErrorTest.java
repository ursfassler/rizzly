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

import main.ClaOption;
import main.Main;

import org.junit.Assert;

import ast.Designator;
import error.RException;

public abstract class ErrorTest {

  protected abstract String getRootdir();

  protected void testForError(String file, String comp, String error) {
    try {
      ClaOption opt = new ClaOption();
      opt.init(getRootdir(), new Designator(file, comp), false, false);
      Main.compile(opt);
    } catch (RException err) {
      Assert.assertEquals(error, err.getMsg());
      return;
    }
    throw new RuntimeException("Error not thrown: " + error);
  }

}
