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

package error;

import ast.ElementInfo;

//TODO introduce enum with entry for every error
//TODO make errors language independent (see above)
//TODO don't directly throw errors, let user ask if an fatal error occurred in order to finish a check

/**
 *
 * @author urs
 */
// TODO do not use static methods anymore
public class RError {

  // TODO use better solution
  public static RizzlyError instance() {
    return new RizzlyError() {
      @Override
      public void err(ErrorType type, String filename, int line, int col, String msg) {
        RError.err(type, filename, line, col, msg);
      }

      @Override
      public void err(ErrorType type, ElementInfo info, String msg) {
        err(type, info.filename, info.line, info.row, msg);
      }
    };
  }

  public static void err(ErrorType type, String filename, int line, int col, String msg) {
    switch (type) {
      case Hint:
      case Warning: {
        System.err.println(RException.mktxt(type, filename, line, col, msg));
        break;
      }
      case Error:
      case Fatal:
      case Assertion: {
        throw new RException(type, filename, line, col, msg);
      }
    }
  }

  public static void err(ErrorType error, ElementInfo info, String string) {
    err(error, info.filename, info.line, info.row, string);
  }

  public static void err(ErrorType error, String string) {
    err(error, "", -1, -1, string);
  }

  public static void ass(boolean condition, ElementInfo info, String msg) {
    if (!condition) {
      err(ErrorType.Assertion, info, msg);
    }
  }

  public static void ass(boolean condition, ElementInfo info) {
    if (!condition) {
      err(ErrorType.Assertion, info, "");
    }
  }

  public static void ass(boolean condition, String msg) {
    if (!condition) {
      err(ErrorType.Assertion, ElementInfo.NO, msg);
    }
  }
}
