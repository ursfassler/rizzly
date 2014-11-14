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

package evl.function;

import common.ElementInfo;

import evl.Evl;
import evl.EvlBase;
import evl.expression.reference.SimpleRef;
import evl.other.EvlList;
import evl.other.Named;
import evl.statement.Block;
import evl.type.Type;
import evl.variable.FuncVariable;

abstract public class Function extends EvlBase implements Named {
  private String name;
  final private EvlList<FuncVariable> param = new EvlList<FuncVariable>();
  private SimpleRef<Type> ret = null;
  private Block body;

  public Function(ElementInfo info, String name, EvlList<FuncVariable> param, SimpleRef<Type> ret, Block body) {
    super(info);
    this.name = name;
    this.param.addAll(param);
    this.ret = ret;
    this.body = body;
  }

  @Override
  public String getName() {
    return name;
  }

  @Override
  public void setName(String name) {
    this.name = name;
  }

  public Block getBody() {
    return body;
  }

  public void setBody(Block body) {
    this.body = body;
  }

  public EvlList<FuncVariable> getParam() {
    return param;
  }

  public SimpleRef<Type> getRet() {
    return ret;
  }

  public void setRet(SimpleRef<Type> ret) {
    this.ret = ret;
  }

  @Override
  public String toString() {
    String ret = name;

    ret += "(";
    ret += l2s(param);
    ret += ")";

    return ret;
  }

  private String l2s(EvlList<FuncVariable> param2) {
    String ret = "";
    boolean first = true;
    for (Evl tp : param2) {
      if (first) {
        first = false;
      } else {
        ret += "; ";
      }
      ret += tp.toString();
    }
    return ret;
  }

}
