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

import parser.scanner.Token;
import parser.scanner.TokenType;
import ast.ElementInfo;
import ast.data.Metadata;
import ast.data.component.CompRef;
import ast.data.component.composition.AsynchroniusConnection;
import ast.data.component.composition.CompUse;
import ast.data.component.composition.Endpoint;
import ast.data.component.composition.EndpointRaw;
import ast.data.component.composition.SynchroniusConnection;
import ast.data.raw.RawComponent;
import ast.data.raw.RawComposition;
import ast.data.reference.RefFactory;
import ast.data.reference.RefName;
import ast.data.reference.Reference;
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
    ElementInfo info = expect(TokenType.COMPOSITION).getInfo();
    ArrayList<Metadata> meta = getMetadata();
    info.metadata.addAll(meta);
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
        CompRef type = expr().parseRefComp();
        expect(TokenType.SEMI);
        ArrayList<Metadata> meta = getMetadata();
        id.getInfo().metadata.addAll(meta);
        ast.data.component.composition.CompUse compUse = new CompUse(id.getInfo(), id.getData(), type);
        comp.getInstantiation().add(compUse);
        break;
      default: {
        RError.err(ErrorType.Error, peek().getInfo(), "Expected interface function or reference");
        break;
      }
    }
  }

  // EBNF connection: endpoint msgType endpoint ";"
  private ast.data.component.composition.Connection parseConnection(Token id) {
    Endpoint src = parseEndpoint(id);
    ElementInfo info = peek().getInfo();
    MessageType type = parseMsgType();
    Endpoint dst = parseEndpoint(next());
    expect(TokenType.SEMI);

    ArrayList<Metadata> meta = getMetadata();
    info.metadata.addAll(meta);

    switch (type) {
      case sync:
        return new SynchroniusConnection(info, src, dst);
      case async:
        return new AsynchroniusConnection(info, src, dst);
      default:
        RError.err(ErrorType.Fatal, info, "Unknown connection type: " + type);
        return null;

    }
  }

  // EBNF endpoint: id [ "." id ]
  private Endpoint parseEndpoint(Token tok) {
    if (tok.getType() != TokenType.IDENTIFIER) {
      RError.err(ErrorType.Error, tok.getInfo(), "Expected IDENTIFIER, got " + tok.getType());
      return null;
    }
    Reference ref = RefFactory.full(tok.getInfo(), tok.getData());
    if (consumeIfEqual(TokenType.PERIOD)) {
      tok = expect(TokenType.IDENTIFIER);
      ref.offset.add(new RefName(tok.getInfo(), tok.getData()));
    }
    return new EndpointRaw(tok.getInfo(), ref);
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

enum MessageType {
  sync, async
}
