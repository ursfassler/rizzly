package pir.statement;

import java.util.ArrayList;
import java.util.List;

public class IfStmt extends Statement {
  private final List<IfStmtEntry> option = new ArrayList<IfStmtEntry>();
  private final Block def;

  public IfStmt(Block def) {
    super();
    this.def = def;
  }

  public List<IfStmtEntry> getOption() {
    return option;
  }

  public Block getDef() {
    return def;
  }

  @Override
  public String toString() {
    return "if";
  }
}
