package fun.statement;

import common.ElementInfo;

import fun.other.FunList;

public class Block extends Statement {
  final private FunList<Statement> statements = new FunList<Statement>();

  public Block(ElementInfo info) {
    super(info);
  }

  public FunList<Statement> getStatements() {
    return statements;
  }

  @Override
  public String toString() {
    return "block" + Long.toHexString(this.hashCode());
  }
}
