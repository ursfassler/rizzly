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

import parser.PeekReader;
import parser.scanner.Symbol;
import parser.scanner.Token;
import parser.scanner.TokenType;
import error.ErrorType;
import error.RError;

public class Number extends TokenParser {

  public Number(PeekReader<Symbol> reader) {
    super(reader);
  }

  @Override
  // EBNF number: numeric { numeric }
  public Token parse() {
    Symbol sym = reader.next();
    BigInteger num = BigInteger.valueOf(sym.sym - '0');
    while (Helper.isAlphaNummeric(reader.peek().sym)) {
      Symbol ne = reader.peek();
      if (!Helper.isNummeric(ne.sym)) {
        RError.err(ErrorType.Error, ne.filename, ne.line, ne.row, "number can't contain characters");
        return null;
      }
      num = num.multiply(BigInteger.TEN);
      num = num.add(BigInteger.valueOf(reader.next().sym - '0'));
    }
    Token res = Helper.token(TokenType.NUMBER, num, sym);
    return res;
  }
}
