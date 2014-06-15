package evl.statement.intern;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import common.ElementInfo;

import evl.expression.Expression;
import evl.expression.reference.Reference;
import evl.statement.Statement;

public class MsgPush extends Statement {
  private Reference queue;
  private Reference func;
  private final List<Expression> data = new ArrayList<Expression>();

  public MsgPush(ElementInfo info, Reference queue, Reference func, Collection<Expression> data) {
    super(info);
    this.queue = queue;
    this.func = func;
    this.data.addAll(data);
  }

  public Reference getQueue() {
    return queue;
  }

  public void setQueue(Reference queue) {
    this.queue = queue;
  }

  public Reference getFunc() {
    return func;
  }

  public void setFunc(Reference func) {
    this.func = func;
  }

  public List<Expression> getData() {
    return data;
  }

  @Override
  public String toString() {
    return queue + ".push(" + func + "," + data + ")";
  }

}
