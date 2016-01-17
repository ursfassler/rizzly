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

import ast.meta.MetaList;
import ast.meta.MetaListImplementation;
import ast.meta.SourcePosition;

/**
 *
 * @author urs
 */
public class Token {
  private TokenType type = null;
  private String data = null;
  private BigInteger num = null;
  final private MetaList metadata;

  public Token(TokenType type, MetaList info) {
    this.type = type;
    this.metadata = info;
  }

  public Token(TokenType type) {
    this.type = type;
    metadata = new MetaListImplementation(); // TODO use dependency injection
  }

  public Token(TokenType type, String data) {
    super();
    this.type = type;
    this.data = data;
    metadata = new MetaListImplementation(); // TODO use dependency injection
  }

  @Deprecated
  public Token(TokenType type, SourcePosition info) {
    super();
    this.type = type;
    metadata = new MetaListImplementation(); // TODO use dependency injection
    metadata.add(info);
  }

  @Deprecated
  public Token(TokenType type, BigInteger num, SourcePosition info) {
    super();
    this.type = type;
    this.num = num;
    metadata = new MetaListImplementation(); // TODO use dependency injection
    metadata.add(info);
  }

  @Deprecated
  public Token(TokenType type, String data, SourcePosition info) {
    super();
    this.type = type;
    this.data = data;
    metadata = new MetaListImplementation(); // TODO use dependency injection
    metadata.add(info);
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

  public MetaList getMetadata() {
    return metadata;
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
