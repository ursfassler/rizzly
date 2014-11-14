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

import cir.CirBase;
import cir.expression.reference.Referencable;
import cir.other.Named;
import cir.type.TypeRef;
import cir.variable.FuncVariable;

abstract public class Function extends CirBase implements Named, Referencable {
  private String name;
  private TypeRef retType;
  final private List<FuncVariable> argument;

  public Function(String name, TypeRef retType, List<FuncVariable> argument) {
    super();
    this.name = name;
    this.retType = retType;
    this.argument = argument;
  }

  @Override
  public String getName() {
    return name;
  }

  @Override
  public void setName(String name) {
    this.name = name;
  }

  public TypeRef getRetType() {
    return retType;
  }

  public void setRetType(TypeRef retType) {
    this.retType = retType;
  }

  public List<FuncVariable> getArgument() {
    return argument;
  }

}
