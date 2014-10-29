package parser;

import fun.statement.Block;

abstract public class ImplBaseParser extends BaseParser {

  public ImplBaseParser(Scanner scanner) {
    super(scanner);
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

}
