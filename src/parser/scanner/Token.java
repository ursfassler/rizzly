/**
 *  This file is part of Rizzly.
 *
 *  Rizzly is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Rizzly is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with Rizzly.  If not, see <http://www.gnu.org/licenses/>.
 */

package parser.scanner;

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
