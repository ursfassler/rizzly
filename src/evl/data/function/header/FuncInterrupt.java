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

package evl.data.function.header;

import common.ElementInfo;

import evl.data.EvlList;
import evl.data.function.Function;
import evl.data.statement.Block;
import evl.data.variable.FuncVariable;

/**
 * Function inside a component. It may be not pure and can therefore not be executed at compile time.
 *
 * @author urs
 */
final public class FuncInterrupt extends Function {

  public FuncInterrupt(ElementInfo info, String name, EvlList<FuncVariable> param, evl.data.function.ret.FuncReturn ret, Block body) {
    super(info, name, param, ret, body);
  }

}
