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

import java.util.ArrayList;

import parser.PeekReader;
import parser.scanner.Symbol;
import parser.scanner.Token;
import parser.scanner.TokenType;
import ast.meta.Metadata;
import ast.meta.SourcePosition;
import error.ErrorType;
import error.RError;

public class Slash extends TokenParser implements MetadataReader {
  private ArrayList<Metadata> metadata = new ArrayList<Metadata>();

  public Slash(PeekReader<Symbol> reader) {
    super(reader);
  }

  @Override
  public Token parse() {
    Symbol first = reader.next();
    Symbol sym = reader.peek();
    switch (sym.sym) {
      case '*':
        reader.next();
        seekTilEndComment();
        return Helper.token(TokenType.IGNORE, first);
      case '/':
        reader.next();
        parseMetadata(first);
        return Helper.token(TokenType.IGNORE, first);
      default:
        return Helper.token(TokenType.DIV, first);
    }
  }

  private void seekTilEndComment() {
    Symbol sym;
    do {
      sym = reader.next();
      if (sym.sym == '*') {
        if (reader.peek().sym == '/') {
          reader.next();
          return;
        }
      }
    } while (reader.hasNext());
    RError.err(ErrorType.Error, sym.filename, sym.line, sym.row, "end of file before end of comment");
  }

  private void parseMetadata(Symbol start) {
    Symbol sym = reader.peek();
    SourcePosition info = new SourcePosition(sym.filename, sym.line, sym.row);

    String key = readMetaKey();
    String data = seekTilNewline();

    Metadata meta = new Metadata(info, key, data);
    metadata.add(meta);
  }

  private String seekTilNewline() {
    String data = "";
    do {
      if (reader.peek().sym == '\n') {
        return data;
      } else {
        data += reader.next().sym;
      }
    } while (reader.hasNext());
    return data;
  }

  private String readMetaKey() {
    String key = "";
    while (true) {
      switch (reader.peek().sym) {
        case ' ':
          reader.next();
          return key;
        case '\n':
          return key;
        default:
          key += reader.next().sym;
          break;
      }
    }
  }

  @Override
  public ArrayList<Metadata> getMetadata() {
    return metadata;
  }

}
