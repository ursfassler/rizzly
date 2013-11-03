package metadata.parser;

import parser.PeekReader;

import common.ElementInfo;

import error.ErrorType;
import error.RError;

/**
 * 
 * @author urs
 */
public class Scanner implements PeekReader<Token> {
  private MetadataReader reader;
  private Token next;

  public Scanner(MetadataReader reader) {
    this.reader = reader;
    next();
  }

  private Token token(TokenType value, ElementInfo info) {
    return new Token(value, info);
  }

  private Token token(TokenType value, String id, ElementInfo info) {
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
    info = new ElementInfo("", 0, 0); // FIXME use correct filename
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
    ElementInfo info = reader.getInfo();
    Character sym = reader.next();
    switch (sym) {
    case ' ':
    case '\t':
    case 13:
    case '\n':
      return token(TokenType.IGNORE, info);
    case '=':
      return token(TokenType.EQUAL, info);
    case '\"':
      return read_22(info);
    default:
      if (isAlphaNummeric(sym)) {
        String id = readIdentifier(Character.toString(sym));
        TokenType type;
        type = TokenType.IDENTIFIER;
        Token toc = token(type, id, info);
        return toc;
      } else {
        RError.err(ErrorType.Error, info, "Unexpected character: #" + Integer.toHexString((int) sym) + " (" + sym + ")");
        return specialToken(TokenType.IGNORE);
      }
    }
  }

  // EBNF id: alpha { alpha | numeric}
  private String readIdentifier(String prefix) {
    String text = prefix;
    while (isAlphaNummeric(reader.peek())) {
      text = text + reader.next();
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
  private Token read_22(ElementInfo sym) {
    String value = readTilEndString();
    return token(TokenType.STRING, value, sym);
  }

  private String readTilEndString() {
    String ret = "";
    char sym;
    while (reader.hasNext()) {
      sym = reader.next();
      if (sym == '\"') {
        return ret;
      } else {
        ret += sym;
      }
    }
    RError.err(ErrorType.Error, reader.getInfo(), "String over end of file");
    return null;
  }

}
