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

package evl.pass.check.io;

import evl.DefTraverser;
import evl.Evl;
import evl.expression.reference.Reference;
import evl.variable.StateVariable;
import evl.variable.Variable;

/**
 * Returns for every function if it reads state. It does not check called functions nor it uses information from the
 * function type or header.
 *
 * @author urs
 *
 */
public class StateReaderInfo extends DefTraverser<Void, Void> {
  private boolean readState = false;

  public static boolean get(Evl inst) {
    StateReaderInfo reduction = new StateReaderInfo();
    reduction.traverse(inst, null);
    return reduction.readState;
  }

  private boolean isStateVariable(Variable var) {
    return var instanceof StateVariable;
  }

  @Override
  protected Void visitReference(Reference obj, Void param) {
    if (obj.getLink() instanceof Variable) {
      Variable var = (Variable) obj.getLink();
      if (isStateVariable(var)) {
        readState = true;
      }
    }
    return super.visitReference(obj, param);
  }

}