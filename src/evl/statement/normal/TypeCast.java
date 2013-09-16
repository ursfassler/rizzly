package evl.statement.normal;

import common.ElementInfo;

import evl.expression.Expression;
import evl.type.TypeRef;
import evl.variable.SsaVariable;

public class TypeCast extends NormalStmt implements SsaGenerator {

  private SsaVariable variable;
  private Expression value;
  private TypeRef cast;

  public TypeCast(ElementInfo info, SsaVariable variable, TypeRef cast, Expression value) {
    super(info);
    this.variable = variable;
    this.value = value;
    this.cast = cast;
  }

  public Expression getValue() {
    return value;
  }

  public void setValue(Expression value) {
    this.value = value;
  }

  @Override
  public SsaVariable getVariable() {
    return variable;
  }

  public void setVariable(SsaVariable variable) {
    this.variable = variable;
  }

  public TypeRef getCast() {
    return cast;
  }

  public void setCast(TypeRef cast) {
    this.cast = cast;
  }

  @Override
  public String toString() {
    return variable + " := " + cast + "(" + value + ")";
  }
}
