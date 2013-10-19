package parser;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import common.Direction;

import error.ErrorType;
import error.RError;
import fun.Copy;
import fun.expression.reference.Reference;
import fun.function.FunctionHeader;
import fun.generator.ComponentGenerator;
import fun.generator.InterfaceGenerator;
import fun.other.Component;
import fun.other.Interface;
import fun.type.Type;
import fun.type.base.EnumType;
import fun.type.base.TypeAlias;
import fun.type.composed.NamedElement;
import fun.type.composed.RecordType;
import fun.type.composed.UnionSelector;
import fun.type.composed.UnionType;
import fun.type.template.UserTypeGenerator;
import fun.variable.IfaceUse;
import fun.variable.TemplateParameter;

public class TypeParser extends BaseParser {

  public TypeParser(Scanner scanner) {
    super(scanner);
  }

  // EBNF ifacedefsec: "interface" ifacedecl { ifacedecl }
  protected List<InterfaceGenerator> parseInterfaceSection() {
    expect(TokenType.INTERFACE);
    List<InterfaceGenerator> ret = new ArrayList<InterfaceGenerator>();
    do {
      ret.add(parseIfacedecl());
    } while (peek().getType() == TokenType.IDENTIFIER);
    return ret;
  }

  // EBNF compdefsec: "component" compdecl { compdecl }
  protected List<ComponentGenerator> parseComponentSection() {
    expect(TokenType.COMPONENT);
    List<ComponentGenerator> ret = new ArrayList<ComponentGenerator>();
    do {
      ret.add(parseCompdecl());
    } while (peek().getType() == TokenType.IDENTIFIER);
    return ret;
  }

  // EBNF typesec: "type" typedecl { typedecl }
  protected List<UserTypeGenerator> parseTypeSection() {
    expect(TokenType.TYPE_SEC);
    List<UserTypeGenerator> ret = new ArrayList<UserTypeGenerator>();
    do {
      ret.add(parseTypedecl());
    } while (peek().getType() == TokenType.IDENTIFIER);
    return ret;
  }

  // EBNF ifacedecl: id genericParam interface
  private InterfaceGenerator parseIfacedecl() {
    Token name = expect(TokenType.IDENTIFIER);
    List<TemplateParameter> genpam;
    if (peek().getType() == TokenType.OPENCURLY) {
      genpam = parseGenericParam();
    } else {
      genpam = new ArrayList<TemplateParameter>();
    }

    Interface type = parseInterface(name);

    InterfaceGenerator func = new InterfaceGenerator(name.getInfo(), name.getData(), genpam, type);
    return func;
  }

  // EBNF compdecl: id genericParam component
  private ComponentGenerator parseCompdecl() {
    Token name = expect(TokenType.IDENTIFIER);
    List<TemplateParameter> genpam;
    if (peek().getType() == TokenType.OPENCURLY) {
      genpam = parseGenericParam();
    } else {
      genpam = new ArrayList<TemplateParameter>();
    }

    Component type = parseComponent(name);

    ComponentGenerator func = new ComponentGenerator(name.getInfo(), name.getData(), genpam, type);
    return func;
  }

  // EBNF component: [ "input" fileUseList ] [ "output" fileUseList ] componentImplementation "end"
  private Component parseComponent(Token name) {
    List<IfaceUse> in = new ArrayList<IfaceUse>();
    List<IfaceUse> out = new ArrayList<IfaceUse>();

    if (consumeIfEqual(TokenType.INPUT)) {
      in = parseFileUseList(IfaceUse.class);
    }

    if (consumeIfEqual(TokenType.OUTPUT)) {
      out = parseFileUseList(IfaceUse.class);
    }

    Component iface = parseComponentImplementation(name);

    iface.getIface(Direction.in).addAll(in);
    iface.getIface(Direction.out).addAll(out);

    expect(TokenType.END);

    return iface;
  }

  // EBNF interface: functionPrototype { functionPrototype } "end"
  private Interface parseInterface(Token name) {
    Interface iface = new Interface(name.getInfo(), name.getData());
    do {
      FunctionHeader proto = parseFunctionPrototype();
      iface.getPrototype().add(proto);
    } while (peek().getType() == TokenType.FUNCTION);

    expect(TokenType.END);

    return iface;
  }

  // EBNF typedecl: id genericParam "=" typedef
  private UserTypeGenerator parseTypedecl() {
    Token name = expect(TokenType.IDENTIFIER);
    List<TemplateParameter> genpam;
    if (peek().getType() == TokenType.OPENCURLY) {
      genpam = parseGenericParam();
    } else {
      genpam = new ArrayList<TemplateParameter>(); // FIXME or directly generate a type here?
    }
    expect(TokenType.EQUAL);

    Type type = parseTypeDef(name.getData());

    UserTypeGenerator func = new UserTypeGenerator(name.getInfo(), name.getData(), genpam, type);
    return func;
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
  private Type parseEnumType(String name) {
    Token tok = expect(TokenType.ENUM);
    Set<String> names = new HashSet<String>();

    EnumType type = new EnumType(tok.getInfo(), name);

    while (peek().getType() != TokenType.END) {
      Token elem = parseEnumElem();
      if (names.contains(elem.getData())) {
        RError.err(ErrorType.Fatal, elem.getInfo(), "Name \"" + elem.getData() + "\" already defined");
      } else {
        names.add(elem.getData());
      }
      type.createElement(elem.getData(), elem.getInfo());
    }

    // FIXME what to do with elements?

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
