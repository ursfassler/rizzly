package metadata.parser;

import java.util.HashMap;
import java.util.Map;

import common.ElementInfo;

import error.ErrorType;
import error.RError;

public class SimpleMetaParser {
  private Scanner scanner;
  private Map<String, String> data = new HashMap<String, String>();

  public SimpleMetaParser(String data) {
    super();
    scanner = new Scanner(new StringReader(data), data);
  }

  public static Map<String, String> parse(String data) {
    SimpleMetaParser parser = new SimpleMetaParser(data);
    parser.parse();
    return parser.data;
  }

  // { entry }
  public void parse() {
    while (scanner.peek().getType() == TokenType.IDENTIFIER) {
      parseEntry();
    }
  }

  // entry: id "=" "\"" text "\""
  private void parseEntry() {
    ElementInfo info = scanner.peek().getInfo();
    String id = expect(TokenType.IDENTIFIER).getData();
    expect(TokenType.EQUAL);
    String text = expect(TokenType.STRING).getData();

    if (data.containsKey(id)) {
      RError.err(ErrorType.Warning, info, "double key in meta data");
    }

    data.put(id, text);
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

}
