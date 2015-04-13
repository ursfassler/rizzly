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

package ast.traverser.other;

import ast.data.Ast;
import ast.data.function.Function;
import ast.data.function.header.FuncFunction;
import ast.data.function.header.FuncProcedure;
import ast.data.function.header.FuncQuery;
import ast.data.function.header.FuncResponse;
import ast.data.function.header.FuncSignal;
import ast.data.function.header.FuncSlot;
import ast.data.function.header.FuncSubHandlerEvent;
import ast.data.function.header.FuncSubHandlerQuery;
import ast.traverser.NullTraverser;

/**
 * Returns for every function if, it writes to outside. It gets the information only from the function type.
 *
 * @author urs
 *
 */
public class OutsideWriterInfo extends NullTraverser<Boolean, Void> {

  public static Boolean get(Function inst) {
    OutsideWriterInfo reduction = new OutsideWriterInfo();
    return reduction.traverse(inst, null);
  }

  @Override
  protected Boolean visitDefault(Ast obj, Void param) {
    throw new RuntimeException("not yet implemented: " + obj.getClass().getCanonicalName());
  }

  @Override
  protected Boolean visitFuncProcedure(FuncProcedure obj, Void param) {
    return false;
  }

  @Override
  protected Boolean visitFuncFunction(FuncFunction obj, Void param) {
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
  protected Boolean visitFuncSignal(FuncSignal obj, Void param) {
    return true;
  }

  @Override
  protected Boolean visitFuncQuery(FuncQuery obj, Void param) {
    return false;
  }

  @Override
  protected Boolean visitFuncSlot(FuncSlot obj, Void param) {
    return false; // FIXME sure?
  }

  @Override
  protected Boolean visitFuncResponse(FuncResponse obj, Void param) {
    return false;
  }

}
