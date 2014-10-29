package fun.statement;

import common.ElementInfo;

import fun.FunBase;
import fun.other.FunList;

public class CaseOpt extends FunBase {
  private FunList<CaseOptEntry> value;
  private Block code;

  public CaseOpt(ElementInfo info, FunList<CaseOptEntry> value, Block code) {
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

  public FunList<CaseOptEntry> getValue() {
    return value;
  }

  @Override
  public String toString() {
    return value.toString();
  }

}
