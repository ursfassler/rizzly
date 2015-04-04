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

package evl.data.component.composition;

import common.ElementInfo;

import evl.data.EvlBase;
import evl.data.EvlList;
import evl.data.expression.reference.SimpleRef;
import evl.data.function.Function;

public class SubCallbacks extends EvlBase {
  final public EvlList<Function> func = new EvlList<Function>();
  final public SimpleRef<CompUse> compUse;

  public SubCallbacks(ElementInfo info, SimpleRef<CompUse> compUse) {
    super(info);
    this.compUse = compUse;
  }

}
