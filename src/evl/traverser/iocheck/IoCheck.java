package evl.traverser.iocheck;

import java.util.Collection;
import java.util.Map;

import error.ErrorType;
import error.RError;
import evl.Evl;
import evl.NullTraverser;
import evl.function.impl.FuncGlobal;
import evl.function.impl.FuncIfaceInRet;
import evl.function.impl.FuncIfaceInVoid;
import evl.function.impl.FuncIfaceOutRet;
import evl.function.impl.FuncIfaceOutVoid;
import evl.function.impl.FuncInputHandlerEvent;
import evl.function.impl.FuncInputHandlerQuery;
import evl.function.impl.FuncPrivateRet;
import evl.function.impl.FuncPrivateVoid;
import evl.function.impl.FuncProtoRet;
import evl.function.impl.FuncProtoVoid;
import evl.function.impl.FuncSubHandlerEvent;
import evl.function.impl.FuncSubHandlerQuery;
import evl.hfsm.HfsmQueryFunction;
import evl.hfsm.Transition;

public class IoCheck extends NullTraverser<Void, Void> {
  private Map<? extends Evl, Boolean> writes;
  private Map<? extends Evl, Boolean> reads;
  private Map<? extends Evl, Boolean> outputs;
  private Map<? extends Evl, Boolean> inputs;

  public IoCheck(Map<? extends Evl, Boolean> writes, Map<? extends Evl, Boolean> reads, Map<? extends Evl, Boolean> outputs, Map<? extends Evl, Boolean> inputs) {
    super();
    this.writes = writes;
    this.reads = reads;
    this.outputs = outputs;
    this.inputs = inputs;
  }

  public void check(Collection<? extends Evl> funcs) {
    for (Evl func : funcs) {
      traverse(func, null);
    }
  }

  @Override
  protected Void visitDefault(Evl obj, Void param) {
    throw new RuntimeException("not yet implemented: " + obj.getClass().getCanonicalName());
  }

  private void checkQuery(Evl obj, String objName) {
    assert (reads.containsKey(obj));
    assert (inputs.containsKey(obj));
    if (writes.get(obj) == true) {
      RError.err(ErrorType.Error, obj.getInfo(), objName + " writes state");
    }
    if (outputs.get(obj) == true) {
      RError.err(ErrorType.Error, obj.getInfo(), objName + " sends event");
    }
  }

  @Override
  protected Void visitFuncGlobal(FuncGlobal obj, Void param) {
    assert (writes.get(obj) == false);
    assert (reads.get(obj) == false);
    assert (outputs.get(obj) == false);
    assert (inputs.get(obj) == false);
    return null;
  }

  @Override
  protected Void visitFuncProtoRet(FuncProtoRet obj, Void param) {
    assert (writes.get(obj) == false);
    assert (reads.get(obj) == false);
    assert (outputs.get(obj) == false);
    assert (inputs.get(obj) == true);
    return null;
  }

  @Override
  protected Void visitFuncProtoVoid(FuncProtoVoid obj, Void param) {
    assert (writes.get(obj) == false);
    assert (reads.get(obj) == false);
    assert (outputs.get(obj) == true);
    assert (inputs.get(obj) == false);
    return null;
  }

  @Override
  protected Void visitFuncPrivateVoid(FuncPrivateVoid obj, Void param) {
    // is allowed to do everything
    assert (writes.containsKey(obj));
    assert (reads.containsKey(obj));
    assert (outputs.containsKey(obj));
    assert (inputs.containsKey(obj));
    return null;
  }

  @Override
  protected Void visitFuncPrivateRet(FuncPrivateRet obj, Void param) {
    // is allowed to do everything
    assert (writes.containsKey(obj));
    assert (reads.containsKey(obj));
    assert (outputs.containsKey(obj));
    assert (inputs.containsKey(obj));
    return null;
  }

  @Override
  protected Void visitFuncInputHandlerEvent(FuncInputHandlerEvent obj, Void param) {
    // is allowed to do everything
    assert (writes.containsKey(obj));
    assert (reads.containsKey(obj));
    assert (outputs.containsKey(obj));
    assert (inputs.containsKey(obj));
    return null;
  }

  @Override
  protected Void visitFuncInputHandlerQuery(FuncInputHandlerQuery obj, Void param) {
    checkQuery(obj, "Query input");
    return null;
  }

  @Override
  protected Void visitFuncSubHandlerEvent(FuncSubHandlerEvent obj, Void param) {
    // is allowed to do everything
    assert (writes.containsKey(obj));
    assert (reads.containsKey(obj));
    assert (outputs.containsKey(obj));
    assert (inputs.containsKey(obj));
    return null;
  }

  @Override
  protected Void visitFuncSubHandlerQuery(FuncSubHandlerQuery obj, Void param) {
    checkQuery(obj, "Query input");
    return null;
  }

  @Override
  protected Void visitHfsmQueryFunction(HfsmQueryFunction obj, Void param) {
    checkQuery(obj, "Query");
    return null;
  }

  @Override
  protected Void visitTransition(Transition obj, Void param) {
    assert (writes.containsKey(obj.getBody()));
    assert (reads.containsKey(obj.getBody()));
    assert (outputs.containsKey(obj.getBody()));
    assert (inputs.containsKey(obj.getBody()));

    checkQuery(obj.getGuard(), "Transition guard");
    return null;
  }

  @Override
  protected Void visitFuncIfaceOutVoid(FuncIfaceOutVoid obj, Void param) {
    assert (writes.get(obj) == false);
    assert (reads.get(obj) == false);
    assert (outputs.get(obj) == true);
    assert (inputs.get(obj) == false);
    return null;
  }

  @Override
  protected Void visitFuncIfaceOutRet(FuncIfaceOutRet obj, Void param) {
    assert (writes.get(obj) == false);
    assert (reads.get(obj) == false);
    assert (outputs.get(obj) == false);
    assert (inputs.get(obj) == true);
    return null;
  }

  @Override
  protected Void visitFuncIfaceInVoid(FuncIfaceInVoid obj, Void param) {
    assert (writes.get(obj) == false);
    assert (reads.get(obj) == false);
    assert (outputs.get(obj) == false);
    assert (inputs.get(obj) == false);
    return null;
  }

  @Override
  protected Void visitFuncIfaceInRet(FuncIfaceInRet obj, Void param) {
    assert (writes.get(obj) == false);
    assert (reads.get(obj) == false);
    assert (outputs.get(obj) == false);
    assert (inputs.get(obj) == false);
    return null;
  }

}
