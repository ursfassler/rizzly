package pir.cfg;

import java.util.ArrayList;
import java.util.List;

import pir.PirObject;
import pir.expression.reference.Referencable;
import pir.statement.bbend.BasicBlockEnd;
import pir.statement.normal.NormalStmt;
import pir.statement.phi.PhiStmt;

// Named since Phi statements references basic blocks
public class BasicBlock extends PirObject implements Referencable {

  private String name;
  final private List<PhiStmt> phi = new ArrayList<PhiStmt>();
  final private List<NormalStmt> code = new ArrayList<NormalStmt>();
  private BasicBlockEnd end = null;

  public BasicBlock(String name) {
    this.name = name;
  }

  @Override
  public String getName() {
    return name;
  }

  @Override
  public void setName(String name) {
    this.name = name;
  }

  public List<NormalStmt> getCode() {
    return code;
  }

  @Override
  public String toString() {
    return name;
  }

  public BasicBlockEnd getEnd() {
    return end;
  }

  public void setEnd(BasicBlockEnd end) {
    this.end = end;
  }

  public List<PhiStmt> getPhi() {
    return phi;
  }
}
