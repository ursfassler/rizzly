package parser;

import java.util.ArrayList;
import java.util.List;

import common.ElementInfo;
import common.Metadata;

import error.ErrorType;
import error.RError;
import evl.composition.MessageType;
import fun.composition.Connection;
import fun.composition.ImplComposition;
import fun.expression.reference.RefName;
import fun.expression.reference.Reference;
import fun.expression.reference.ReferenceUnlinked;
import fun.other.Component;

public class ImplCompositionParser extends ImplBaseParser {

  public ImplCompositionParser(Scanner scanner) {
    super(scanner);
  }

  public static Component parse(Scanner scanner, Token name) {
    ImplCompositionParser parser = new ImplCompositionParser(scanner);
    return parser.parseImplementationComposition(name);
  }

  // EBNF implementationComposition: "composition" { compDeclBlock | connectionDeclBlock }
  private ImplComposition parseImplementationComposition(Token name) {
    ElementInfo info = expect(TokenType.COMPOSITION).getInfo();
    ArrayList<Metadata> meta = getMetadata();
    info.getMetadata().addAll(meta);
    ImplComposition comp = new ImplComposition(name.getInfo(), name.getData());

    while (true) {
      switch (peek().getType()) {
      case COMPONENT: {
        comp.getComponent().addAll(parseCompDeclBlock());
        break;
      }
      case CONNECTION: {
        comp.getConnection().addAll(parseConnectionDeclBlock());
        break;
      }
      default: {
        return comp;
      }
      }
    }
  }

  // EBNF connectionDeclBlock: "connection" connection { connection }
  private List<Connection> parseConnectionDeclBlock() {
    expect(TokenType.CONNECTION);
    List<Connection> ret = new ArrayList<Connection>();
    do {
      ret.add(parseConnection());
    } while (peek().getType() == TokenType.IDENTIFIER);
    return ret;
  }

  // EBNF connection: endpoint msgType endpoint ";"
  private Connection parseConnection() {
    Reference src = parseEndpoint();
    ElementInfo info = peek().getInfo();
    MessageType type = parseMsgType();
    Reference dst = parseEndpoint();
    expect(TokenType.SEMI);

    ArrayList<Metadata> meta = getMetadata();
    info.getMetadata().addAll(meta);

    if (type != MessageType.sync) {
      RError.err(ErrorType.Fatal, info, "Only synchron messages supported at the moment");
      return null;
    }

    return new Connection(info, src, dst, type);
  }

  // EBNF endpoint: id [ "." id ]
  private Reference parseEndpoint() {
    Reference ref = new ReferenceUnlinked(peek().getInfo());
    Token tok = expect(TokenType.IDENTIFIER);
    ref.getOffset().add(new RefName(tok.getInfo(), tok.getData()));
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
