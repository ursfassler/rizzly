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
import common.Property;

import evl.Evl;
import evl.function.header.FuncCtrlOutDataOut;
import evl.function.ret.FuncReturnNone;
import evl.other.EvlList;
import evl.other.Named;
import evl.statement.Block;
import evl.variable.FuncVariable;

//TODO rename
//TODO rename trap to runtime exception and provide arguments
public class KnowLlvmLibrary extends KnowledgeEntry {
  private static final ElementInfo info = ElementInfo.NO;
  private KnowledgeBase kb;

  @Override
  public void init(KnowledgeBase kb) {
    this.kb = kb;
  }

  private Evl findItem(String name) {
    return kb.getRoot().getChildren().find(name);
  }

  private void addItem(Named item) {
    kb.getRoot().getChildren().add(item);
  }

  public FuncCtrlOutDataOut getTrap() {
    final String NAME = Designator.NAME_SEP + "trap";

    FuncCtrlOutDataOut ret = (FuncCtrlOutDataOut) findItem(NAME);

    if (ret == null) {
      ret = new FuncCtrlOutDataOut(info, NAME, new EvlList<FuncVariable>(), new FuncReturnNone(info), new Block(info));
      ret.properties().put(Property.Extern, true);
      ret.properties().put(Property.Public, true);
      addItem(ret);
    }

    return ret;
  }
}
