package common;

public class Metadata {
  final private ElementInfo info;
  final private String key;
  final private String value;

  public Metadata(ElementInfo info, String key, String value) {
    super();
    this.info = info;
    this.key = key;
    this.value = value;
  }

  public ElementInfo getInfo() {
    return info;
  }

  public String getKey() {
    return key;
  }

  public String getValue() {
    return value;
  }

  @Override
  public String toString() {
    return key + ":" + value;
  }

}
