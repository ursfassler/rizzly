package evl.statement;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import common.ElementInfo;

/**
 * 
 * @author urs
 */
public class IfStmt extends Statement {

  private List<IfOption> option = new ArrayList<IfOption>();
  private Block defblock;

  public IfStmt(ElementInfo info, Collection<IfOption> option, Block defblock) {
    super(info);
    this.option = new ArrayList<IfOption>(option);
    this.defblock = defblock;
  }

  public Block getDefblock() {
    return defblock;
  }

  public void setDefblock(Block defblock) {
    assert (defblock != null);
    this.defblock = defblock;
  }

  public List<IfOption> getOption() {
    return option;
  }

}
