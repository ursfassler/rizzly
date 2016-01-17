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

package ast.repository.query;

import ast.data.Ast;
import ast.data.function.Function;
import ast.data.function.header.FuncFunction;
import ast.data.function.header.FuncProcedure;
import ast.data.function.header.FuncQuery;
import ast.data.function.header.FuncResponse;
import ast.data.function.header.Signal;
import ast.data.function.header.Slot;
import ast.data.function.header.FuncSubHandlerEvent;
import ast.data.function.header.FuncSubHandlerQuery;
import ast.dispatcher.NullDispatcher;

public class FunctionTypeName {
  static final private FunctionTypeNameDispatcher dispatcher = new FunctionTypeNameDispatcher();

  static public String get(Function function) {
    return dispatcher.traverse(function, null);
  }
}

class FunctionTypeNameDispatcher extends NullDispatcher<String, Void> {

  @Override
  protected String visitDefault(Ast obj, Void param) {
    throw new RuntimeException("not yet implemented: " + obj.getClass().getName());
  }

  @Override
  protected String visitFuncSignal(Signal obj, Void param) {
    return "signal";
  }

  @Override
  protected String visitFuncQuery(FuncQuery obj, Void param) {
    return "query";
  }

  @Override
  protected String visitFuncSlot(Slot obj, Void param) {
    return "slot";
  }

  @Override
  protected String visitFuncResponse(FuncResponse obj, Void param) {
    return "response";
  }

  @Override
  protected String visitFuncProcedure(FuncProcedure obj, Void param) {
    return "procedure";
  }

  @Override
  protected String visitFuncFunction(FuncFunction obj, Void param) {
    return "function";
  }

  @Override
  protected String visitFuncSubHandlerQuery(FuncSubHandlerQuery obj, Void param) {
    return "sub-response";
  }

  @Override
  protected String visitFuncSubHandlerEvent(FuncSubHandlerEvent obj, Void param) {
    return "sub-slot";
  }

}
