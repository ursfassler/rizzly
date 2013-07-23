package evl.expression;

import error.ErrorType;
import error.RError;

public enum UnaryOp {
  MINUS, NOT;

  public String toString() {
    switch (this) {
    case MINUS:
      return "-";
    case NOT:
      return "not";
    default:
      RError.err(ErrorType.Fatal, "not supported yet: " + this.ordinal());
      return null;
    }
  }

}
