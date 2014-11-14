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

import common.Metadata;

import error.ErrorType;
import error.RError;

/**
 *
 * @author urs
 */
public abstract class Parser {
  private Scanner scanner;

  public Parser(Scanner scanner) {
    this.scanner = scanner;
  }

  protected Scanner getScanner() {
    return scanner;
  }

  protected Token peek() {
    return scanner.peek();
  }

  protected Token next() {
    return scanner.next();
  }

  protected boolean hasNext() {
    return scanner.hasNext();
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

  protected boolean isLhsOrUse() {
    switch (peek().getType()) {
      case IDENTIFIER:
        return true;
      default:
        return false;
    }
  }

  protected boolean consumeIfEqual(TokenType tok) {
    if (scanner.peek().getType() == tok) {
      scanner.next();
      return true;
    } else {
      return false;
    }
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

  protected void wrongToken(TokenType type) {
    if (!scanner.hasNext()) {
      Token tok = scanner.peek();
      RError.err(ErrorType.Error, tok.getInfo(), "expected token not found: " + tok);
    }
    Token got = scanner.next();
    RError.err(ErrorType.Error, got.getInfo(), "expected " + type + " got " + got);
  }

  protected ArrayList<Metadata> getMetadata() {
    return scanner.getMetadata();
  }

}
