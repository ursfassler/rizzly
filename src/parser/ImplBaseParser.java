package parser;

import java.util.ArrayList;
import java.util.List;

import fun.function.impl.FuncEntryExit;
import fun.statement.Block;
import fun.variable.CompUse;
import fun.variable.Variable;

abstract public class ImplBaseParser extends BaseParser {

  public ImplBaseParser(Scanner scanner) {
    super(scanner);
  }

  // EBNF compDeclBlock: "component" fileUseList
  protected List<CompUse> parseCompDeclBlock() {
    expect(TokenType.COMPONENT);
    return parseFileUseList(CompUse.class);
  }

  // EBNF varDefBlock: "var" vardefMustinit ";" { vardefMustinit ";" }
  protected <T extends Variable> List<T> parseVarDefBlock(Class<T> kind) {
    expect(TokenType.VAR);
    List<T> res = new ArrayList<T>();
    do {
      res.addAll(parseVarDef(kind, InitType.MustInit));
      expect(TokenType.SEMI);
    } while (peek().getType() == TokenType.IDENTIFIER);
    return res;
  }

  // EBNF entryCode: "entry" block "end"
  protected Block parseEntryCode() {
    expect(TokenType.ENTRY);
    Block entry;
    entry = stmt().parseBlock();
    expect(TokenType.END);
    return entry;
  }

  // EBNF exitCode: "exit" block "end"
  protected Block parseExitCode() {
    expect(TokenType.EXIT);
    Block entry;
    entry = stmt().parseBlock();
    expect(TokenType.END);
    return entry;
  }

  protected FuncEntryExit makeEntryExitFunc(String name, Block body) {
    FuncEntryExit func = new FuncEntryExit(body.getInfo());
    func.setName(name);
    func.setBody(body);
    return func;
  }

}
