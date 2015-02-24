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

import common.ElementInfo;
import common.Metadata;

import error.ErrorType;
import error.RError;
import fun.Fun;
import fun.other.CompImpl;
import fun.other.ImplElementary;
import fun.other.Template;
import fun.variable.ConstPrivate;
import fun.variable.StateVariable;
import fun.variable.TemplateParameter;

public class ImplElementaryParser extends ImplBaseParser {
  public ImplElementaryParser(Scanner scanner) {
    super(scanner);
  }

  public static CompImpl parse(Scanner scanner, String name) {
    ImplElementaryParser parser = new ImplElementaryParser(scanner);
    return parser.parseImplementationElementary(name);
  }

  // EBNF implementationElementary: "elementary" { entryCode | exitCode | compDeclBlock | varDeclBlock | constDeclBlock
  // | privateFunction | responseFunction | slotFunction | interruptFunction | entry | exit }
  private ImplElementary parseImplementationElementary(String name) {
    ElementInfo info = expect(TokenType.ELEMENTARY).getInfo();
    ArrayList<Metadata> meta = getMetadata();
    info.getMetadata().addAll(meta);
    ImplElementary comp = new ImplElementary(info, name);

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
          Fun obj = parseDeclaration(id.getData());
          Template decl = new Template(id.getInfo(), id.getData(), genpam, obj);
          comp.getDeclaration().add(decl);
        } else if (consumeIfEqual(TokenType.COLON)) {
          Fun var = parseInstantiation(id.getData());
          comp.getInstantiation().add(var);
        } else {
          Token got = peek();
          RError.err(ErrorType.Error, got.getInfo(), "got unexpected token: " + got);
          break;
        }
      } else {
        parseAnonymous(comp);
      }
    }

    return comp;
  }

  private void parseAnonymous(ImplElementary comp) {
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
        RError.err(ErrorType.Fatal, peek().getInfo(), "not yet implemented: " + peek().getType());
        throw new RuntimeException("not yet implemented: " + peek().getType());
    }
  }

  private Fun parseInstantiation(String name) {
    switch (peek().getType()) {
      case CONST: {
        ConstPrivate var = parseConstDef(ConstPrivate.class, name);
        expect(TokenType.SEMI);
        return var;
      }
      case RESPONSE:
      case SLOT: {
        return parseFuncDef(peek().getType(), name, false);
      }
      default: {
        StateVariable var = parseVarDef2(StateVariable.class, name);
        expect(TokenType.SEMI);
        return var;
      }
    }
  }

  private Fun parseDeclaration(String name) {
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
