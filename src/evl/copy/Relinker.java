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

package evl.copy;

import java.util.Map;

import evl.data.Evl;
import evl.data.EvlList;
import evl.data.Named;
import evl.data.expression.reference.BaseRef;
import evl.traverser.DefTraverser;

public class Relinker extends DefTraverser<Void, Map<? extends Named, ? extends Named>> {
  static private final Relinker INSTANCE = new Relinker();

  static public void relink(Evl obj, Map<? extends Named, ? extends Named> map) {
    INSTANCE.traverse(obj, map);
  }

  static public void relink(EvlList<? extends Evl> obj, Map<? extends Named, ? extends Named> map) {
    for (Evl itr : obj) {
      relink(itr, map);
    }
  }

  @Override
  protected Void visitBaseRef(BaseRef obj, Map<? extends Named, ? extends Named> param) {
    if (param.containsKey(obj.link)) {
      obj.link = param.get(obj.link);
    }
    return super.visitBaseRef(obj, param);
  }
}
