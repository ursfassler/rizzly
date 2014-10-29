package evl.statement;

import common.ElementInfo;

import evl.other.EvlList;

public class Block extends Statement {
  final private EvlList<Statement> statements = new EvlList<Statement>();

  public Block(ElementInfo info) {
    super(info);
  }

  public EvlList<Statement> getStatements() {
    return statements;
  }

  @Override
  public String toString() {
    return "block" + Long.toHexString(this.hashCode());
  }
}
