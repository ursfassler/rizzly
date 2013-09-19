package evl.hfsm;

import java.util.Collection;

import common.ElementInfo;

import evl.cfg.BasicBlockList;
import evl.expression.Expression;
import evl.other.IfaceUse;
import evl.other.ListOfNamed;
import evl.variable.Variable;

public class Transition extends StateItem {
  private String name;
  private State src;
  private State dst;
  private IfaceUse eventIface;
  private String eventFunc;
  private Expression guard;
  final private ListOfNamed<Variable> param;
  private BasicBlockList body;

  public Transition(ElementInfo info, String name, State src, State dst, IfaceUse eventIface, String eventFunc, Expression guard, Collection<Variable> param, BasicBlockList body) {
    super(info);
    this.name = name;
    this.src = src;
    this.dst = dst;
    this.eventIface = eventIface;
    this.eventFunc = eventFunc;
    this.guard = guard;
    this.param = new ListOfNamed<Variable>(param);
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

  public IfaceUse getEventIface() {
    return eventIface;
  }

  public void setEventIface(IfaceUse eventIface) {
    this.eventIface = eventIface;
  }

  public String getEventFunc() {
    return eventFunc;
  }

  public void setEventFunc(String eventFunc) {
    this.eventFunc = eventFunc;
  }

  public ListOfNamed<Variable> getParam() {
    return param;
  }

  public BasicBlockList getBody() {
    return body;
  }

  public void setBody(BasicBlockList body) {
    this.body = body;
  }

  @Override
  public String toString() {
    return src + " -> " + dst + " by " + eventIface.getName() + "." + eventFunc + " if " + guard;
  }

}
