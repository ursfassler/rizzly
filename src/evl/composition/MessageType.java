package evl.composition;

public enum MessageType {
  sync, async;

  @Override
  public String toString() {
    switch (this) {
    case sync:
      return "->";
    case async:
      return ">>";
    default:
      throw new RuntimeException("Not yet implemented: " + this.ordinal());
    }
  }

}
