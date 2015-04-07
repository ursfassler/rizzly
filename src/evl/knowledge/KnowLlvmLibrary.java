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

package evl.knowledge;

import common.Designator;
import common.ElementInfo;

import evl.data.Evl;
import evl.data.EvlList;
import evl.data.Named;
import evl.data.function.FunctionProperty;
import evl.data.function.header.FuncCtrlOutDataOut;
import evl.data.function.ret.FuncReturnNone;
import evl.data.statement.Block;
import evl.data.variable.FuncVariable;

//TODO rename
//TODO rename trap to runtime exception and provide arguments
public class KnowLlvmLibrary extends KnowledgeEntry {
  private KnowledgeBase kb;

  @Override
  public void init(KnowledgeBase kb) {
    this.kb = kb;
  }

  private Evl findItem(String name) {
    return kb.getRoot().children.find(name);
  }

  private void addItem(Named item) {
    kb.getRoot().children.add(item);
  }

  public FuncCtrlOutDataOut getTrap() {
    final String NAME = Designator.NAME_SEP + "trap";
    final ElementInfo info = new ElementInfo(NAME, 0, 0);

    FuncCtrlOutDataOut ret = (FuncCtrlOutDataOut) findItem(NAME);

    if (ret == null) {
      ret = new FuncCtrlOutDataOut(info, NAME, new EvlList<FuncVariable>(), new FuncReturnNone(info), new Block(info));
      ret.property = FunctionProperty.External;
      addItem(ret);
    }

    return ret;
  }
}
