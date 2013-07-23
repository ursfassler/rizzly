package evl.traverser;

import evl.Evl;
import evl.NullTraverser;
import evl.function.FunctionBase;
import evl.function.impl.FuncGlobal;
import evl.function.impl.FuncInputHandlerEvent;
import evl.function.impl.FuncInputHandlerQuery;
import evl.function.impl.FuncPrivateRet;
import evl.function.impl.FuncPrivateVoid;
import evl.function.impl.FuncProtoRet;
import evl.function.impl.FuncProtoVoid;
import evl.function.impl.FuncSubHandlerEvent;
import evl.function.impl.FuncSubHandlerQuery;
import evl.hfsm.HfsmQueryFunction;

/**
 * Returns for every function if, it reads form outside. It gets the information only from the function type.
 *
 * @author urs
 *
 */
public class OutsideReaderInfo extends NullTraverser<Boolean, Void> {

  public static Boolean get(FunctionBase inst) {
    OutsideReaderInfo reduction = new OutsideReaderInfo();
    return reduction.traverse(inst, null);
  }

  @Override
  protected Boolean visitDefault(Evl obj, Void param) {
    throw new RuntimeException("not yet implemented: " + obj.getClass().getCanonicalName());
  }

  @Override
  protected Boolean visitFuncGlobal(FuncGlobal obj, Void param) {
    return false;
  }

  @Override
  protected Boolean visitFuncPrivateVoid(FuncPrivateVoid obj, Void param) {
    return false;
  }

  @Override
  protected Boolean visitFuncPrivateRet(FuncPrivateRet obj, Void param) {
    return false;
  }

  @Override
  protected Boolean visitFuncProtoRet(FuncProtoRet obj, Void param) {
    return true;
  }

  @Override
  protected Boolean visitFuncProtoVoid(FuncProtoVoid obj, Void param) {
    return false;
  }

  @Override
  protected Boolean visitFuncInputHandlerEvent(FuncInputHandlerEvent obj, Void param) {
    return false;
  }

  @Override
  protected Boolean visitFuncInputHandlerQuery(FuncInputHandlerQuery obj, Void param) {
    return false;
  }

  @Override
  protected Boolean visitFuncSubHandlerQuery(FuncSubHandlerQuery obj, Void param) {
    return false;
  }

  @Override
  protected Boolean visitFuncSubHandlerEvent(FuncSubHandlerEvent obj, Void param) {
    return false;
  }

  @Override
  protected Boolean visitHfsmQueryFunction(HfsmQueryFunction obj, Void param) {
    return false;
  }

}
