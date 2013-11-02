package cir.statement;

import java.util.ArrayList;
import java.util.List;

import util.Range;
import cir.CirBase;


public class CaseEntry extends CirBase {
  final private List<Range> values;
  private Statement code;

  public CaseEntry(Block code) {
    super();
    this.values = new ArrayList<Range>();
    this.code = code;
  }

  public List<Range> getValues() {
    return values;
  }

  public Statement getCode() {
    return code;
  }

  public void setCode(Statement code) {
    this.code = code;
  }

  @Override
  public String toString() {
    return values.toString();
  }

}
