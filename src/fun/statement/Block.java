package fun.statement;

import java.util.ArrayList;
import java.util.List;

import common.ElementInfo;



public class Block extends Statement {
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
}
