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

import common.ElementInfo;
import common.Metadata;

import error.ErrorType;
import error.RError;
import evl.data.component.composition.MessageType;
import fun.composition.Connection;
import fun.composition.ImplComposition;
import fun.expression.reference.RefName;
import fun.expression.reference.Reference;
import fun.other.CompImpl;
import fun.variable.CompUse;

public class ImplCompositionParser extends ImplBaseParser {

  public ImplCompositionParser(Scanner scanner) {
    super(scanner);
  }

  public static CompImpl parse(Scanner scanner, String name) {
    ImplCompositionParser parser = new ImplCompositionParser(scanner);
    return parser.parseImplementationComposition(name);
  }

  // EBNF implementationComposition: "composition" { compDeclBlock | connectionDeclBlock }
  private ImplComposition parseImplementationComposition(String name) {
    ElementInfo info = expect(TokenType.COMPOSITION).getInfo();
    ArrayList<Metadata> meta = getMetadata();
    info.getMetadata().addAll(meta);
    ImplComposition comp = new ImplComposition(info, name);

    while (!consumeIfEqual(TokenType.END)) {
      Token id = expect(TokenType.IDENTIFIER);

      if (consumeIfEqual(TokenType.COLON)) {
        parseInstantiation(id, comp);
      } else {
        Connection obj = parseConnection(id);
        comp.getConnection().add(obj);
      }
    }

    return comp;
  }

  private void parseInstantiation(Token id, ImplComposition comp) {
    switch (peek().getType()) {
      case IDENTIFIER:
        Reference type = expr().parseRef();
        expect(TokenType.SEMI);
        ArrayList<Metadata> meta = getMetadata();
        id.getInfo().getMetadata().addAll(meta);
        CompUse compUse = new CompUse(id.getInfo(), id.getData(), type);
        comp.getInstantiation().add(compUse);
        break;
      default: {
        RError.err(ErrorType.Error, peek().getInfo(), "Expected interface function or reference");
        break;
      }
    }
  }

  // EBNF connection: endpoint msgType endpoint ";"
  private Connection parseConnection(Token id) {
    Reference src = parseEndpoint(id);
    ElementInfo info = peek().getInfo();
    MessageType type = parseMsgType();
    Reference dst = parseEndpoint(next());
    expect(TokenType.SEMI);

    ArrayList<Metadata> meta = getMetadata();
    info.getMetadata().addAll(meta);

    return new Connection(info, src, dst, type);
  }

  // EBNF endpoint: id [ "." id ]
  private Reference parseEndpoint(Token tok) {
    if (tok.getType() != TokenType.IDENTIFIER) {
      RError.err(ErrorType.Error, tok.getInfo(), "Expected IDENTIFIER, got " + tok.getType());
      return null;
    }
    Reference ref = new Reference(tok.getInfo(), tok.getData());
    if (consumeIfEqual(TokenType.PERIOD)) {
      tok = expect(TokenType.IDENTIFIER);
      ref.getOffset().add(new RefName(tok.getInfo(), tok.getData()));
    }
    return ref;
  }

  // EBNF msgType: "->" | ">>"
  private MessageType parseMsgType() {
    Token tok = next();
    switch (tok.getType()) {
      case SYNC_MSG: {
        return MessageType.sync;
      }
      case ASYNC_MSG: {
        return MessageType.async;
      }
      default: {
        RError.err(ErrorType.Error, tok.getInfo(), "Expected synchron or asynchron connection");
        return null;
      }
    }
  }

}
