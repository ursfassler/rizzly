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

import java.util.HashMap;
import java.util.Map;

import parser.PeekReader;
import parser.scanner.tokenparser.Alpha;
import parser.scanner.tokenparser.Apostrophe;
import parser.scanner.tokenparser.Colon;
import parser.scanner.tokenparser.Greater;
import parser.scanner.tokenparser.Minus;
import parser.scanner.tokenparser.OneSymbol;
import parser.scanner.tokenparser.Period;
import parser.scanner.tokenparser.Slash;
import parser.scanner.tokenparser.Smaller;
import parser.scanner.tokenparser.TokenParser;

public class ParseTable {
  public static Map<Character, TokenParser> get(PeekReader<Symbol> reader) {
    Map<Character, TokenParser> ret = new HashMap<Character, TokenParser>();

    ret.putAll(alpha(reader));
    ret.putAll(numeric(reader));
    ret.putAll(brackets(reader));
    ret.putAll(whitespace(reader));
    ret.putAll(others(reader));

    return ret;
  }

  private static Map<Character, TokenParser> others(PeekReader<Symbol> reader) {
    Map<Character, TokenParser> ret = new HashMap<Character, TokenParser>();

    ret.put('=', new OneSymbol(TokenType.EQUAL, reader));
    ret.put(',', new OneSymbol(TokenType.COMMA, reader));
    ret.put(';', new OneSymbol(TokenType.SEMI, reader));
    ret.put('*', new OneSymbol(TokenType.STAR, reader));
    ret.put('+', new OneSymbol(TokenType.PLUS, reader));
    ret.put('-', new Minus(reader));
    ret.put(':', new Colon(reader));
    ret.put('.', new Period(reader));
    ret.put('\'', new Apostrophe(reader));
    ret.put('/', new Slash(reader));

    return ret;
  }

  static private Map<Character, TokenParser> brackets(PeekReader<Symbol> reader) {
    Map<Character, TokenParser> ret = new HashMap<Character, TokenParser>();

    ret.put('<', new Smaller(reader));
    ret.put('>', new Greater(reader));
    ret.put('(', new OneSymbol(TokenType.OPENPAREN, reader));
    ret.put(')', new OneSymbol(TokenType.CLOSEPAREN, reader));
    ret.put('[', new OneSymbol(TokenType.OPENBRACKETS, reader));
    ret.put(']', new OneSymbol(TokenType.CLOSEBRACKETS, reader));
    ret.put('{', new OneSymbol(TokenType.OPENCURLY, reader));
    ret.put('}', new OneSymbol(TokenType.CLOSECURLY, reader));

    return ret;
  }

  static private Map<Character, TokenParser> whitespace(PeekReader<Symbol> reader) {
    Map<Character, TokenParser> ret = new HashMap<Character, TokenParser>();

    OneSymbol ignoreParser = new OneSymbol(TokenType.IGNORE, reader);
    ret.put(' ', ignoreParser);
    ret.put('\t', ignoreParser);
    ret.put('\r', ignoreParser);
    ret.put('\n', ignoreParser);

    return ret;
  }

  static private Map<Character, TokenParser> alpha(PeekReader<Symbol> reader) {
    Map<Character, TokenParser> ret = new HashMap<Character, TokenParser>();

    Alpha alphaParser = new Alpha(reader);

    for (char sym = 'a'; sym <= 'z'; sym++) {
      ret.put(sym, alphaParser);
    }
    for (char sym = 'A'; sym <= 'Z'; sym++) {
      ret.put(sym, alphaParser);
    }

    return ret;
  }

  static private Map<Character, TokenParser> numeric(PeekReader<Symbol> reader) {
    Map<Character, TokenParser> ret = new HashMap<Character, TokenParser>();

    parser.scanner.tokenparser.Number numberParser = new parser.scanner.tokenparser.Number(reader);

    for (char sym = '0'; sym <= '9'; sym++) {
      ret.put(sym, numberParser);
    }

    return ret;
  }

}
