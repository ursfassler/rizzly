package common;

public class ElementInfo {
  private String filename;
  private int line;
  private int row;

  public ElementInfo(String filename, int line, int row) {
    super();
    this.filename = filename;
    this.line = line;
    this.row = row;
  }

  public ElementInfo() {
    this.filename = "";
    this.line = 0;
    this.row = 0;
  }

  public String getFilename() {
    return filename;
  }

  public int getLine() {
    return line;
  }

  public int getRow() {
    return row;
  }

  @Override
  public String toString() {
    return filename + ": " + line + "," + row;
  }

}
