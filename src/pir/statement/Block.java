package pir.statement;

import java.util.ArrayList;
import java.util.List;

public class Block extends Statement {
  final private List<Statement> statement = new ArrayList<Statement>();

  public List<Statement> getStatement() {
    return statement;
  }

  @Override
  public String toString() {
    return "begin";
  }
}
