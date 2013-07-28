package metadata.parser;

import parser.PeekReader;
import parser.Symbol;

import common.ElementInfo;

import error.ErrorType;
import error.RError;

/**
 *
 * @author urs
 */
public class Scanner implements PeekReader<Token> {
  private StringReader reader;
  private Token next;
  private String source;

  public Scanner(StringReader reader, String source) {
    this.reader = reader;
    this.source = source;
    next();
  }

  private Token token(TokenType value, Symbol sym) {
    return new Token(value, new ElementInfo(source, sym.line, sym.row));
  }

  private Token token(TokenType value, String id, Symbol sym) {
    ElementInfo info = new ElementInfo(source, sym.line, sym.row);
    return new Token(value, id, info);
  }

  public boolean hasNext() {
    return peek() != null;
  }

  public Token peek() {
    return next;
  }

  private Token specialToken(TokenType value) {
    ElementInfo info;
    info = new ElementInfo(source, 0, 0);
    return new Token(value, info);
  }

  public Token next() {
    Token res = next;

    do {
      if (!reader.hasNext()) {
        next = specialToken(TokenType.EOF);
        break;
      }
      next = getNext();
    } while (next.getType() == TokenType.IGNORE);
    return res;
  }

  private Token getNext() {
    if (!reader.hasNext()) {
      return specialToken(TokenType.EOF);
    }
    Symbol sym = reader.next();
    switch (sym.sym) {
    case ' ':
    case '\t':
    case 13:
    case '\n':
      return token(TokenType.IGNORE, sym);
    case '=':
      return token(TokenType.EQUAL, sym);
    case '\"':
      return read_22(sym);
    default:
      if (isAlphaNummeric(sym.sym)) {
        String id = readIdentifier(Character.toString(sym.sym));
        TokenType type;
        type = TokenType.IDENTIFIER;
        Token toc = token(type, id, sym);
        return toc;
      } else {
        RError.err(ErrorType.Error, source, sym.line, sym.row, "Unexpected character: #" + Integer.toHexString((int) sym.sym) + " (" + sym.sym + ")");
        return specialToken(TokenType.IGNORE);
      }
    }
  }

  // EBNF id: alpha { alpha | numeric}
  private String readIdentifier(String prefix) {
    String text = prefix;
    while (isAlphaNummeric(reader.peek().sym)) {
      text = text + reader.next().sym;
    }
    return text;
  }

  // EBNF alpha: "a".."z" | "A".."Z"
  private boolean isAlpha(char sym) {
    return (sym >= 'a' && sym <= 'z') || (sym >= 'A' && sym <= 'Z') || (sym == '_');
  }

  // EBNF numeric: "0".."9"
  private boolean isNummeric(char sym) {
    return (sym >= '0' && sym <= '9');
  }

  private boolean isAlphaNummeric(char sym) {
    return isAlpha(sym) || isNummeric(sym);
  }

  // "
  private Token read_22(Symbol sym) {
    String value = readTilEndString();
    return token(TokenType.STRING, value, sym);
  }

  private String readTilEndString() {
    String ret = "";
    Symbol sym;
    do {
      sym = reader.next();
      if (sym.sym == '\"') {
        return ret;
      } else {
        ret += sym.sym;
      }
    } while (reader.hasNext());
    RError.err(ErrorType.Error, source, sym.line, sym.row, "String over end of file");
    return null;
  }

}
