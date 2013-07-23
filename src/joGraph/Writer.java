/*
 * Part of upcompiler. Copyright (c) 2012, Urs Fässler, Licensed under the GNU Genera Public License, v3
 * @author: urs@bitzgi.ch
 */

package joGraph;

import java.io.PrintStream;

public class Writer {
  private PrintStream stream;
  private int         indent      = 0;
  private boolean     didIndented = false;

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
