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

package evl.data.function;

import common.ElementInfo;

import evl.data.Evl;
import evl.data.EvlBase;
import evl.data.EvlList;
import evl.data.Named;
import evl.data.function.ret.FuncReturn;
import evl.data.statement.Block;
import evl.data.variable.FuncVariable;

abstract public class Function extends EvlBase implements Named {
  private String name;
  final public EvlList<FuncVariable> param = new EvlList<FuncVariable>();
  public FuncReturn ret;
  public Block body;

  public Function(ElementInfo info, String name, EvlList<FuncVariable> param, FuncReturn ret, Block body) {
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
