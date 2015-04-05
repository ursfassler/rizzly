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

import parser.PeekReader;
import parser.scanner.Symbol;
import parser.scanner.Token;
import parser.scanner.TokenType;

public class Minus extends TokenParser {

  public Minus(PeekReader<Symbol> reader) {
    super(reader);
  }

  @Override
  public Token parse() {
    Symbol first = reader.next();
    Symbol sym = reader.peek();
    switch (sym.sym) {
      case '>':
        reader.next();
        return Helper.token(TokenType.SYNC_MSG, first);
      default:
        return Helper.token(TokenType.MINUS, first);
    }
  }
}
