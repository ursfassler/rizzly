package evl.hfsm;

import common.ElementInfo;

import evl.EvlBase;
import evl.expression.Expression;
import evl.expression.reference.SimpleRef;
import evl.function.header.FuncCtrlInDataIn;
import evl.other.EvlList;
import evl.statement.Block;
import evl.variable.FuncVariable;

public class Transition extends EvlBase implements StateItem {
  private SimpleRef<State> src;
  private SimpleRef<State> dst;
  private SimpleRef<FuncCtrlInDataIn> eventFunc;
  final private EvlList<FuncVariable> param = new EvlList<FuncVariable>();
  private Expression guard;
  private Block body;

  public Transition(ElementInfo info, SimpleRef<State> src, SimpleRef<State> dst, SimpleRef<FuncCtrlInDataIn> eventFunc, Expression guard, EvlList<FuncVariable> param, Block body) {
    super(info);
    this.src = src;
    this.dst = dst;
    this.eventFunc = eventFunc;
    this.param.addAll(param);
    this.guard = guard;
    this.body = body;
  }

  public SimpleRef<State> getSrc() {
    return src;
  }

  public void setSrc(SimpleRef<State> src) {
    this.src = src;
  }

  public SimpleRef<State> getDst() {
    return dst;
  }

  public void setDst(SimpleRef<State> dst) {
    this.dst = dst;
  }

  public Expression getGuard() {
    return guard;
  }

  public void setGuard(Expression guard) {
    this.guard = guard;
  }

  public SimpleRef<FuncCtrlInDataIn> getEventFunc() {
    return eventFunc;
  }

  public void setEventFunc(SimpleRef<FuncCtrlInDataIn> eventFunc) {
    this.eventFunc = eventFunc;
  }

  public EvlList<FuncVariable> getParam() {
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
    return src + " -> " + dst + " by " + eventFunc + param + " if " + guard;
  }

}
