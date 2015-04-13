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

package ast.pass.check.model.io;

import java.util.Collection;
import java.util.Map;

import ast.data.Ast;
import ast.data.component.hfsm.Transition;
import ast.data.function.header.FuncFunction;
import ast.data.function.header.FuncProcedure;
import ast.data.function.header.FuncQuery;
import ast.data.function.header.FuncResponse;
import ast.data.function.header.FuncSignal;
import ast.data.function.header.FuncSlot;
import ast.data.function.header.FuncSubHandlerEvent;
import ast.data.function.header.FuncSubHandlerQuery;
import ast.traverser.NullTraverser;
import error.ErrorType;
import error.RError;

public class IoCheck extends NullTraverser<Void, Void> {
  private Map<? extends Ast, Boolean> writes;
  private Map<? extends Ast, Boolean> reads;
  private Map<? extends Ast, Boolean> outputs;
  private Map<? extends Ast, Boolean> inputs;

  public IoCheck(Map<? extends Ast, Boolean> writes, Map<? extends Ast, Boolean> reads, Map<? extends Ast, Boolean> outputs, Map<? extends Ast, Boolean> inputs) {
    super();
    this.writes = writes;
    this.reads = reads;
    this.outputs = outputs;
    this.inputs = inputs;
  }

  public void check(Collection<? extends Ast> funcs) {
    for (Ast func : funcs) {
      traverse(func, null);
    }
  }

  @Override
  protected Void visitDefault(Ast obj, Void param) {
    throw new RuntimeException("not yet implemented: " + obj.getClass().getCanonicalName());
  }

  private void checkQuery(Ast obj, String objName) {
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
  protected Void visitFuncProcedure(FuncProcedure obj, Void param) {
    // is allowed to do everything
    assert (writes.containsKey(obj));
    assert (reads.containsKey(obj));
    assert (outputs.containsKey(obj));
    assert (inputs.containsKey(obj));
    return null;
  }

  @Override
  protected Void visitFuncFunction(FuncFunction obj, Void param) {
    // TODO not allowed to write or output
    // assert (writes.get(obj) == false);
    // assert (reads.get(obj) == false);
    // assert (outputs.get(obj) == false);
    // assert (inputs.get(obj) == false);
    // is allowed to do everything
    assert (writes.containsKey(obj));
    assert (reads.containsKey(obj));
    assert (outputs.containsKey(obj));
    assert (inputs.containsKey(obj));
    return null;
  }

  @Override
  protected Void visitFuncSlot(FuncSlot obj, Void param) {
    // is allowed to do everything
    assert (writes.containsKey(obj));
    assert (reads.containsKey(obj));
    assert (outputs.containsKey(obj));
    assert (inputs.containsKey(obj));
    return null;
  }

  @Override
  protected Void visitFuncResponse(FuncResponse obj, Void param) {
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
    assert (writes.containsKey(obj.body));
    assert (reads.containsKey(obj.body));
    assert (outputs.containsKey(obj.body));
    assert (inputs.containsKey(obj.body));

    checkQuery(obj.guard, "Transition guard");
    return null;
  }

  @Override
  protected Void visitFuncSignal(FuncSignal obj, Void param) {
    assert (writes.get(obj) == false);
    assert (reads.get(obj) == false);
    assert (outputs.get(obj) == true);
    assert (inputs.get(obj) == false);
    return null;
  }

  @Override
  protected Void visitFuncQuery(FuncQuery obj, Void param) {
    assert (writes.get(obj) == false);
    assert (reads.get(obj) == false);
    assert (outputs.get(obj) == false);
    assert (inputs.get(obj) == true);
    return null;
  }

}
