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

package evl.statement;

import common.ElementInfo;

import evl.other.EvlList;

/**
 *
 * @author urs
 */
public class IfStmt extends Statement {

  private EvlList<IfOption> option = new EvlList<IfOption>();
  private Block defblock;

  public IfStmt(ElementInfo info, EvlList<IfOption> option, Block defblock) {
    super(info);
    this.option = new EvlList<IfOption>(option);
    this.defblock = defblock;
  }

  public Block getDefblock() {
    return defblock;
  }

  public void setDefblock(Block defblock) {
    assert (defblock != null);
    this.defblock = defblock;
  }

  public EvlList<IfOption> getOption() {
    return option;
  }

}
