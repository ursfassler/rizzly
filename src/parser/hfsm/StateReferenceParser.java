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

package parser.hfsm;

import parser.PeekNReader;
import parser.scanner.Token;
import parser.scanner.TokenType;
import ast.ElementInfo;
import ast.data.component.hfsm.StateRef;
import ast.data.reference.RefFactory;
import ast.data.reference.RefName;
import ast.data.reference.Reference;
import error.ErrorType;
import error.RizzlyError;

/**
 *
 * @author urs
 *
 *         EBNF stateRef: id { "." id }
 *
 */
public class StateReferenceParser {

  final private RizzlyError error;
  final private PeekNReader<Token> scanner;

  public StateReferenceParser(PeekNReader<Token> scanner, RizzlyError error) {
    this.scanner = scanner;
    this.error = error;
  }

  public StateRef parse() {
    Token token = scanner.peek(0);
    switch (token.getType()) {
      case IDENTIFIER:
        return identifier();
      default:
        ElementInfo info = token.getInfo();
        String message = "expected " + TokenType.IDENTIFIER + ", got " + token.getType();
        error.err(ErrorType.Error, info.filename, info.line, info.row, message);
        return null;
    }
  }

  private StateRef identifier() {
    Token token = scanner.next();
    Reference ref = RefFactory.create(token.getInfo(), token.getData());

    while (scanner.peek(0).getType() == TokenType.PERIOD) {
      scanner.next();
      Token sub = scanner.next();
      ref.offset.add(new RefName(sub.getInfo(), sub.getData()));
    }

    return new StateRef(token.getInfo(), ref);
  }
}
