package cir.statement;

import java.util.ArrayList;
import java.util.List;

import cir.CirBase;


public class CaseEntry extends CirBase {
  final private List<Integer> values;
  private Statement code;

  public CaseEntry(Block code) {
    super();
    this.values = new ArrayList<Integer>();
    this.code = code;
  }

  public List<Integer> getValues() {
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
