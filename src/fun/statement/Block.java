package fun.statement;

import java.util.ArrayList;
import java.util.List;

import common.ElementInfo;

import fun.function.FunctionBodyImplementation;

public class Block extends Statement implements FunctionBodyImplementation {
  final private List<Statement> statements = new ArrayList<Statement>();

  public Block(ElementInfo info) {
    super(info);
  }

  public List<Statement> getStatements() {
    return statements;
  }

  @Override
  public String toString() {
    return "block" + Long.toHexString(this.hashCode());
  }

  @Override
  public boolean isEmpty() {
    return statements.isEmpty();
  }
}
