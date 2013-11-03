package parser;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import common.Direction;

import error.ErrorType;
import error.RError;
import fun.Copy;
import fun.expression.Number;
import fun.expression.reference.RefName;
import fun.expression.reference.Reference;
import fun.expression.reference.ReferenceUnlinked;
import fun.function.FunctionHeader;
import fun.other.Component;
import fun.other.Generator;
import fun.other.ListOfNamed;
import fun.type.Type;
import fun.type.base.EnumElement;
import fun.type.base.EnumType;
import fun.type.base.TypeAlias;
import fun.type.composed.NamedElement;
import fun.type.composed.RecordType;
import fun.type.composed.UnionSelector;
import fun.type.composed.UnionType;
import fun.variable.Constant;
import fun.variable.TemplateParameter;

public class TypeParser extends BaseParser {

  public TypeParser(Scanner scanner) {
    super(scanner);
  }

  // EBNF compdefsec: "component" compdecl { compdecl }
  protected List<Generator> parseComponentSection() {
    expect(TokenType.COMPONENT);
    List<Generator> ret = new ArrayList<Generator>();
    do {
      ret.add(parseCompdecl());
    } while (peek().getType() == TokenType.IDENTIFIER);
    return ret;
  }

  // EBNF typesec: "type" typedecl { typedecl }
  protected List<Generator> parseTypeSection(ListOfNamed<Constant> constants) {
    expect(TokenType.TYPE_SEC);
    List<Generator> ret = new ArrayList<Generator>();
    do {
      ret.add(parseTypedecl(constants));
    } while (peek().getType() == TokenType.IDENTIFIER);
    return ret;
  }

  // EBNF compdecl: id genericParam component
  private Generator parseCompdecl() {
    Token name = expect(TokenType.IDENTIFIER);
    List<TemplateParameter> genpam;
    if (peek().getType() == TokenType.OPENCURLY) {
      genpam = parseGenericParam();
    } else {
      genpam = new ArrayList<TemplateParameter>();
    }

    Component type = parseComponent(name);

    Generator func = new Generator(name.getInfo(), type, genpam);
    return func;
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
  private Generator parseTypedecl(ListOfNamed<Constant> constants) {
    Token name = expect(TokenType.IDENTIFIER);
    List<TemplateParameter> genpam;
    if (peek().getType() == TokenType.OPENCURLY) {
      genpam = parseGenericParam();
    } else {
      genpam = new ArrayList<TemplateParameter>(); // FIXME or directly generate a type here?
    }
    expect(TokenType.EQUAL);

    Type type = parseTypeDef(name.getData(), constants);

    Generator func = new Generator(name.getInfo(), type, genpam);
    return func;
  }

  // EBNF typedef: recordtype | uniontype | enumtype | arraytype | derivatetype
  private Type parseTypeDef(String name, ListOfNamed<Constant> constants) {
    switch (peek().getType()) {
    case RECORD:
      return parseRecordType(name);
    case UNION:
      return parseUnionType(name);
    case ENUM:
      return parseEnumType(name, constants);
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
    return ret;
  }

  // EBNF unionType: "Union" "(" id ")" { recordElem } "end"
  private Type parseUnionType(String name) {
    Token tok = expect(TokenType.UNION);
    expect(TokenType.OPENPAREN);
    Token selector = expect(TokenType.IDENTIFIER);
    expect(TokenType.CLOSEPAREN);

    UnionType ret = new UnionType(tok.getInfo(), name, new UnionSelector(selector.getInfo(), selector.getData()));

    while (peek().getType() != TokenType.END) {
      ret.getElement().addAll(parseRecordElem());
    }
    expect(TokenType.END);
    return ret;
  }

  // EBNF enumType: "Enum" { enumElem } "end"
  private Type parseEnumType(String name, ListOfNamed<Constant> constants) {
    Token tok = expect(TokenType.ENUM);

    EnumType type = new EnumType(tok.getInfo(), name);

    while (peek().getType() != TokenType.END) {
      Token elemTok = parseEnumElem();

      Constant old = constants.find(elemTok.getData());
      if (old != null) {
        RError.err(ErrorType.Hint, old.getInfo(), "first definition was here");
        RError.err(ErrorType.Error, elemTok.getInfo(), "Name \"" + elemTok.getData() + "\" already defined");
      } else {
        ReferenceUnlinked typeRef = new ReferenceUnlinked(elemTok.getInfo());
        typeRef.getOffset().add(new RefName(elemTok.getInfo(), name));
        EnumElement elem = new EnumElement(elemTok.getInfo(), elemTok.getData(), typeRef);
        elem.setDef(new Number(elemTok.getInfo(), BigInteger.valueOf(type.getElement().size())));
        constants.add(elem);

        ReferenceUnlinked elemRef = new ReferenceUnlinked(elemTok.getInfo());
        elemRef.getOffset().add(new RefName(elemTok.getInfo(), elem.getName()));
        type.getElement().add(elemRef);
      }
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
