package fun.hfsm;

import common.ElementInfo;

import fun.FunBase;
import fun.expression.Expression;
import fun.expression.reference.Reference;
import fun.other.FunList;
import fun.statement.Block;
import fun.variable.FuncVariable;

public class Transition extends FunBase implements StateContent {
  private Reference src;
  private Reference dst;
  private Reference event;
  final private FunList<FuncVariable> param = new FunList<FuncVariable>();
  private Expression guard;
  private Block body;

  public Transition(ElementInfo info) {
    super(info);
  }

  public Reference getSrc() {
    return src;
  }

  public void setSrc(Reference src) {
    this.src = src;
  }

  public Reference getDst() {
    return dst;
  }

  public void setDst(Reference dst) {
    this.dst = dst;
  }

  public Expression getGuard() {
    return guard;
  }

  public void setGuard(Expression guard) {
    this.guard = guard;
  }

  public Reference getEvent() {
    return event;
  }

  public void setEvent(Reference event) {
    this.event = event;
  }

  public FunList<FuncVariable> getParam() {
    return param;
  }

  public Block getBody() {
    return body;
  }

  public void setBody(Block body) {
    this.body = body;
  }

  @Override
  public String toString() {
    return src + " -> " + dst + " by " + event + " if " + guard;
  }

}
