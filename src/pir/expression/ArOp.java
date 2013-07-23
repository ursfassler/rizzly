package pir.expression;

import error.ErrorType;
import error.RError;

public enum ArOp {
  PLUS, MINUS, MUL, DIV, OR, AND, MOD, SHL, SHR;

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
    case AND:
      return " and ";
    case MOD:
      return " mod ";
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
