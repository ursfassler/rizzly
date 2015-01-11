/**
 *  This file is part of Rizzly.
 *
 *  Rizzly is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Rizzly is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with Rizzly.  If not, see <http://www.gnu.org/licenses/>.
 */

package evl.pass.check.io;

import java.util.Collection;
import java.util.Map;

import error.ErrorType;
import error.RError;
import evl.Evl;
import evl.NullTraverser;
import evl.function.header.FuncCtrlInDataIn;
import evl.function.header.FuncCtrlInDataOut;
import evl.function.header.FuncCtrlOutDataIn;
import evl.function.header.FuncCtrlOutDataOut;
import evl.function.header.FuncGlobal;
import evl.function.header.FuncPrivateRet;
import evl.function.header.FuncPrivateVoid;
import evl.function.header.FuncSubHandlerEvent;
import evl.function.header.FuncSubHandlerQuery;
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
  protected Void visitFuncIfaceInVoid(FuncCtrlInDataIn obj, Void param) {
    // is allowed to do everything
    assert (writes.containsKey(obj));
    assert (reads.containsKey(obj));
    assert (outputs.containsKey(obj));
    assert (inputs.containsKey(obj));
    return null;
  }

  @Override
  protected Void visitFuncIfaceInRet(FuncCtrlInDataOut obj, Void param) {
    checkQuery(obj, "Response");
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
    checkQuery(obj, "Response");
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
  protected Void visitFuncIfaceOutVoid(FuncCtrlOutDataOut obj, Void param) {
    assert (writes.get(obj) == false);
    assert (reads.get(obj) == false);
    assert (outputs.get(obj) == true);
    assert (inputs.get(obj) == false);
    return null;
  }

  @Override
  protected Void visitFuncIfaceOutRet(FuncCtrlOutDataIn obj, Void param) {
    assert (writes.get(obj) == false);
    assert (reads.get(obj) == false);
    assert (outputs.get(obj) == false);
    assert (inputs.get(obj) == true);
    return null;
  }

}
