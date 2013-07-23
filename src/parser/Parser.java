package parser;

import java.util.ArrayList;
import java.util.List;

import error.ErrorType;
import error.RError;

/**
 * 
 * @author urs
 */
public abstract class Parser {
  private PeekReader<Token> scanner;

  public Parser(PeekReader<Token> scanner) {
    this.scanner = scanner;
  }

  protected PeekReader<Token> getScanner() {
    return scanner;
  }

  protected Token peek() {
    return scanner.peek();
  }

  protected Token next() {
    return scanner.next();
  }

  protected boolean hasNext() {
    return scanner.hasNext();
  }

  // EBNF designator: id { "." id }
  protected List<String> parseDesignator() {
    ArrayList<String> name = new ArrayList<String>();
    do {
      Token tok = expect(TokenType.IDENTIFIER);
      name.add(tok.getData());
    } while (consumeIfEqual(TokenType.PERIOD));
    return name;
  }

  protected boolean isLhsOrUse() {
    switch (peek().getType()) {
    case IDENTIFIER:
      return true;
    default:
      return false;
    }
  }

  protected boolean consumeIfEqual(TokenType tok) {
    if (scanner.peek().getType() == tok) {
      scanner.next();
      return true;
    } else {
      return false;
    }
  }

  protected Token expect(TokenType type) {
    if (!scanner.hasNext()) {
      Token tok = scanner.peek();
      RError.err(ErrorType.Error, tok.getInfo(), "expected token not found: " + tok);
    }
    Token got = scanner.next();
    if (got.getType() != type) {
      RError.err(ErrorType.Error, got.getInfo(), "expected " + type + " got " + got);
    }
    return got;
  }

  protected void wrongToken(TokenType type) {
    if (!scanner.hasNext()) {
      Token tok = scanner.peek();
      RError.err(ErrorType.Error, tok.getInfo(), "expected token not found: " + tok);
    }
    Token got = scanner.next();
    RError.err(ErrorType.Error, got.getInfo(), "expected " + type + " got " + got);
  }

}
