package cir.expression;

import error.ErrorType;
import error.RError;

public enum Op {
  PLUS, MINUS, MUL, DIV, BITOR, BITAND, LOCOR, LOCAND, MOD,

  EQUAL, NOT_EQUAL, LESS, LESS_EQUAL, GREATER, GREATER_EQUEAL,

  LOCNOT, BITNOT,

  SHL, SHR;

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
      case BITOR:
        return "|";
      case LOCOR:
        return "||";
      case BITAND:
        return "&";
      case LOCAND:
        return "&&";
      case MOD:
        return "%";
      case SHL:
        return "<<";
      case SHR:
        return ">>";
      case EQUAL:
        return "==";
      case NOT_EQUAL:
        return "!=";
      case LESS:
        return "<";
      case LESS_EQUAL:
        return "<=";
      case GREATER:
        return ">";
      case GREATER_EQUEAL:
        return ">=";
      case BITNOT:
        return "~";
      case LOCNOT:
        return "!";
      default:
        RError.err(ErrorType.Fatal, "not supported yet: " + this.ordinal());
        return null;
    }
  }

}
