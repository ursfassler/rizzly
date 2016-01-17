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

package parser;

import java.util.ArrayList;
import java.util.List;

import parser.scanner.Token;
import parser.scanner.TokenType;
import error.ErrorType;
import error.RError;

/**
 *
 * @author urs
 */
public abstract class Parser {
  private PeekNReader<Token> scanner;

  public Parser(PeekNReader<Token> scanner) {
    this.scanner = scanner;
  }

  protected PeekNReader<Token> getScanner() {
    return scanner;
  }

  public Token peek() {
    return scanner.peek(0);
  }

  public Token peek(int i) {
    return scanner.peek(i);
  }

  protected Token next() {
    return scanner.next();
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

  // TODO move into own class
  @Deprecated
  public boolean consumeIfEqual(TokenType tok) {
    if (peek().getType() == tok) {
      next();
      return true;
    } else {
      return false;
    }
  }

  // TODO move into own class
  @Deprecated
  public Token expect(TokenType type) {
    Token got = next();
    if (got.getType() != type) {
      RError.err(ErrorType.Error, "expected " + type + " got " + got, got.getMetadata());
    }
    return got;
  }

  protected void wrongToken(TokenType expected) {
    Token got = next();
    RError.err(ErrorType.Error, "expected " + expected + " got " + got, got.getMetadata());
  }

}
