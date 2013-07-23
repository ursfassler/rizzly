package evl.doc;

import java.io.PrintStream;

public class StreamWriter implements Writer {
  private PrintStream stream;
  private int indent = 0;
  private boolean spaceAdded = false;

  public StreamWriter(PrintStream stream) {
    super();
    this.stream = stream;
  }

  public void wr(String s) {
    if (!spaceAdded) {
      doIndent();
      spaceAdded = true;
    }
    stream.print(s);
  }

  public void nl() {
    stream.println();
    spaceAdded = false;
  }

  public void emptyLine() {
    nl();
    nl(); // FIXME make it correct
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

}
