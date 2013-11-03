package evl.hfsm;

import java.util.Collection;

import common.ElementInfo;

import evl.EvlBase;
import evl.expression.Expression;
import evl.expression.reference.Reference;
import evl.other.ListOfNamed;
import evl.statement.Block;
import evl.variable.FuncVariable;

public class Transition extends EvlBase implements StateItem {
  private String name;
  private State src; // TODO use reference
  private State dst; // TODO use reference
  private Reference eventFunc;
  final private ListOfNamed<FuncVariable> param;
  private Expression guard;
  private Block body;

  public Transition(ElementInfo info, String name, State src, State dst, Reference eventFunc, Expression guard, Collection<FuncVariable> param, Block body) {
    super(info);
    this.name = name;
    this.src = src;
    this.dst = dst;
    this.eventFunc = eventFunc;
    this.param = new ListOfNamed<FuncVariable>(param);
    this.guard = guard;
    this.body = body;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public State getSrc() {
    return src;
  }

  public void setSrc(State src) {
    this.src = src;
  }

  public State getDst() {
    return dst;
  }

  public void setDst(State dst) {
    this.dst = dst;
  }

  public Expression getGuard() {
    return guard;
  }

  public void setGuard(Expression guard) {
    this.guard = guard;
  }

  public Reference getEventFunc() {
    return eventFunc;
  }

  public void setEventFunc(Reference eventFunc) {
    this.eventFunc = eventFunc;
  }

  public ListOfNamed<FuncVariable> getParam() {
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
