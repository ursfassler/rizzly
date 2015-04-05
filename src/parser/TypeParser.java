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

import parser.scanner.Scanner;
import parser.scanner.Token;
import parser.scanner.TokenType;
import error.ErrorType;
import error.RError;
import fun.Copy;
import fun.Fun;
import fun.expression.reference.Reference;
import fun.function.FuncHeader;
import fun.other.CompImpl;
import fun.other.FunList;
import fun.type.Type;
import fun.type.base.EnumElement;
import fun.type.base.EnumType;
import fun.type.composed.NamedElement;
import fun.type.composed.RecordType;
import fun.type.composed.UnionType;

//TODO cleanup (mostly components)
//TODO update EBNF
public class TypeParser extends BaseParser {

  public TypeParser(Scanner scanner) {
    super(scanner);
  }

  // EBNF compdecl: "Component" compIfaceList implementation
  public CompImpl parseCompdecl(String name) {
    expect(TokenType.COMPONENT);
    FunList<FuncHeader> list = parseCompIfaceList();
    CompImpl comp;
    switch (peek().getType()) {
      case ELEMENTARY:
        comp = ImplElementaryParser.parse(getScanner(), name);
        break;
      case COMPOSITION:
        comp = ImplCompositionParser.parse(getScanner(), name);
        break;
      case HFSM:
        comp = ImplHfsmParser.parse(getScanner(), name);
        break;
      default: {
        wrongToken(TokenType.ELEMENTARY);
        return null;
      }
    }
    comp.getIface().addAll(list);
    return comp;
  }

  // EBNF compIfaceList: { funcHeader }
  private FunList<FuncHeader> parseCompIfaceList() {
    FunList<FuncHeader> func = new FunList<FuncHeader>();
    while (peek().getType() == TokenType.IDENTIFIER) {
      Token name = expect(TokenType.IDENTIFIER);
      expect(TokenType.COLON);
      switch (peek().getType()) {
        case RESPONSE:
        case SLOT:
        case SIGNAL:
        case QUERY: {
          func.add(parseFuncDef(peek().getType(), name.getData(), true));
          break;
        }
        default: {
          RError.err(ErrorType.Error, peek().getInfo(), "expected slot, signal, query or response");
        }
      }
    }
    return func;
  }

  // EBNF typedef: recordtype | uniontype | enumtype | arraytype | derivatetype
  public Fun parseTypeDef(String name) {
    switch (peek().getType()) {
      case RECORD:
        return parseRecordType(name);
      case UNION:
        return parseUnionType(name);
      case ENUM:
        return parseEnumType(name);
      case IDENTIFIER:
        return parseDerivateType();
      default:
        RError.err(ErrorType.Fatal, peek().getInfo(), "Expected record, union or type reference");
        return null;
    }

  }

  // EBNF derivatetype: ref ";"
  private Reference parseDerivateType() {
    Reference ref = expr().parseRef();
    expect(TokenType.SEMI);
    return ref;
  }

  // EBNF recordtype: "Record" { recordElem } "end"
  private Type parseRecordType(String name) {
    Token tok = expect(TokenType.RECORD);
    RecordType ret = new RecordType(tok.getInfo(), name);
    while (peek().getType() != TokenType.END) {
      ret.getElement().addAll(parseRecordElem());
    }
    expect(TokenType.END);
    RError.err(ErrorType.Warning, tok.getInfo(), "Type checking of records is not yet fully implemented");
    return ret;
  }

  // EBNF unionType: "Union" { recordElem } "end"
  private Type parseUnionType(String name) {
    Token tok = expect(TokenType.UNION);

    UnionType ret = new UnionType(tok.getInfo(), name);

    while (peek().getType() != TokenType.END) {
      ret.getElement().addAll(parseRecordElem());
    }
    expect(TokenType.END);
    RError.err(ErrorType.Warning, tok.getInfo(), "Unions are probably broken and do not work as intended");
    return ret;
  }

  // EBNF enumType: "Enum" { enumElem } "end"
  private Type parseEnumType(String name) {
    Token tok = expect(TokenType.ENUM);

    EnumType type = new EnumType(tok.getInfo(), name);

    while (peek().getType() != TokenType.END) {
      Token elemTok = parseEnumElem();
      type.getElement().add(new EnumElement(elemTok.getInfo(), elemTok.getData()));
    }

    expect(TokenType.END);
    return type;
  }

  // EBNF recordElem: id { "," id } ":" ref ";"
  private List<NamedElement> parseRecordElem() {
    ArrayList<Token> id = new ArrayList<Token>();
    do {
      id.add(expect(TokenType.IDENTIFIER));
    } while (consumeIfEqual(TokenType.COMMA));
    expect(TokenType.COLON);
    Reference type = expr().parseRef();
    expect(TokenType.SEMI);

    List<NamedElement> res = new ArrayList<NamedElement>(id.size());
    for (Token name : id) {
      Reference ctype = Copy.copy(type);
      res.add(new NamedElement(name.getInfo(), name.getData(), ctype));
    }

    return res;
  }

  // EBNF enumElem: id ";"
  private Token parseEnumElem() {
    Token ret = expect(TokenType.IDENTIFIER);
    expect(TokenType.SEMI);
    return ret;
  }

}
