package evl.statement;

import common.ElementInfo;

import evl.EvlBase;
import evl.other.EvlList;

public class CaseOpt extends EvlBase {
  private EvlList<CaseOptEntry> value;
  private Block code;

  public CaseOpt(ElementInfo info, EvlList<CaseOptEntry> value, Block code) {
    super(info);
    this.value = value;
    this.code = code;
  }

  public Block getCode() {
    return code;
  }

  public void setCode(Block code) {
    this.code = code;
  }

  public EvlList<CaseOptEntry> getValue() {
    return value;
  }

  @Override
  public String toString() {
    return value.toString();
  }

}
