package evl.expression;

import error.ErrorType;
import error.RError;

/**
 * 
 * @author urs
 */
public enum ExpOp {

  PLUS, MINUS, MUL, DIV, OR, MOD, AND, SHR, SHL;

  public String toString() {
    switch (this) {
    case PLUS:
      return "+";
    case MINUS:
      return "-";
    case MUL:
      return "*";
    case DIV:
      return "/";
    case OR:
      return " or ";
    case MOD:
      return " mod ";
    case AND:
      return " and ";
    case SHL:
      return " shl ";
    case SHR:
      return " shr ";
    default:
      RError.err(ErrorType.Fatal, "not supported yet: " + this.ordinal());
      return null;
    }
  }

}
