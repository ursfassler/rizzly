package pir.statement;

import java.util.List;

import pir.PirObject;
import util.Range;


public class CaseEntry extends PirObject {
  final private List<Range> values;
  private Block code;

  public CaseEntry(List<Range> values, Block code) {
    super();
    this.values = values;
    this.code = code;
  }

  public List<Range> getValues() {
    return values;
  }

  public Block getCode() {
    return code;
  }

  public void setCode(Block code) {
    this.code = code;
  }

}
