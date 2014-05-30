package parser;

import java.util.ArrayList;
import java.util.List;

import common.Metadata;

import error.ErrorType;
import error.RError;
import fun.Copy;
import fun.expression.AnyValue;
import fun.expression.Expression;
import fun.expression.reference.Reference;
import fun.function.FuncWithBody;
import fun.function.FunctionHeader;
import fun.function.impl.FuncGlobal;
import fun.function.impl.FuncPrivateRet;
import fun.function.impl.FuncPrivateVoid;
import fun.function.impl.FuncProtRet;
import fun.function.impl.FuncProtVoid;
import fun.type.base.AnyType;
import fun.variable.Constant;
import fun.variable.FuncVariable;
import fun.variable.TemplateParameter;
import fun.variable.Variable;
import fun.variable.VariableFactory;

public class BaseParser extends Parser {

  enum InitType {
    NoInit, CanInit, MustInit
  }

  public BaseParser(Scanner scanner) {
    super(scanner);
  }

  protected StatementParser stmt() {
    return new StatementParser(getScanner());
  }

  protected TypeParser type() {
    return new TypeParser(getScanner());
  }

  protected ExpressionParser expr() {
    return new ExpressionParser(getScanner());
  }

  // EBNF funcDefList: funcDef { funcDef }
  protected List<FunctionHeader> parseFunctionDefList() {
    List<FunctionHeader> res = new ArrayList<FunctionHeader>();
    do {
      res.add(parseFunctionDef());
    } while (peek().getType() == TokenType.IDENTIFIER);
    return res;
  }

  // EBNF fileUseList: vardef ";" { vardef ";" }
  protected <T extends Variable> List<T> parseFileUseList(Class<T> kind) {
    List<T> res = new ArrayList<T>();
    do {
      List<T> vardefs = stmt().parseVarDef(kind, InitType.NoInit);
      expect(TokenType.SEMI);

      ArrayList<Metadata> meta = getMetadata();
      for (T var : vardefs) {
        var.getInfo().getMetadata().addAll(meta);
      }

      res.addAll(vardefs);
    } while (peek().getType() == TokenType.IDENTIFIER);
    return res;
  }

  // EBNF funcDef: id vardeflist [ ":" typeref ] ";"
  protected FunctionHeader parseFunctionDef() {
    Token tok = expect(TokenType.IDENTIFIER);

    List<FuncVariable> varlist = parseVardefList();

    FunctionHeader func;
    if (consumeIfEqual(TokenType.COLON)) {
      Reference ref = expr().parseRef();
      FuncProtRet rfunc = new FuncProtRet(tok.getInfo());
      rfunc.setRet(ref);
      func = rfunc;
    } else {
      func = new FuncProtVoid(tok.getInfo());
    }

    expect(TokenType.SEMI);

    func.setName(tok.getData());
    func.getParam().addAll(varlist);

    return func;
  }

  // EBNF constDefBlock: "const" constdef { constdef }
  protected <T extends Constant> List<T> parseConstDefBlock(Class<T> kind) {
    List<T> res = new ArrayList<T>();
    expect(TokenType.CONST);
    do {
      res.add(parseConstDef(kind));
    } while (peek().getType() == TokenType.IDENTIFIER);
    return res;
  }

  // EBNF globalFunction: "function" id genericParam vardeflist ":" ref block "end"
  protected FuncGlobal parseGlobalFunction() {
    Token tok = expect(TokenType.FUNCTION);

    FuncGlobal func = new FuncGlobal(tok.getInfo());
    func.setName(expect(TokenType.IDENTIFIER).getData());

    func.getTemplateParam().addAll(parseGenericParam());

    func.getParam().addAll(parseVardefList());

    expect(TokenType.COLON);
    Reference ref = expr().parseRef();
    func.setRet(ref);

    func.setBody(stmt().parseBlock());
    expect(TokenType.END);

    return func;
  }

  // TODO can we merge it with another function parser?
  // EBNF privateFunction: "function" id vardeflist [ ":" typeref ] block "end"
  protected FunctionHeader parsePrivateFunction() {
    Token tok = expect(TokenType.FUNCTION);

    Token name = expect(TokenType.IDENTIFIER);

    List<FuncVariable> varlist = parseVardefList();

    FunctionHeader func;
    if (consumeIfEqual(TokenType.COLON)) {
      FuncPrivateRet rfunc = new FuncPrivateRet(tok.getInfo());
      Reference ref = expr().parseRef();
      rfunc.setRet(ref);
      func = rfunc;
    } else {
      func = new FuncPrivateVoid(tok.getInfo());
    }

    func.setName(name.getData());
    func.getParam().addAll(varlist);

    ((FuncWithBody) func).setBody(stmt().parseBlock());
    expect(TokenType.END);

    return func;
  }

  // EBNF vardefNoinit: id { "," id } ":" typeref
  // EBNF vardefCaninit: id { "," id } ":" typeref [ "=" exprList ]
  // EBNF vardefMustinit: id { "," id } ":" typeref "=" exprList
  protected <T extends Variable> List<T> parseVarDef(Class<T> kind, InitType init) {
    List<Token> names = new ArrayList<Token>();
    do {
      Token id = expect(TokenType.IDENTIFIER);
      names.add(id);
    } while (consumeIfEqual(TokenType.COMMA));

    expect(TokenType.COLON);
    Reference type = expr().parseRef();

    List<Expression> def = new ArrayList<Expression>();
    if ((init == InitType.MustInit) || ((init == InitType.CanInit) && (peek().getType() == TokenType.EQUAL))) {
      expect(TokenType.EQUAL);
      def = expr().parseExprList();
    } else {
      for (Token name : names) {
        def.add(new AnyValue(name.getInfo()));
      }
    }

    if (names.size() != def.size()) {
      RError.err(ErrorType.Error, names.get(0).getInfo(), "expected " + names.size() + " init values, got " + def.size());
      return null;
    }

    List<T> ret = new ArrayList<T>();
    for (int i = 0; i < names.size(); i++) {
      Reference ntype = Copy.copy(type);
      T var;
      if (init == InitType.NoInit) {
        var = VariableFactory.create(kind, names.get(i).getInfo(), names.get(i).getData(), ntype);
      } else {
        var = VariableFactory.create(kind, names.get(i).getInfo(), names.get(i).getData(), ntype, def.get(i));
      }
      ret.add((T) var);
    }

    return ret;
  }

  // EBNF genericParam: [ "{" vardef { ";" vardef } "}" ]
  protected List<TemplateParameter> parseGenericParam() {
    ArrayList<TemplateParameter> ret = new ArrayList<TemplateParameter>();
    if (consumeIfEqual(TokenType.OPENCURLY)) {
      do {
        List<TemplateParameter> param = parseVarDef(TemplateParameter.class, InitType.NoInit);
        ret.addAll(param);
      } while (consumeIfEqual(TokenType.SEMI));
      expect(TokenType.CLOSECURLY);
    }
    return ret;
  }

  // EBNF vardeflist: "(" [ vardef { ";" vardef } ] ")"
  protected List<FuncVariable> parseVardefList() {
    List<FuncVariable> res = new ArrayList<FuncVariable>();
    expect(TokenType.OPENPAREN);
    if (peek().getType() == TokenType.IDENTIFIER) {
      do {
        List<FuncVariable> list = stmt().parseVarDef(FuncVariable.class, InitType.NoInit);
        res.addAll(list);
      } while (consumeIfEqual(TokenType.SEMI));
    }
    expect(TokenType.CLOSEPAREN);
    return res;
  }

  // EBNF constdef: id [ ":" typeref ] "=" expr ";"
  private <T extends Constant> T parseConstDef(Class<T> kind) {
    Token id = expect(TokenType.IDENTIFIER);
    Reference type;
    if (consumeIfEqual(TokenType.COLON)) {
      type = expr().parseRef();
    } else {
      type = new Reference(id.getInfo(), AnyType.NAME);
    }
    expect(TokenType.EQUAL);
    Expression value = expr().parse();
    expect(TokenType.SEMI);

    T ret = VariableFactory.create(kind, id.getInfo(), id.getData(), type, value);
    return ret;
  }

}
