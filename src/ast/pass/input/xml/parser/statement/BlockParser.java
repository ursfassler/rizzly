package ast.pass.input.xml.parser.statement;

import ast.data.Ast;
import ast.data.AstList;
import ast.data.statement.Block;
import ast.data.statement.Statement;
import ast.pass.input.xml.infrastructure.Parser;
import ast.pass.input.xml.infrastructure.XmlParser;
import ast.pass.input.xml.scanner.ExpectionParser;
import error.RizzlyError;

public class BlockParser implements Parser {
  private static final String Name = "Block";
  private final ExpectionParser stream;
  private final XmlParser parser;
  private final RizzlyError error;

  public BlockParser(ExpectionParser stream, XmlParser parser, RizzlyError error) {
    this.stream = stream;
    this.parser = parser;
    this.error = error;
  }

  @Override
  public Parser parserFor(String name) {
    return Name.equals(name) ? this : null;
  }

  @Override
  public Parser parserFor(Class<? extends Ast> type) {
    return type == Block.class ? this : null;
  }

  @Override
  public Block parse() {
    stream.elementStart(Name);
    AstList<Statement> statements = parser.itemsOf(Statement.class);
    stream.elementEnd();

    Block object = new Block();
    object.statements.addAll(statements);
    return object;
  }

}
