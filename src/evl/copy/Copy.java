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
import evl.data.EvlList;

public final class Copy {

  public static <T extends Evl> T copy(T obj) {
    CopyEvl copier = new CopyEvl();
    T nobj = copier.copy(obj);
    Relinker.relink(nobj, copier.getCopied());
    return nobj;
  }

  public static <T extends Evl> EvlList<T> copy(EvlList<T> obj) {
    CopyEvl copier = new CopyEvl();
    EvlList<T> nobj = copier.copy(obj);
    Relinker.relink(nobj, copier.getCopied());
    return nobj;
  }

}
