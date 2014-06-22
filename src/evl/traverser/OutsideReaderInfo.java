package evl.traverser;

import evl.Evl;
import evl.NullTraverser;
import evl.function.FunctionBase;
import evl.function.impl.FuncGlobal;
import evl.function.impl.FuncIfaceInRet;
import evl.function.impl.FuncIfaceInVoid;
import evl.function.impl.FuncIfaceOutRet;
import evl.function.impl.FuncIfaceOutVoid;
import evl.function.impl.FuncImplResponse;
import evl.function.impl.FuncImplSlot;
import evl.function.impl.FuncPrivateRet;
import evl.function.impl.FuncPrivateVoid;
import evl.function.impl.FuncProtoRet;
import evl.function.impl.FuncProtoVoid;
import evl.function.impl.FuncSubHandlerEvent;
import evl.function.impl.FuncSubHandlerQuery;

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
  protected Boolean visitFuncImplSlot(FuncImplSlot obj, Void param) {
    return false;
  }

  @Override
  protected Boolean visitFuncImplResponse(FuncImplResponse obj, Void param) {
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
  protected Boolean visitFuncIfaceOutVoid(FuncIfaceOutVoid obj, Void param) {
    return false;
  }

  @Override
  protected Boolean visitFuncIfaceOutRet(FuncIfaceOutRet obj, Void param) {
    return true;
  }

  @Override
  protected Boolean visitFuncIfaceInVoid(FuncIfaceInVoid obj, Void param) {
    return false;
  }

  @Override
  protected Boolean visitFuncIfaceInRet(FuncIfaceInRet obj, Void param) {
    return false; // FIXME sure?
  }

}
