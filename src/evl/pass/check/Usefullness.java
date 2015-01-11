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

package evl.pass.check;

import pass.EvlPass;
import error.ErrorType;
import error.RError;
import evl.function.header.FuncCtrlInDataIn;
import evl.function.header.FuncCtrlInDataOut;
import evl.function.header.FuncCtrlOutDataIn;
import evl.function.header.FuncCtrlOutDataOut;
import evl.knowledge.KnowledgeBase;
import evl.other.Component;
import evl.other.Namespace;
import evl.traverser.ClassGetter;

// TODO do test in FUN part, issues warnings only once for parameterized
// components.
/**
 * Checks if there is a input and output data flow. Gives a warning otherwise.
 *
 * A component with only input or output data flow can not do a lot (or not more than a global function can do).
 */
public class Usefullness extends EvlPass {

  @Override
  public void process(Namespace evl, KnowledgeBase kb) {
    for (Component comp : ClassGetter.get(Component.class, evl)) {
      boolean inEmpty = comp.getIface().getItems(FuncCtrlInDataIn.class).isEmpty() && comp.getIface().getItems(FuncCtrlOutDataIn.class).isEmpty();
      boolean outEmpty = comp.getIface().getItems(FuncCtrlOutDataOut.class).isEmpty() && comp.getIface().getItems(FuncCtrlInDataOut.class).isEmpty();
      String name = comp.getName();
      if (inEmpty && outEmpty) {
        RError.err(ErrorType.Warning, comp.getInfo(), "Component " + name + " has no input and no output data flow");
      } else if (outEmpty) {
        RError.err(ErrorType.Warning, comp.getInfo(), "Component " + name + " has no output data flow");
      } else if (inEmpty) {
        RError.err(ErrorType.Warning, comp.getInfo(), "Component " + name + " has no input data flow");
      }
    }
  }

}
