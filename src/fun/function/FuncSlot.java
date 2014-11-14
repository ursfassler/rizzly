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

package fun.function;

import common.ElementInfo;

import fun.content.CompIfaceContent;
import fun.expression.reference.Reference;
import fun.other.FunList;
import fun.statement.Block;
import fun.variable.FuncVariable;

/**
 *
 * @author urs
 */
public class FuncSlot extends FuncImpl implements CompIfaceContent {

  public FuncSlot(ElementInfo info, String name, FunList<FuncVariable> param, Reference ret, Block body) {
    super(info, name, param, ret, body);
  }

  public FuncSlot(ElementInfo info, String name, FunList<FuncVariable> param, Reference ret) {
    super(info, name, param, ret, new Block(info));
  }

}
