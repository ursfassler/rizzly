package parser;

import java.util.ArrayList;
import java.util.List;

import util.Pair;
import fun.Copy;
import fun.expression.Expression;
import fun.expression.reference.RefName;
import fun.expression.reference.Reference;
import fun.expression.reference.ReferenceUnlinked;
import fun.function.FuncWithBody;
import fun.function.FunctionHeader;
import fun.function.impl.FuncGlobal;
import fun.function.impl.FuncPrivateRet;
import fun.function.impl.FuncPrivateVoid;
import fun.function.impl.FuncProtRet;
import fun.function.impl.FuncProtVoid;
import fun.statement.Assignment;
import fun.statement.Statement;
import fun.statement.VarDefStmt;
import fun.variable.CompfuncParameter;
import fun.variable.Constant;
import fun.variable.FuncVariable;
import fun.variable.Variable;
import fun.variable.VariableFactory;

public class BaseParser extends Parser {

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

  protected <T extends Variable> List<T> parseVarDefType(Class<T> kind, List<Token> names) {
    expect(TokenType.COLON);
    Reference type = expr().parseRef();

    List<T> varlist = new ArrayList<T>();
    for (Token id : names) {
      Reference ntype = Copy.copy(type);

      T var = VariableFactory.create(kind, id.getInfo(), id.getData(), ntype);
      var.setName(id.getData());
      varlist.add((T) var);
    }
    return varlist;
  }

  // EBNF fileUseList: vardef ";" { vardef ";" }
  protected <T extends Variable> List<T> parseFileUseList(Class<T> kind) {
    List<T> res = new ArrayList<T>();
    do {
      res.addAll(stmt().parseVarDef(kind));
      expect(TokenType.SEMI);
    } while (peek().getType() == TokenType.IDENTIFIER);
    return res;
  }

  // EBNF functionPrototype: "function" id vardeflist [ ":" typeref ] ";"
  protected FunctionHeader parseFunctionPrototype() {
    Token tok = expect(TokenType.FUNCTION);

    String name = expect(TokenType.IDENTIFIER).getData();

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

    func.setName(name);
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

  // EBNF globalFunction: "function" id vardeflist block "end"
  protected FuncGlobal parseGlobalFunction() {
    Token tok = expect(TokenType.FUNCTION);

    FuncGlobal func = new FuncGlobal(tok.getInfo());
    func.setName(expect(TokenType.IDENTIFIER).getData());
    func.getParam().addAll(parseVardefList());

    expect(TokenType.COLON);
    Reference ref = expr().parseRef();
    func.setRet(ref);

    func.setBody(stmt().parseBlock());
    expect(TokenType.END);

    return func;
  }

  // EBNF privateFunction: "function" designator vardeflist [ ":" typeref ] block "end"
  protected Pair<List<String>, FunctionHeader> parsePrivateFunction() {
    Token tok = expect(TokenType.FUNCTION);

    List<String> name = parseDesignator();
    String funcName = name.get(name.size() - 1);
    name.remove(name.size() - 1);

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

    func.setName(funcName);
    func.getParam().addAll(varlist);

    ((FuncWithBody) func).setBody(stmt().parseBlock());
    expect(TokenType.END);

    return new Pair<List<String>, FunctionHeader>(name, func);
  }

  // EBNF vardef: id { "," id } ":" typeref
  protected <T extends Variable> List<T> parseVarDef(Class<T> kind) {
    List<Token> names = new ArrayList<Token>();
    do {
      Token id = expect(TokenType.IDENTIFIER);
      names.add(id);
    } while (consumeIfEqual(TokenType.COMMA));
    return parseVarDefType(kind, names);
  }

  // EBNF genericParam: [ "{" vardef { ";" vardef } "}" ]
  protected List<CompfuncParameter> parseGenericParam() {
    ArrayList<CompfuncParameter> ret = new ArrayList<CompfuncParameter>();
    if (consumeIfEqual(TokenType.OPENCURLY)) {
      do {
        List<CompfuncParameter> param = parseVarDef(CompfuncParameter.class);
        ret.addAll(param);
      } while (consumeIfEqual(TokenType.SEMI));
      expect(TokenType.CLOSECURLY);
    }
    return ret;
  }

  // EBNF vardefinit: vardef [ "=" exprList ]
  protected List<Statement> parseVarDefInit(List<FuncVariable> vardef) {
    List<Statement> stmtlist = new ArrayList<Statement>();

    for (FuncVariable var : vardef) {
      VarDefStmt stmt = new VarDefStmt(var.getInfo(), var);
      stmtlist.add(stmt);
    }

    if (consumeIfEqual(TokenType.EQUAL)) {
      List<Expression> def = expr().parseExprList();
      assert (def.size() == vardef.size());
      for (int i = 0; i < vardef.size(); i++) {
        Expression expr = def.get(i);
        FuncVariable var = vardef.get(i);
        Reference ref = new ReferenceUnlinked(expr.getInfo());
        ref.getOffset().add(new RefName(expr.getInfo(), var.getName()));
        Assignment stmt = new Assignment(expr.getInfo(), ref, expr);
        stmtlist.add(stmt);
      }
    }

    return stmtlist;
  }

  // EBNF vardeflist: [ "(" [ vardef { ";" vardef } ] ")" ]
  protected List<FuncVariable> parseVardefList() {
    List<FuncVariable> res = new ArrayList<FuncVariable>();
    if (consumeIfEqual(TokenType.OPENPAREN)) {
      if (peek().getType() == TokenType.IDENTIFIER) {
        do {
          List<FuncVariable> list = stmt().parseVarDef(FuncVariable.class);
          res.addAll(list);
        } while (consumeIfEqual(TokenType.SEMI));
      }
      expect(TokenType.CLOSEPAREN);
    }
    return res;
  }

  // EBNF constdef: id [ ":" typeref ] "=" expr ";"
  private <T extends Constant> T parseConstDef(Class<T> kind) {
    Token id = expect(TokenType.IDENTIFIER);
    Reference type;
    if (consumeIfEqual(TokenType.COLON)) {
      type = expr().parseRef();
    } else {
      expect(TokenType.COLON); // untyped constants not yet implemented
      // type = new RefUnlinked(id.getInfo(), UnknownType.NAME);
      type = null;
    }
    expect(TokenType.EQUAL);
    Expression value = expr().parse();
    expect(TokenType.SEMI);

    T ret = VariableFactory.create(kind, id.getInfo(), id.getData(), type);
    ret.setDef(value);
    return ret;
  }

}
