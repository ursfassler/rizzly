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
import java.util.List;

import parser.scanner.Token;
import parser.scanner.TokenType;
import ast.data.Ast;
import ast.data.Named;
import ast.data.raw.RawComponent;
import ast.data.raw.RawElementary;
import ast.data.template.Template;
import ast.data.variable.PrivateConstant;
import ast.data.variable.StateVariable;
import ast.data.variable.TemplateParameter;
import ast.meta.MetaList;
import error.ErrorType;
import error.RError;

public class ImplElementaryParser extends ImplBaseParser {
  public ImplElementaryParser(PeekNReader<Token> scanner) {
    super(scanner);
  }

  public static RawComponent parse(PeekNReader<Token> scanner, String name) {
    ImplElementaryParser parser = new ImplElementaryParser(scanner);
    return parser.parseImplementationElementary(name);
  }

  // EBNF implementationElementary: "elementary" { entryCode | exitCode |
  // compDeclBlock | varDeclBlock | constDeclBlock
  // | privateFunction | responseFunction | slotFunction | interruptFunction |
  // entry | exit }
  private RawElementary parseImplementationElementary(String name) {
    MetaList info = expect(TokenType.ELEMENTARY).getMetadata();
    RawElementary comp = new RawElementary(info, name);

    while (!consumeIfEqual(TokenType.END)) {
      if (peek().getType() == TokenType.IDENTIFIER) {
        Token id = expect(TokenType.IDENTIFIER);

        if (consumeIfEqual(TokenType.EQUAL)) {
          List<TemplateParameter> genpam;
          if (peek().getType() == TokenType.OPENCURLY) {
            genpam = parseGenericParam();
          } else {
            genpam = new ArrayList<TemplateParameter>();
          }
          Named obj = parseDeclaration(id.getData());
          Template decl = new Template(id.getMetadata(), id.getData(), genpam, obj);
          comp.getDeclaration().add(decl);
        } else if (consumeIfEqual(TokenType.COLON)) {
          Ast var = parseInstantiation(id.getData());
          comp.getInstantiation().add(var);
        } else {
          Token got = peek();
          RError.err(ErrorType.Error, "got unexpected token: " + got, got.getMetadata());
          break;
        }
      } else {
        parseAnonymous(comp);
      }
    }

    return comp;
  }

  private void parseAnonymous(RawElementary comp) {
    switch (peek().getType()) {
      case ENTRY:
        comp.setEntryFunc(parseEntryCode());
        break;
      case EXIT:
        comp.setExitFunc(parseExitCode());
        break;
      case INTERRUPT:
        throw new RuntimeException("not yet implemented");
      default:
        RError.err(ErrorType.Fatal, "not yet implemented: " + peek().getType(), peek().getMetadata());
        throw new RuntimeException("not yet implemented: " + peek().getType());
    }
  }

  private Ast parseInstantiation(String name) {
    switch (peek().getType()) {
      case CONST: {
        PrivateConstant var = parseConstDef(PrivateConstant.class, name);
        expect(TokenType.SEMI);
        return var;
      }
      case RESPONSE:
      case SLOT: {
        return parseFuncDef(peek().getType(), name, false);
      }
      default: {
        StateVariable var = parseStateVardef(name);
        expect(TokenType.SEMI);
        return var;
      }
    }
  }

  private Named parseDeclaration(String name) {
    switch (peek().getType()) {
      case FUNCTION:
      case PROCEDURE:
        return parseFuncDef(peek().getType(), name, false);
      default: {
        return type().parseTypeDef(name);
      }
    }
  }

}
