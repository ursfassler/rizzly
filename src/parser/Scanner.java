package parser;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import common.ElementInfo;
import common.Metadata;

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
  private ArrayList<Metadata> metadata = new ArrayList<Metadata>();

  {
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

    keywords.put("is", TokenType.IS);
    keywords.put("as", TokenType.AS);
    keywords.put("in", TokenType.IN);

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

  public ArrayList<Metadata> getMetadata() {
    return new ArrayList<Metadata>(metadata);
  }

  private Token token(TokenType value, Symbol sym) {
    return new Token(value, new ElementInfo(source, sym.line, sym.row));
  }

  private Token token(TokenType value, String id, Symbol sym) {
    ElementInfo info = new ElementInfo(source, sym.line, sym.row);
    return new Token(value, id, info);
  }

  private Token token(TokenType value, BigInteger num, Symbol sym) {
    ElementInfo info = new ElementInfo(source, sym.line, sym.row);
    return new Token(value, num, info);
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
    metadata = new ArrayList<Metadata>();

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
    case '<':
      return read_3C(sym);
    case '=':
      return token(TokenType.EQUAL, sym);
    case '>':
      return read_3E(sym);
    case '-':
      return read_2D(sym);
    case ',':
      return token(TokenType.COMMA, sym);
    case ':':
      return read_3A(sym);
    case ';':
      return token(TokenType.SEMI, sym);
    case '\'':
      return read_27(sym);
    case '/':
      return read_2F(sym);
    case '.':
      return read_2E(sym);
    case '(':
      return token(TokenType.OPENPAREN, sym);
    case ')':
      return token(TokenType.CLOSEPAREN, sym);
    case '{':
      return token(TokenType.OPENCURLY, sym);
    case '}':
      return token(TokenType.CLOSECURLY, sym);
    case '[':
      return token(TokenType.OPEN_ARRAY, sym);
    case ']':
      return token(TokenType.CLOSE_ARRAY, sym);
    case '*':
      return token(TokenType.STAR, sym);
    case '+':
      return token(TokenType.PLUS, sym);
    default:
      if (isAlpha(sym.sym)) {
        String id = readIdentifier(Character.toString(sym.sym));
        if (keywords.containsKey(id)) {
          return token(keywords.get(id), sym);
        } else {
          TokenType type;
          type = TokenType.IDENTIFIER;
          Token toc = token(type, id, sym);
          return toc;
        }
      } else if (isNummeric(sym.sym)) {
        return readNumber(sym);
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

  // EBNF number: numeric { numeric }
  private Token readNumber(Symbol sym) {
    BigInteger num = BigInteger.valueOf(sym.sym - '0');
    while (isAlphaNummeric(reader.peek().sym)) {
      Symbol ne = reader.peek();
      if (!isNummeric(ne.sym)) {
        RError.err(ErrorType.Error, source, ne.line, ne.row, "number can't contain characters");
        return null;
      }
      num = num.multiply(BigInteger.TEN);
      num = num.add(BigInteger.valueOf(reader.next().sym - '0'));
    }
    Token res = token(TokenType.NUMBER, num, sym);
    return res;
  }

  // '
  private Token read_27(Symbol sym) {
    String value = readTilEndString();
    return token(TokenType.STRING, value, sym);
  }

  // /
  private Token read_2F(Symbol start) {
    Symbol sym = reader.peek();
    switch (sym.sym) {
    case '*':
      reader.next();
      seekTilEndComment();
      return token(TokenType.IGNORE, start);
    case '/':
      reader.next();
      parseMetadata(start);
      return token(TokenType.IGNORE, start);
    default:
      return token(TokenType.DIV, start);
    }
  }

  // .
  private Token read_2E(Symbol start) {
    Symbol sym = reader.peek();
    switch (sym.sym) {
    case '.':
      reader.next();
      return token(TokenType.RANGE, start);
    default:
      return token(TokenType.PERIOD, start);
    }
  }

  // :
  private Token read_3A(Symbol start) {
    Symbol sym = reader.peek();
    switch (sym.sym) {
    case '=':
      reader.next();
      return token(TokenType.BECOMES, start);
    default:
      return token(TokenType.COLON, start);
    }
  }

  // <
  private Token read_3C(Symbol start) {
    Symbol sym = reader.peek();
    switch (sym.sym) {
    case '=':
      reader.next();
      return token(TokenType.LEQ, start);
    case '>':
      reader.next();
      return token(TokenType.NEQ, start);
    default:
      return token(TokenType.LOWER, start);
    }
  }

  // >
  private Token read_3E(Symbol start) {
    Symbol sym = reader.peek();
    switch (sym.sym) {
    case '=':
      reader.next();
      return token(TokenType.GEQ, start);
    case '>':
      reader.next();
      return token(TokenType.ASYNC_MSG, start);
    default:
      return token(TokenType.GREATER, start);
    }
  }

  // -
  private Token read_2D(Symbol start) {
    Symbol sym = reader.peek();
    switch (sym.sym) {
    case '>':
      reader.next();
      return token(TokenType.SYNC_MSG, start);
    default:
      return token(TokenType.MINUS, start);
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

  private String seekTilNewline() {
    String data = "";
    do {
      if (reader.peek().sym == '\n') {
        return data;
      } else {
        data += reader.next().sym;
      }
    } while (reader.hasNext());
    return data;
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

  private void parseMetadata(Symbol start) {
    Symbol sym = reader.peek();
    ElementInfo info = new ElementInfo(source, sym.line, sym.row);

    String key = readMetaKey();
    String data = seekTilNewline();

    Metadata meta = new Metadata(info, key, data);
    metadata.add(meta);
  }

  private String readMetaKey() {
    String key = "";
    while (true) {
      switch (reader.peek().sym) {
      case ' ':
        reader.next();
        return key;
      case '\n':
        return key;
      default:
        key += reader.next().sym;
        break;
      }
    }
  }

}
