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

public class RException extends RuntimeException {
  private static final long serialVersionUID = 3964100276539972712L;

  final private ErrorType type;
  final private String filename;
  final private int line;
  final private int col;
  final private String msg;

  public RException(ErrorType type, String filename, int line, int col, String msg) {
    super(mktxt(type, filename, line, col, msg));
    this.type = type;
    this.filename = filename;
    this.line = line;
    this.col = col;
    this.msg = msg;
  }

  public static String mktxt(ErrorType type, String filename, int line, int col, String msg) {
    return filename + ":" + line + ":" + col + ": " + type + ": " + msg;
  }

  public ErrorType getType() {
    return type;
  }

  public String getFilename() {
    return filename;
  }

  public int getLine() {
    return line;
  }

  public int getCol() {
    return col;
  }

  public String getMsg() {
    return msg;
  }

}
