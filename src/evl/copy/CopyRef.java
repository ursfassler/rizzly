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

import evl.data.Evl;
import evl.data.expression.reference.RefCall;
import evl.data.expression.reference.RefIndex;
import evl.data.expression.reference.RefItem;
import evl.data.expression.reference.RefName;
import evl.data.expression.reference.RefTemplCall;
import evl.traverser.NullTraverser;

public class CopyRef extends NullTraverser<RefItem, Void> {
  private CopyEvl cast;

  public CopyRef(CopyEvl cast) {
    super();
    this.cast = cast;
  }

  @Override
  protected RefItem visitDefault(Evl obj, Void param) {
    throw new RuntimeException("not yet implemented: " + obj.getClass().getCanonicalName());
  }

  @Override
  protected RefItem visitRefTemplCall(RefTemplCall obj, Void param) {
    return new RefTemplCall(obj.getInfo(), cast.copy(obj.actualParameter));
  }

  @Override
  protected RefItem visitRefCall(RefCall obj, Void param) {
    return new RefCall(obj.getInfo(), cast.copy(obj.actualParameter));
  }

  @Override
  protected RefItem visitRefName(RefName obj, Void param) {
    return new RefName(obj.getInfo(), obj.name);
  }

  @Override
  protected RefItem visitRefIndex(RefIndex obj, Void param) {
    return new RefIndex(obj.getInfo(), cast.copy(obj.index));
  }

}
