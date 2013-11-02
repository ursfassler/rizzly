package evl.statement;

import java.util.List;

import common.ElementInfo;

import evl.EvlBase;


public class CaseOpt extends EvlBase {
  private List<CaseOptEntry> value;
  private Block code;

  public CaseOpt(ElementInfo info, List<CaseOptEntry> value, Block code) {
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

  public List<CaseOptEntry> getValue() {
    return value;
  }

  @Override
  public String toString() {
    return value.toString();
  }

}
