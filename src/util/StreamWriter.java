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

  public void sectionSeparator() {
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
