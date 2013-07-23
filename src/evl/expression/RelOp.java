package evl.expression;

import error.ErrorType;
import error.RError;

/**
 *
 * @author urs
 */
public enum RelOp {

  EQUAL,
  NOT_EQUAL,
  LESS,
  LESS_EQUAL,
  GREATER,
  GREATER_EQUEAL;

  public String toString() {
    switch( this ) {
      case EQUAL:
        return "=";
      case NOT_EQUAL:
        return "<>";
      case LESS:
        return "<";
      case LESS_EQUAL:
        return "<=";
      case GREATER:
        return ">";
      case GREATER_EQUEAL:
        return ">=";
      default:
        RError.err(ErrorType.Fatal, "not supported yet");
        return null;
    }
  }
}
