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

package parser.scanner.tokenparser;

import java.math.BigInteger;

import parser.scanner.Symbol;
import parser.scanner.Token;
import parser.scanner.TokenType;
import ast.ElementInfo;

/**
 *
 * @author urs
 */
public class Helper {
  static public Token token(TokenType value, Symbol sym) {
    return new Token(value, new ElementInfo(sym.filename, sym.line, sym.row));
  }

  static public Token token(TokenType value, String id, Symbol sym) {
    ElementInfo info = new ElementInfo(sym.filename, sym.line, sym.row);
    return new Token(value, id, info);
  }

  static public Token token(TokenType value, BigInteger num, Symbol sym) {
    ElementInfo info = new ElementInfo(sym.filename, sym.line, sym.row);
    return new Token(value, num, info);
  }

  static public Token specialToken(TokenType value) {
    return new Token(value, ElementInfo.NO);
  }

  static public boolean isAlphaNummeric(char sym) {
    return isAlpha(sym) || isNummeric(sym);
  }

  // EBNF alpha: "a".."z" | "A".."Z" | "_"
  static public boolean isAlpha(char sym) {
    return (sym >= 'a' && sym <= 'z') || (sym >= 'A' && sym <= 'Z') || (sym == '_');
  }

  // EBNF numeric: "0".."9"
  static public boolean isNummeric(char sym) {
    return (sym >= '0' && sym <= '9');
  }

}
