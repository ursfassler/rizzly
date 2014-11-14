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

package cir.function;

import java.util.List;

import cir.statement.Block;
import cir.type.TypeRef;
import cir.variable.FuncVariable;

abstract public class FunctionImpl extends Function {
  final private Block body;

  public FunctionImpl(String name, TypeRef retType, List<FuncVariable> argument, Block body) {
    super(name, retType, argument);
    this.body = body;
  }

  public Block getBody() {
    return body;
  }

}
