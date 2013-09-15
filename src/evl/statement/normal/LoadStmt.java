package evl.statement.normal;

import common.ElementInfo;

import evl.expression.reference.Reference;
import evl.variable.SsaVariable;

/**
 *
 * @author urs
 */
public class LoadStmt extends NormalStmt implements SsaGenerator {

  private SsaVariable variable;
  private Reference address;

  public LoadStmt(ElementInfo info, SsaVariable variable, Reference address) {
    super(info);
    this.variable = variable;
    this.address = address;
  }

  public Reference getAddress() {
    return address;
  }

  public void setAddress(Reference address) {
    this.address = address;
  }

  @Override
  public SsaVariable getVariable() {
    return variable;
  }

  public void setVariable(SsaVariable variable) {
    this.variable = variable;
  }

  @Override
  public String toString() {
    return variable + " := load(" + address + ")";
  }
}
