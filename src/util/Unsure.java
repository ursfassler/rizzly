package util;

public enum Unsure {
  DontKnow, True, False;

  public static Unsure fromBool(boolean value) {
    if (value) {
      return True;
    } else {
      return False;
    }
  }

  public static Unsure equal(Unsure left, Unsure right) {
    if ((left == DontKnow) || (right == DontKnow)) {
      return DontKnow;
    }
    return fromBool(left == right);
  }

  public static Unsure notequal(Unsure left, Unsure right) {
    if ((left == DontKnow) || (right == DontKnow)) {
      return DontKnow;
    }
    return fromBool(left != right);
  }

  public static Unsure not(Unsure value) {
    if (value == DontKnow) {
      return DontKnow;
    }
    return fromBool(value == True);
  }

  public static Unsure and(Unsure left, Unsure right) {
    if ((left == DontKnow) || (right == DontKnow)) {
      return DontKnow;
    }
    return fromBool((left == True) && (right == True));
  }
}
