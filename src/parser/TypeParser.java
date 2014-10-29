package parser;

import java.util.ArrayList;
import java.util.List;

import error.ErrorType;
import error.RError;
import fun.Copy;
import fun.Fun;
import fun.expression.reference.Reference;
import fun.other.CompImpl;
import fun.type.Type;
import fun.type.base.EnumElement;
import fun.type.base.EnumType;
import fun.type.composed.NamedElement;
import fun.type.composed.RecordType;
import fun.type.composed.UnionType;

public class TypeParser extends BaseParser {

  public TypeParser(Scanner scanner) {
    super(scanner);
  }

  // EBNF compdecl: compIfaceList component "end"
  public CompImpl parseCompdecl(String name) {
    switch (peek().getType()) {
      case ELEMENTARY:
        return ImplElementaryParser.parse(getScanner(), name);
      case COMPOSITION:
        return ImplCompositionParser.parse(getScanner(), name);
      case HFSM:
        return ImplHfsmParser.parse(getScanner(), name);
      default: {
        wrongToken(TokenType.ELEMENTARY);
        return null;
      }
    }
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
