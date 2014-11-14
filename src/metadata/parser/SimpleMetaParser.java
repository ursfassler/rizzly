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

package metadata.parser;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import common.ElementInfo;
import common.Metadata;

import error.ErrorType;
import error.RError;

public class SimpleMetaParser {
  private Scanner scanner;
  private Map<String, String> data = new HashMap<String, String>();

  public SimpleMetaParser(List<Metadata> metadata) {
    super();
    scanner = new Scanner(new MetadataReader(metadata));
  }

  public static Map<String, String> parse(List<Metadata> metadata) {
    SimpleMetaParser parser = new SimpleMetaParser(metadata);
    parser.parse();
    return parser.data;
  }

  // { entry }
  public void parse() {
    while (scanner.peek().getType() == TokenType.IDENTIFIER) {
      parseEntry();
    }
  }

  // entry: id "=" "\"" text "\""
  private void parseEntry() {
    ElementInfo info = scanner.peek().getInfo();
    String id = expect(TokenType.IDENTIFIER).getData();
    expect(TokenType.EQUAL);
    String text = expect(TokenType.STRING).getData();

    if (data.containsKey(id)) {
      RError.err(ErrorType.Warning, info, "double key in meta data");
    }

    data.put(id, text);
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

}
