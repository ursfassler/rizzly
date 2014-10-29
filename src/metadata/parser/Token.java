package metadata.parser;

import common.ElementInfo;

/**
 * 
 * @author urs
 */
public class Token {
  private TokenType type;
  private String data;
  private ElementInfo info;

  public Token(TokenType type, ElementInfo info) {
    super();
    this.type = type;
    this.info = info;
  }

  public Token(TokenType type, String data, ElementInfo info) {
    super();
    this.type = type;
    this.data = data;
    this.info = info;
  }

  public TokenType getType() {
    return type;
  }

  public String getData() {
    assert ((type == TokenType.IDENTIFIER) || (type == TokenType.STRING));
    return data;
  }

  public ElementInfo getInfo() {
    return info;
  }

  @Override
  public String toString() {
    switch (type) {
      case IDENTIFIER:
      case STRING:
        return type + "(" + data + ")";
      default:
        return type.toString();
    }
  }
}
