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

import java.util.ArrayList;
import java.util.Map;

import parser.PeekReader;
import parser.scanner.tokenparser.Helper;
import parser.scanner.tokenparser.MetadataReader;
import parser.scanner.tokenparser.TokenParser;

import common.Metadata;

import error.ErrorType;
import error.RError;

/**
 *
 * @author urs
 */
public class Scanner implements PeekReader<Token> {
  private final Map<Character, TokenParser> parseTable;
  private MetadataReader meta;
  private PeekReader<Symbol> reader;
  private Token next;

  public Scanner(PeekReader<Symbol> reader, String source) {
    this.reader = reader;
    this.parseTable = ParseTable.get(reader);

    // FIXME very ugly hack; make better concept for meta data
    for (TokenParser tp : parseTable.values()) {
      if (tp instanceof MetadataReader) {
        meta = (MetadataReader) tp;
      }
    }

    next();
  }

  public ArrayList<Metadata> getMetadata() {
    return new ArrayList<Metadata>(meta.getMetadata());
  }

  @Override
  public boolean hasNext() {
    return peek() != null;
  }

  @Override
  public Token peek() {
    return next;
  }

  @Override
  public Token next() {
    meta.getMetadata().clear();

    Token res = next;

    do {
      if (!reader.hasNext()) {
        next = Helper.specialToken(TokenType.EOF);
        break;
      }
      next = getNext();
    } while (next.getType() == TokenType.IGNORE);
    return res;
  }

  private Token getNext() {
    if (!reader.hasNext()) {
      return Helper.specialToken(TokenType.EOF);
    }
    return parse();
  }

  public Token parse() {
    Symbol sym = reader.peek();
    TokenParser parser = parseTable.get(sym.sym);
    if (parser != null) {
      return parser.parse();
    } else {
      RError.err(ErrorType.Error, sym.filename, sym.line, sym.row, "Unexpected character: #" + Integer.toHexString(sym.sym) + " (" + sym.sym + ")");
      return Helper.specialToken(TokenType.IGNORE);
    }
  }

}
