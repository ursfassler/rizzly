package evl.statement;

import common.ElementInfo;

import evl.other.EvlList;

/**
 * 
 * @author urs
 */
public class IfStmt extends Statement {

  private EvlList<IfOption> option = new EvlList<IfOption>();
  private Block defblock;

  public IfStmt(ElementInfo info, EvlList<IfOption> option, Block defblock) {
    super(info);
    this.option = new EvlList<IfOption>(option);
    this.defblock = defblock;
  }

  public Block getDefblock() {
    return defblock;
  }

  public void setDefblock(Block defblock) {
    assert (defblock != null);
    this.defblock = defblock;
  }

  public EvlList<IfOption> getOption() {
    return option;
  }

}
