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

package evl.traverser.iocheck;

import evl.DefTraverser;
import evl.Evl;
import evl.statement.Assignment;
import evl.variable.StateVariable;
import evl.variable.Variable;

/**
 * Returns for every function if it writes state. It does not check called functions nor it uses information from the
 * function type or header.
 *
 * @author urs
 *
 */
public class StateWriterInfo extends DefTraverser<Void, Void> {
  private boolean writeState = false;

  public static boolean get(Evl inst) {
    StateWriterInfo reduction = new StateWriterInfo();
    reduction.traverse(inst, null);
    return reduction.writeState;
  }

  private boolean isStateVariable(Variable var) {
    return var instanceof StateVariable;
  }

  @Override
  protected Void visitAssignment(Assignment obj, Void param) {
    if (obj.getLeft().getLink() instanceof Variable) {
      Variable var = (Variable) obj.getLeft().getLink();
      if (isStateVariable(var)) {
        writeState = true;
      }
    }
    return null;
  }

}
