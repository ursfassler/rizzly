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

package evl.traverser;

import evl.Evl;
import evl.NullTraverser;
import evl.function.Function;
import evl.function.header.FuncCtrlInDataIn;
import evl.function.header.FuncCtrlInDataOut;
import evl.function.header.FuncCtrlOutDataIn;
import evl.function.header.FuncCtrlOutDataOut;
import evl.function.header.FuncGlobal;
import evl.function.header.FuncPrivateRet;
import evl.function.header.FuncPrivateVoid;
import evl.function.header.FuncSubHandlerEvent;
import evl.function.header.FuncSubHandlerQuery;

/**
 * Returns for every function if, it writes to outside. It gets the information only from the function type.
 *
 * @author urs
 *
 */
public class OutsideWriterInfo extends NullTraverser<Boolean, Void> {

  public static Boolean get(Function inst) {
    OutsideWriterInfo reduction = new OutsideWriterInfo();
    return reduction.traverse(inst, null);
  }

  @Override
  protected Boolean visitDefault(Evl obj, Void param) {
    throw new RuntimeException("not yet implemented: " + obj.getClass().getCanonicalName());
  }

  @Override
  protected Boolean visitFuncGlobal(FuncGlobal obj, Void param) {
    return false;
  }

  @Override
  protected Boolean visitFuncPrivateVoid(FuncPrivateVoid obj, Void param) {
    return false;
  }

  @Override
  protected Boolean visitFuncPrivateRet(FuncPrivateRet obj, Void param) {
    return false;
  }

  @Override
  protected Boolean visitFuncSubHandlerQuery(FuncSubHandlerQuery obj, Void param) {
    return false;
  }

  @Override
  protected Boolean visitFuncSubHandlerEvent(FuncSubHandlerEvent obj, Void param) {
    return false;
  }

  @Override
  protected Boolean visitFuncIfaceOutVoid(FuncCtrlOutDataOut obj, Void param) {
    return true;
  }

  @Override
  protected Boolean visitFuncIfaceOutRet(FuncCtrlOutDataIn obj, Void param) {
    return false;
  }

  @Override
  protected Boolean visitFuncIfaceInVoid(FuncCtrlInDataIn obj, Void param) {
    return false; // FIXME sure?
  }

  @Override
  protected Boolean visitFuncIfaceInRet(FuncCtrlInDataOut obj, Void param) {
    return false;
  }

}
