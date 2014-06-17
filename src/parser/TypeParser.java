package parser;

import java.util.ArrayList;
import java.util.List;

import common.Direction;

import error.ErrorType;
import error.RError;
import fun.Copy;
import fun.expression.reference.Reference;
import fun.function.FunctionHeader;
import fun.other.Component;
import fun.type.Type;
import fun.type.TypeGenerator;
import fun.type.base.EnumElement;
import fun.type.base.EnumType;
import fun.type.base.TypeAlias;
import fun.type.composed.NamedElement;
import fun.type.composed.RecordType;
import fun.type.composed.UnionType;
import fun.variable.TemplateParameter;

public class TypeParser extends BaseParser {

  public TypeParser(Scanner scanner) {
    super(scanner);
  }

  // EBNF compdefsec: "component" compdecl { compdecl }
  protected List<Component> parseComponentSection() {
    expect(TokenType.COMPONENT);
    List<Component> ret = new ArrayList<Component>();
    do {
      ret.add(parseCompdecl());
    } while (peek().getType() == TokenType.IDENTIFIER);
    return ret;
  }

  // EBNF typesec: "type" typedecl { typedecl }
  protected List<Type> parseTypeSection() {
    expect(TokenType.TYPE_SEC);
    List<Type> ret = new ArrayList<Type>();
    do {
      ret.add(parseTypedecl());
    } while (peek().getType() == TokenType.IDENTIFIER);
    return ret;
  }

  // EBNF compdecl: id genericParam component
  private Component parseCompdecl() {
    Token name = expect(TokenType.IDENTIFIER);
    List<TemplateParameter> genpam;
    if (peek().getType() == TokenType.OPENCURLY) {
      genpam = parseGenericParam();
    } else {
      genpam = new ArrayList<TemplateParameter>();
    }

    Component type = parseComponent(name);
    type.getTemplateParam().addAll(genpam);

    return type;
  }

  // EBNF component: [ "input" funcDefList ] [ "output" funcDefList ] componentImplementation "end"
  private Component parseComponent(Token name) {
    List<FunctionHeader> in = new ArrayList<FunctionHeader>();
    List<FunctionHeader> out = new ArrayList<FunctionHeader>();

    if (consumeIfEqual(TokenType.INPUT)) {
      in = parseFunctionDefList();
    }

    if (consumeIfEqual(TokenType.OUTPUT)) {
      out = parseFunctionDefList();
    }

    Component iface = parseComponentImplementation(name);

    iface.getIface(Direction.in).addAll(in);
    iface.getIface(Direction.out).addAll(out);

    expect(TokenType.END);

    return iface;
  }

  // EBNF typedecl: id genericParam "=" typedef
  private Type parseTypedecl() {
    Token name = expect(TokenType.IDENTIFIER);
    List<TemplateParameter> genpam;
    if (peek().getType() == TokenType.OPENCURLY) {
      genpam = parseGenericParam();
    } else {
      genpam = null;
    }
    expect(TokenType.EQUAL);

    Type type = parseTypeDef(name.getData());
    if (genpam != null) {
      if (type instanceof TypeGenerator) {
        ((TypeGenerator) type).getTemplateParam().addAll(genpam);
      } else {
        RError.err(ErrorType.Error, name.getInfo(), "Expected type template");
      }

    }

    return type;
  }

  // EBNF typedef: recordtype | uniontype | enumtype | arraytype | derivatetype
  private Type parseTypeDef(String name) {
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
      RError.err(ErrorType.Fatal, peek().getInfo(), "Expected record, union or type reference");
      return null;
    }

  }

  // EBNF derivatetype: ref ";"
  private Type parseDerivateType(String name) {
    Reference ref = expr().parseRef();
    expect(TokenType.SEMI);
    return new TypeAlias(ref.getInfo(), name, ref);
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

  // EBNF componentImplementation: "implementation" ( implementationElementary | implementationComposition )
  private Component parseComponentImplementation(Token name) {
    expect(TokenType.IMPLEMENTATION);
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
}
