package common;

import java.util.HashMap;
import java.util.Map;

public class ElementInfo {
  private String filename;
  private int line;
  private int row;
  private Map<String, String> metadata = new HashMap<String, String>();

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

  public Map<String, String> getMetadata() {
    return new HashMap<String, String>(metadata);
  }

  public void addMetadata(Map<String, String> meta) {
    for (String key : meta.keySet()) {
      String data = meta.get(key);

      String value = metadata.get(key);
      if (value == null) {
        value = data;
      } else {
        value += " " + data;
      }
      metadata.put(key, value);
    }
  }

  @Override
  public String toString() {
    return filename + ": " + line + "," + row;
  }

}
