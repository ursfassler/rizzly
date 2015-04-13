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

package ast.data.component.hfsm;

import ast.ElementInfo;
import ast.data.AstList;
import ast.data.Named;
import ast.data.expression.reference.SimpleRef;
import ast.data.function.header.FuncProcedure;

abstract public class State extends Named implements StateContent {
  public SimpleRef<FuncProcedure> entryFunc;
  public SimpleRef<FuncProcedure> exitFunc;
  final public AstList<StateContent> item = new AstList<StateContent>();

  public State(ElementInfo info, String name, SimpleRef<FuncProcedure> entryFunc, SimpleRef<FuncProcedure> exitFunc) {
    super(info, name);
    this.entryFunc = entryFunc;
    this.exitFunc = exitFunc;
  }

}
