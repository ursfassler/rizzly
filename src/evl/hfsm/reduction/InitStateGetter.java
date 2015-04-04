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

package evl.hfsm.reduction;

import evl.Evl;
import evl.NullTraverser;
import evl.expression.reference.Reference;
import evl.hfsm.State;
import evl.hfsm.StateComposite;
import evl.hfsm.StateSimple;

/**
 *
 * Gets the initial leaf state form a state
 *
 * @author urs
 *
 */
public class InitStateGetter extends NullTraverser<StateSimple, Void> {
  @Override
  protected StateSimple visitDefault(Evl obj, Void param) {
    throw new RuntimeException("not yet implemented: " + obj.getClass().getCanonicalName());
  }

  static public StateSimple get(State obj) {
    InitStateGetter getter = new InitStateGetter();
    return getter.traverse(obj, null);
  }

  @Override
  protected StateSimple visitStateSimple(StateSimple obj, Void param) {
    return obj;
  }

  @Override
  protected StateSimple visitStateComposite(StateComposite obj, Void param) {
    return visit(obj.initial.link, param);
  }

  @Override
  protected StateSimple visitReference(Reference obj, Void param) {
    assert (false);
    assert (obj.offset.isEmpty());
    return visit(obj.link, param);
  }

}
