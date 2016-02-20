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

import parser.scanner.Token;
import parser.scanner.TokenType;
import ast.data.component.composition.AsynchroniusConnection;
import ast.data.component.composition.ComponentUse;
import ast.data.component.composition.Connection;
import ast.data.component.composition.Endpoint;
import ast.data.component.composition.EndpointRaw;
import ast.data.component.composition.SynchroniusConnection;
import ast.data.raw.RawComponent;
import ast.data.raw.RawComposition;
import ast.data.reference.LinkedReferenceWithOffset_Implementation;
import ast.data.reference.RefFactory;
import ast.data.reference.RefName;
import ast.data.reference.Reference;
import ast.meta.MetaList;
import error.ErrorType;
import error.RError;

public class ImplCompositionParser extends ImplBaseParser {

  public ImplCompositionParser(PeekNReader<Token> scanner) {
    super(scanner);
  }

  public static RawComponent parse(PeekNReader<Token> scanner, String name) {
    ImplCompositionParser parser = new ImplCompositionParser(scanner);
    return parser.parseImplementationComposition(name);
  }

  // EBNF implementationComposition: "composition" { compDeclBlock |
  // connectionDeclBlock }
  private RawComposition parseImplementationComposition(String name) {
    MetaList info = expect(TokenType.COMPOSITION).getMetadata();
    RawComposition comp = new RawComposition(info, name);

    while (!consumeIfEqual(TokenType.END)) {
      Token id = expect(TokenType.IDENTIFIER);

      if (consumeIfEqual(TokenType.COLON)) {
        parseInstantiation(id, comp);
      } else {
        ast.data.component.composition.Connection obj = parseConnection(id);
        comp.getConnection().add(obj);
      }
    }

    return comp;
  }

  private void parseInstantiation(Token id, RawComposition comp) {
    switch (peek().getType()) {
      case IDENTIFIER:
        Reference type = expr().parseRef();
        expect(TokenType.SEMI);
        ComponentUse compUse = new ComponentUse(id.getMetadata(), id.getData(), type);
        comp.getInstantiation().add(compUse);
        break;
      default: {
        RError.err(ErrorType.Error, "Expected interface function or reference", peek().getMetadata());
        break;
      }
    }
  }

  // EBNF connection: endpoint msgType endpoint ";"
  private Connection parseConnection(Token id) {
    Endpoint src = parseEndpoint(id);
    MetaList info = peek().getMetadata();
    MessageType type = parseMsgType();
    Endpoint dst = parseEndpoint(next());
    expect(TokenType.SEMI);

    Connection connection;

    switch (type) {
      case sync:
        connection = new SynchroniusConnection(src, dst);
        break;
      case async:
        connection = new AsynchroniusConnection(src, dst);
        break;
      default:
        RError.err(ErrorType.Fatal, "Unknown connection type: " + type, info);
        return null;
    }

    connection.metadata().add(info);
    return connection;
  }

  // EBNF endpoint: id [ "." id ]
  private Endpoint parseEndpoint(Token tok) {
    if (tok.getType() != TokenType.IDENTIFIER) {
      RError.err(ErrorType.Error, "Expected IDENTIFIER, got " + tok.getType(), tok.getMetadata());
      return null;
    }
    LinkedReferenceWithOffset_Implementation ref = RefFactory.oldFull(tok.getMetadata(), tok.getData());
    if (consumeIfEqual(TokenType.PERIOD)) {
      tok = expect(TokenType.IDENTIFIER);
      ref.getOffset().add(new RefName(tok.getMetadata(), tok.getData()));
    }
    return new EndpointRaw(tok.getMetadata(), ref);
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
        RError.err(ErrorType.Error, "Expected synchron or asynchron connection", tok.getMetadata());
        return null;
      }
    }
  }

}

enum MessageType {
  sync, async
}
