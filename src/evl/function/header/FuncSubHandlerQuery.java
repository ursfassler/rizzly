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

package evl.function.header;

import common.ElementInfo;

import evl.function.Function;
import evl.function.ret.FuncReturn;
import evl.other.EvlList;
import evl.statement.Block;
import evl.variable.FuncVariable;

/**
 *
 * @author urs
 */
public class FuncSubHandlerQuery extends Function {

  public FuncSubHandlerQuery(ElementInfo info, String name, EvlList<FuncVariable> param, FuncReturn ret, Block body) {
    super(info, name, param, ret, body);
  }
}
