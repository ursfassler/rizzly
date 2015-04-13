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

package fun.other;

import common.ElementInfo;

import evl.data.Evl;
import evl.data.EvlList;
import evl.data.statement.Block;

public class RawElementary extends RawComponent {
  final private EvlList<Template> declaration = new EvlList<Template>();
  final private EvlList<Evl> instantiation = new EvlList<Evl>();
  private Block entryFunc = new Block(ElementInfo.NO);
  private Block exitFunc = new Block(ElementInfo.NO);

  public RawElementary(ElementInfo info, String name) {
    super(info, name);
  }

  public EvlList<Template> getDeclaration() {
    return declaration;
  }

  public EvlList<Evl> getInstantiation() {
    return instantiation;
  }

  public Block getEntryFunc() {
    return entryFunc;
  }

  public Block getExitFunc() {
    return exitFunc;
  }

  public void setEntryFunc(Block entryFunc) {
    this.entryFunc = entryFunc;
  }

  public void setExitFunc(Block exitFunc) {
    this.exitFunc = exitFunc;
  }

}
