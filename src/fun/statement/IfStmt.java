package fun.statement;

import common.ElementInfo;

import fun.other.FunList;

/**
 * 
 * @author urs
 */
public class IfStmt extends Statement {

  private FunList<IfOption> option = new FunList<IfOption>();
  private Block defblock;

  public IfStmt(ElementInfo info) {
    super(info);
    defblock = new Block(info);
  }

  public Block getDefblock() {
    return defblock;
  }

  public void setDefblock(Block defblock) {
    assert (defblock != null);
    this.defblock = defblock;
  }

  public FunList<IfOption> getOption() {
    return option;
  }

  public void addOption(IfOption opt) {
    option.add(opt);
  }

}
