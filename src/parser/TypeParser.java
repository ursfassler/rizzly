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

import parser.hfsm.ImplHfsmParser;
import parser.scanner.Token;
import parser.scanner.TokenType;
import ast.copy.Copy;
import ast.data.AstList;
import ast.data.Named;
import ast.data.function.Function;
import ast.data.raw.RawComponent;
import ast.data.reference.LinkedReferenceWithOffset_Implementation;
import ast.data.type.Type;
import ast.data.type.TypeReference;
import ast.data.type.base.EnumElement;
import ast.data.type.base.EnumTypeFactory;
import ast.data.type.composed.NamedElement;
import ast.data.type.composed.RecordType;
import ast.data.type.composed.UnsafeUnionType;
import ast.data.type.out.AliasType;
import error.ErrorType;
import error.RError;

//TODO cleanup (mostly components)
//TODO update EBNF
public class TypeParser extends BaseParser {

  public TypeParser(PeekNReader<Token> scanner) {
    super(scanner);
  }

  // EBNF compdecl: "Component" compIfaceList implementation
  public RawComponent parseCompdecl(String name) {
    expect(TokenType.COMPONENT);
    AstList<Function> list = parseCompIfaceList();
    RawComponent comp;
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
  private AstList<Function> parseCompIfaceList() {
    AstList<Function> func = new AstList<Function>();
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
          RError.err(ErrorType.Error, "expected slot, signal, query or response", peek().getMetadata());
        }
      }
    }
    return func;
  }

  // EBNF typedef: recordtype | uniontype | enumtype | arraytype | derivatetype
  public Named parseTypeDef(String name) {
    switch (peek().getType()) {
      case RECORD:
        return parseRecordType(name);
      case UNION:
        return parseUnionType(name);
      case ENUM:
        return parseEnumType(name);
      case IDENTIFIER:
        return parseDerivateType(name);
      default:
        RError.err(ErrorType.Fatal, "Expected record, union or type reference", peek().getMetadata());
        return null;
    }

  }

  // EBNF derivatetype: ref ";"
  private AliasType parseDerivateType(String name) {
    LinkedReferenceWithOffset_Implementation ref = expr().oldParseRef();
    expect(TokenType.SEMI);
    TypeReference typeref = new TypeReference(ref);
    typeref.metadata().add(ref.metadata());
    return new AliasType(ref.metadata(), name, typeref);
  }

  // EBNF recordtype: "Record" { recordElem } "end"
  private Type parseRecordType(String name) {
    Token tok = expect(TokenType.RECORD);
    RecordType ret = new RecordType(name);
    ret.metadata().add(tok.getMetadata());
    while (peek().getType() != TokenType.END) {
      ret.element.addAll(parseRecordElem());
    }
    expect(TokenType.END);
    RError.err(ErrorType.Warning, "Type checking of records is not yet fully implemented", tok.getMetadata());
    return ret;
  }

  // EBNF unionType: "Union" { recordElem } "end"
  private ast.data.type.Type parseUnionType(String name) {
    Token tok = expect(TokenType.UNION);

    UnsafeUnionType ret = new UnsafeUnionType(name);
    ret.metadata().add(tok.getMetadata());

    while (peek().getType() != TokenType.END) {
      ret.element.addAll(parseRecordElem());
    }
    expect(TokenType.END);
    RError.err(ErrorType.Warning, "Unions are probably broken and do not work as intended", tok.getMetadata());
    return ret;
  }

  // EBNF enumType: "Enum" { enumElem } "end"
  private ast.data.type.Type parseEnumType(String name) {
    Token tok = expect(TokenType.ENUM);

    ast.data.type.base.EnumType type = EnumTypeFactory.create(tok.getMetadata(), name);

    while (peek().getType() != TokenType.END) {
      Token elemTok = parseEnumElem();
      type.element.add(new EnumElement(elemTok.getMetadata(), elemTok.getData()));
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
    TypeReference type = expr().parseRefType();
    expect(TokenType.SEMI);

    List<NamedElement> res = new ArrayList<NamedElement>(id.size());
    for (Token name : id) {
      TypeReference ctype = Copy.copy(type);
      res.add(new NamedElement(name.getMetadata(), name.getData(), ctype));
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
