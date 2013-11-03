package fun.statement;

import java.util.ArrayList;
import java.util.List;

import common.ElementInfo;

/**
 * 
 * @author urs
 */
public class IfStmt extends Statement {

  private List<IfOption> option = new ArrayList<IfOption>();
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

  public List<IfOption> getOption() {
    return option;
  }

  public void addOption(IfOption opt) {
    option.add(opt);
  }

}
