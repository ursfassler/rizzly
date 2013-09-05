package parser;

import java.util.ArrayList;
import java.util.List;

import common.Direction;
import common.ElementInfo;

import error.ErrorType;
import error.RError;
import fun.Copy;
import fun.expression.reference.Reference;
import fun.function.FunctionHeader;
import fun.generator.ComponentGenerator;
import fun.generator.InterfaceGenerator;
import fun.generator.TypeGenerator;
import fun.other.Component;
import fun.other.Interface;
import fun.type.Type;
import fun.type.base.EnumElement;
import fun.type.base.EnumType;
import fun.type.base.TypeAlias;
import fun.type.composed.NamedElement;
import fun.type.composed.RecordType;
import fun.type.composed.UnionType;
import fun.variable.CompfuncParameter;
import fun.variable.IfaceUse;

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
  protected List<TypeGenerator> parseTypeSection() {
    expect(TokenType.TYPE_SEC);
    List<TypeGenerator> ret = new ArrayList<TypeGenerator>();
    do {
      ret.add(parseTypedecl());
    } while (peek().getType() == TokenType.IDENTIFIER);
    return ret;
  }

  // EBNF ifacedecl: id genericParam interface
  private InterfaceGenerator parseIfacedecl() {
    Token name = expect(TokenType.IDENTIFIER);
    List<CompfuncParameter> genpam;
    if (peek().getType() == TokenType.OPENCURLY) {
      genpam = parseGenericParam();
    } else {
      genpam = new ArrayList<CompfuncParameter>();
    }

    Interface type = parseInterface(name.getInfo());

    InterfaceGenerator func = new InterfaceGenerator(name.getInfo(), name.getData(), genpam, type);
    return func;
  }

  // EBNF compdecl: id genericParam  component
  private ComponentGenerator parseCompdecl() {
    Token name = expect(TokenType.IDENTIFIER);
    List<CompfuncParameter> genpam;
    if (peek().getType() == TokenType.OPENCURLY) {
      genpam = parseGenericParam();
    } else {
      genpam = new ArrayList<CompfuncParameter>();
    }

    Component type = parseComponent(name.getInfo());

    ComponentGenerator func = new ComponentGenerator(name.getInfo(), name.getData(), genpam, type);
    return func;
  }

  // EBNF component: [ "input" fileUseList ] [ "output" fileUseList ] componentImplementation "end"
  private Component parseComponent(ElementInfo info) {
    List<IfaceUse> in = new ArrayList<IfaceUse>();
    List<IfaceUse> out = new ArrayList<IfaceUse>();

    if (consumeIfEqual(TokenType.INPUT)) {
      in = parseFileUseList(IfaceUse.class);
    }

    if (consumeIfEqual(TokenType.OUTPUT)) {
      out = parseFileUseList(IfaceUse.class);
    }

    Component iface = parseComponentImplementation(info);

    iface.getIface(Direction.in).addAll(in);
    iface.getIface(Direction.out).addAll(out);

    expect(TokenType.END);

    return iface;
  }

  // EBNF interface: functionPrototype { functionPrototype } "end"
  private Interface parseInterface(ElementInfo info) {
    Interface iface = new Interface(info);
    do {
      FunctionHeader proto = parseFunctionPrototype();
      iface.getPrototype().add(proto);
    } while (peek().getType() == TokenType.FUNCTION);

    expect(TokenType.END);

    return iface;
  }


  // EBNF typedecl: id genericParam "=" typedef
  private TypeGenerator parseTypedecl() {
    Token name = expect(TokenType.IDENTIFIER);
    List<CompfuncParameter> genpam;
    if (peek().getType() == TokenType.OPENCURLY) {
      genpam = parseGenericParam();
    } else {
      genpam = new ArrayList<CompfuncParameter>(); // FIXME or directly generate a type here?
    }
    expect(TokenType.EQUAL);

    Type type = parseTypeDef();

    TypeGenerator func = new TypeGenerator(name.getInfo(), name.getData(), genpam, type);
    return func;
  }

  // EBNF typedef: recordtype | uniontype | enumtype | arraytype | derivatetype
  private Type parseTypeDef() {
    switch (peek().getType()) {
    case RECORD:
      return parseRecordType();
    case UNION:
      return parseUnionType();
    case ENUM:
      return parseEnumType();
    case IDENTIFIER:
      return parseDerivateType();
    default:
      RError.err(ErrorType.Fatal, peek().getInfo(), "Expected record, union or type reference");
      return null;
    }

  }

  // EBNF derivatetype: ref ";"
  private Type parseDerivateType() {
    Reference ref = expr().parseRef();
    expect(TokenType.SEMI);
    return new TypeAlias(ref.getInfo(), ref);
  }

  // EBNF recordtype: "Record" { recordElem } "end"
  private Type parseRecordType() {
    Token tok = expect(TokenType.RECORD);
    RecordType ret = new RecordType(tok.getInfo());
    while (peek().getType() != TokenType.END) {
      ret.getElement().addAll(parseRecordElem());
    }
    expect(TokenType.END);
    return ret;
  }

  // EBNF unionType: "Union" "(" id ")" { recordElem } "end"
  private Type parseUnionType() {
    Token tok = expect(TokenType.UNION);
    expect(TokenType.OPENPAREN);
    String selector = expect(TokenType.IDENTIFIER).getData();
    expect(TokenType.CLOSEPAREN);
    
    UnionType ret = new UnionType(tok.getInfo(),selector);
    
    while (peek().getType() != TokenType.END) {
      ret.getElement().addAll(parseRecordElem());
    }
    expect(TokenType.END);
    return ret;
  }

  // EBNF enumType: "Enum" { enumElem } "end"
  private Type parseEnumType() {
    Token tok = expect(TokenType.ENUM);
    EnumType type = new EnumType(tok.getInfo());
    while (peek().getType() != TokenType.END) {
      EnumElement elem = parseEnumElem();
      EnumElement old = type.find(elem.getName());
      if (old != null) {
        RError.err(ErrorType.Fatal, elem.getInfo(), "Name \"" + elem.getName() + "\" already defined at " + old.getInfo());
      }
      type.getElement().add(elem);
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
  private EnumElement parseEnumElem() {
    Token ret = expect(TokenType.IDENTIFIER);
    expect(TokenType.SEMI);
    return new EnumElement(ret.getInfo(), ret.getData());
  }

  // EBNF componentImplementation: "implementation" ( implementationElementary | implementationComposition )
  private Component parseComponentImplementation(ElementInfo info) {
    expect(TokenType.IMPLEMENTATION);
    switch (peek().getType()) {
    case ELEMENTARY:
      return ImplElementaryParser.parse(getScanner());
    case COMPOSITION:
      return ImplCompositionParser.parse(getScanner());
    case HFSM:
      return ImplHfsmParser.parse(getScanner());
    default: {
      wrongToken(TokenType.ELEMENTARY);
      return null;
    }
    }
  }

}
