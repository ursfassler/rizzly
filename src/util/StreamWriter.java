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

package util;

import java.io.PrintStream;

public class StreamWriter implements Writer {
  private PrintStream stream;
  private int indent = 0;
  private boolean spaceAdded = false;

  public StreamWriter(PrintStream stream) {
    super();
    this.stream = stream;
  }

  @Override
  public void wr(String s) {
    if (!spaceAdded) {
      doIndent();
      spaceAdded = true;
    }
    stream.print(s);
  }

  @Override
  public void nl() {
    stream.println();
    spaceAdded = false;
  }

  @Override
  public void sectionSeparator() {
    nl();
    nl(); // FIXME make it correct
  }

  private void doIndent() {
    for (int i = 0; i < indent; i++) {
      stream.print("  ");
    }
  }

  @Override
  public void incIndent() {
    indent++;
  }

  @Override
  public void decIndent() {
    indent--;
  }

  @Override
  public void kw(String name) {
    wr(name);
  }

  @Override
  public void wc(String text) {
    wr(text);
  }

  @Override
  public void wl(String text, String hint, String file, String id) {
    wr(text);
    wr("[");
    wr(id);
    wr("]");
  }

  @Override
  public void wl(String text, String hint, String file) {
    wr(text);
  }

  @Override
  public void wa(String name, String id) {
    wr(name);
    wr("[");
    wr(id);
    wr("]");
  }

}
