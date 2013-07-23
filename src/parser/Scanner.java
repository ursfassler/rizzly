package parser;

import java.util.HashMap;
import java.util.Map;

import common.ElementInfo;

import error.ErrorType;
import error.RError;

/**
 *
 * @author urs
 */
public class Scanner implements PeekReader<Token> {
  private static final Map<String, TokenType> keywords = new HashMap<String, TokenType>();
  private PeekReader<Symbol> reader;
  private Token next;
  private String source;

  {
    keywords.put("interface", TokenType.INTERFACE);
    keywords.put("component", TokenType.COMPONENT);
    keywords.put("connection", TokenType.CONNECTION);
    keywords.put("implementation", TokenType.IMPLEMENTATION);
    keywords.put("elementary", TokenType.ELEMENTARY);
    keywords.put("composition", TokenType.COMPOSITION);
    keywords.put("hfsm", TokenType.HFSM);
    keywords.put("transition", TokenType.TRANSITION);
    keywords.put("state", TokenType.STATE);
    keywords.put("to", TokenType.TO);
    keywords.put("by", TokenType.BY);
    keywords.put("entry", TokenType.ENTRY);
    keywords.put("exit", TokenType.EXIT);
    keywords.put("end", TokenType.END);
    keywords.put("var", TokenType.VAR);
    keywords.put("const", TokenType.CONST);
    keywords.put("function", TokenType.FUNCTION);
    keywords.put("type", TokenType.TYPE_SEC);
    keywords.put("Record", TokenType.RECORD);
    keywords.put("Union", TokenType.UNION);
    keywords.put("Enum", TokenType.ENUM);
    keywords.put("return", TokenType.RETURN);
    keywords.put("import", TokenType.IMPORT);
    keywords.put("input", TokenType.INPUT);
    keywords.put("output", TokenType.OUTPUT);
    keywords.put("for", TokenType.FOR);
    keywords.put("while", TokenType.WHILE);
    keywords.put("case", TokenType.CASE);
    keywords.put("do", TokenType.DO);
    keywords.put("of", TokenType.OF);
    keywords.put("if", TokenType.IF);
    keywords.put("then", TokenType.THEN);
    keywords.put("ef", TokenType.EF);
    keywords.put("else", TokenType.ELSE);

    keywords.put("False", TokenType.FALSE);
    keywords.put("True", TokenType.TRUE);

    keywords.put("not", TokenType.NOT);
    keywords.put("or", TokenType.OR);
    keywords.put("mod", TokenType.MOD);
    keywords.put("and", TokenType.AND);
    keywords.put("shr", TokenType.SHR);
    keywords.put("shl", TokenType.SHL);
  }

  public Scanner(PeekReader<Symbol> reader, String source) {
    this.reader = reader;
    this.source = source;
    next();
  }

  private Token token(TokenType value) {
    Symbol peek = reader.peek();
    ElementInfo info;
    if (peek != null) {
      info = new ElementInfo(source, peek.line, peek.row);
    } else {
      info = new ElementInfo(source, 0, 0);
    }
    return new Token(value, info);
  }

  private Token token(TokenType value, String id) {
    ElementInfo info = new ElementInfo(source, reader.peek().line, reader.peek().row);
    return new Token(value, id, info);
  }

  private Token token(TokenType value, int num) {
    ElementInfo info = new ElementInfo(source, reader.peek().line, reader.peek().row);
    return new Token(value, num, info);
  }

  public boolean hasNext() {
    return peek() != null;
  }

  public Token peek() {
    return next;
  }

  public Token next() {
    Token res = next;

    next = token(TokenType.IGNORE);

    while (next.getType() == TokenType.IGNORE) {
      if (!reader.hasNext()) {
        next = token(TokenType.EOF);
        break;
      }
      next = getNext();
    }

    return res;
  }

  private Token getNext() {
    if (!reader.hasNext()) {
      return token(TokenType.EOF);
    }
    Symbol sym = reader.next();
    switch (sym.sym) {
    case ' ':
    case '\t':
    case 13:
    case '\n':
      return token(TokenType.IGNORE);
    case '<':
      return read_3C();
    case '=':
      return token(TokenType.EQUAL);
    case '>':
      return read_3E();
    case '-':
      return read_2D();
    case ',':
      return token(TokenType.COMMA);
    case ':':
      return read_3A();
    case ';':
      return token(TokenType.SEMI);
    case '\'':
      return read_27();
    case '/':
      return read_2F();
    case '.':
      return read_2E();
    case '(':
      return token(TokenType.OPENPAREN);
    case ')':
      return token(TokenType.CLOSEPAREN);
    case '{':
      return token(TokenType.OPENCURLY);
    case '}':
      return token(TokenType.CLOSECURLY);
    case '[':
      return token(TokenType.OPEN_ARRAY);
    case ']':
      return token(TokenType.CLOSE_ARRAY);
    case '*':
      return token(TokenType.STAR);
    case '+':
      return token(TokenType.PLUS);
    default:
      if (isAlpha(sym.sym)) {
        String id = readIdentifier(Character.toString(sym.sym));
        if (keywords.containsKey(id)) {
          return token(keywords.get(id));
        } else {
          TokenType type;
          type = TokenType.IDENTIFIER;
          Token toc = token(type, id);
          return toc;
        }
      } else if (isNummeric(sym.sym)) {
        return readNumber(sym.sym);
      } else {
        RError.err(ErrorType.Error, source, sym.line, sym.row, "Unexpected character: #" + Integer.toHexString((int) sym.sym) + " (" + sym.sym + ")");
        return token(TokenType.IGNORE);
      }
    }
  }

  // private boolean expect(char c) {
  // Symbol sym = reader.next();
  // if (sym.sym != c) {
  // Error.err(ErrorType.Error, sym.line, sym.row, "Expected " + c + " found " + sym);
  // }
  // return true;
  // }

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

  // EBNF number: numeric { numeric }
  private Token readNumber(char sym) {
    int num = sym - '0';
    while (isAlphaNummeric(reader.peek().sym)) {
      Symbol ne = reader.peek();
      if (!isNummeric(ne.sym)) {
        RError.err(ErrorType.Error, source, ne.line, ne.row, "number can't contain characters");
        return null;
      }
      num = num * 10 + reader.next().sym - '0';
    }
    Token res = token(TokenType.NUMBER, num);
    return res;
  }

  // '
  private Token read_27() {
    String value = readTilEndString();
    return token(TokenType.STRING, value);
  }

  // /
  private Token read_2F() {
    Symbol sym = reader.peek();
    switch (sym.sym) {
    case '*':
      reader.next();
      seekTilEndComment();
      return token(TokenType.IGNORE);
    case '/':
      reader.next();
      seekTilNewline();
      return token(TokenType.IGNORE);
    default:
      return token(TokenType.DIV);
    }
  }

  // .
  private Token read_2E() {
    Symbol sym = reader.peek();
    switch (sym.sym) {
    case '.':
      reader.next();
      return token(TokenType.RANGE);
    default:
      return token(TokenType.PERIOD);
    }
  }

  // :
  private Token read_3A() {
    Symbol sym = reader.peek();
    switch (sym.sym) {
    case '=':
      reader.next();
      return token(TokenType.BECOMES);
    default:
      return token(TokenType.COLON);
    }
  }

  // <
  private Token read_3C() {
    Symbol sym = reader.peek();
    switch (sym.sym) {
    case '=':
      reader.next();
      return token(TokenType.LEQ);
    case '>':
      reader.next();
      return token(TokenType.NEQ);
    default:
      return token(TokenType.LOWER);
    }
  }

  // >
  private Token read_3E() {
    Symbol sym = reader.peek();
    switch (sym.sym) {
    case '=':
      reader.next();
      return token(TokenType.GEQ);
    case '>':
      reader.next();
      return token(TokenType.ASYNC_MSG);
    default:
      return token(TokenType.GREATER);
    }
  }

  // -
  private Token read_2D() {
    Symbol sym = reader.peek();
    switch (sym.sym) {
    case '>':
      reader.next();
      return token(TokenType.SYNC_MSG);
    default:
      return token(TokenType.MINUS);
    }
  }

  private String readTilEndString() {
    String ret = "";
    Symbol sym;
    do {
      sym = reader.next();
      if (sym.sym == '\'') {
        return ret;
      } else {
        ret += sym.sym;
      }
    } while (reader.hasNext());
    RError.err(ErrorType.Error, source, sym.line, sym.row, "String over end of file");
    return null;
  }

  private void seekTilNewline() {
    Symbol sym;
    do {
      sym = reader.next();
      if (sym.sym == '\n') {
        return;
      }
    } while (reader.hasNext());
  }

  private void seekTilEndComment() {
    Symbol sym;
    do {
      sym = reader.next();
      if (sym.sym == '*') {
        if (reader.peek().sym == '/') {
          reader.next();
          return;
        }
      }
    } while (reader.hasNext());
    RError.err(ErrorType.Error, source, sym.line, sym.row, "end of file before end of comment");
  }
}
