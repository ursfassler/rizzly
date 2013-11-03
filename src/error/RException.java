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
