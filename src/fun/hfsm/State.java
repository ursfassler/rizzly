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

package fun.hfsm;

import common.ElementInfo;

import fun.FunBase;
import fun.other.FunList;
import fun.other.Named;
import fun.statement.Block;

abstract public class State extends FunBase implements Named, StateContent {
  private String name;
  private Block entryFunc = null;
  private Block exitFunc = null;
  final protected FunList<StateContent> item = new FunList<StateContent>();

  public State(ElementInfo info, String name) {
    super(info);
    this.name = name;
  }

  @Override
  public String getName() {
    return name;
  }

  @Override
  public void setName(String name) {
    this.name = name;
  }

  public Block getEntryFunc() {
    return entryFunc;
  }

  public void setEntryFunc(Block entryFunc) {
    this.entryFunc = entryFunc;
  }

  public Block getExitFunc() {
    return exitFunc;
  }

  public void setExitFunc(Block exitFunc) {
    this.exitFunc = exitFunc;
  }

  public FunList<StateContent> getItemList() {
    return item;
  }

}
