package ast.pass.input.xml.parser.statement;

import ast.data.Ast;
import ast.data.expression.Expression;
import ast.data.reference.Reference;
import ast.data.statement.AssignmentSingle;
import ast.pass.input.xml.infrastructure.Parser;
import ast.pass.input.xml.infrastructure.XmlParser;
import ast.pass.input.xml.scanner.ExpectionParser;
import error.RizzlyError;

public class SingleAssignmentParser implements Parser {
  private static final String Name = "SingleAssignment";
  private final ExpectionParser stream;
  private final XmlParser parser;
  private final RizzlyError error;

  public SingleAssignmentParser(ExpectionParser stream, XmlParser parser, RizzlyError error) {
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
    return type == AssignmentSingle.class ? this : null;
  }

  @Override
  public AssignmentSingle parse() {
    stream.elementStart(Name);
    Reference left = parser.itemOf(Reference.class);
    Expression right = parser.itemOf(Expression.class);
    stream.elementEnd();

    return new AssignmentSingle(left, right);
  }

}
