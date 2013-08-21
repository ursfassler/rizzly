package parser;

import java.math.BigInteger;

import common.ElementInfo;

/**
 * 
 * @author urs
 */
public class Token {
  private TokenType type;
  private String data;
  private BigInteger num;
  private ElementInfo info;

  public Token(TokenType type, ElementInfo info) {
    super();
    this.type = type;
    this.info = info;
  }

  public Token(TokenType type, BigInteger num, ElementInfo info) {
    super();
    this.type = type;
    this.num = num;
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

  public BigInteger getNum() {
    assert (type == TokenType.NUMBER);
    return num;
  }

  public ElementInfo getInfo() {
    return info;
  }

  @Override
  public String toString() {
    switch (type) {
    case IDENTIFIER:
      return type + "(" + data + ")";
    case NUMBER:
      return type + "(" + num + ")";
    default:
      return type.toString();
    }
  }
}
