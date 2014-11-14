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

package joGraph;

import java.io.PrintStream;

public class Writer {
  private PrintStream stream;
  private int indent = 0;
  private boolean didIndented = false;

  public Writer(PrintStream stream) {
    this.stream = stream;
  }

  public void wrln(Object msg) {
    wr(msg);
    nl();
  }

  public void wr(Object msg) {
    if (!didIndented) {
      doIndent();
      didIndented = true;
    }
    stream.print(msg);
  }

  public void nl() {
    stream.println();
    didIndented = false;
  }

  private void doIndent() {
    for (int i = 0; i < indent; i++) {
      stream.print("  ");
    }
  }

  public void incIndent() {
    indent++;
  }

  public void decIndent() {
    indent--;
  }

  public int getIndent() {
    return indent;
  }

}
