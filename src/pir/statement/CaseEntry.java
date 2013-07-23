package pir.statement;

import java.util.List;

import pir.PirObject;


public class CaseEntry extends PirObject {
  final private List<CaseOptEntry> values;
  private Block code;

  public CaseEntry(List<CaseOptEntry> values, Block code) {
    super();
    this.values = values;
    this.code = code;
  }

  public List<CaseOptEntry> getValues() {
    return values;
  }

  public Block getCode() {
    return code;
  }

  public void setCode(Block code) {
    this.code = code;
  }

}
