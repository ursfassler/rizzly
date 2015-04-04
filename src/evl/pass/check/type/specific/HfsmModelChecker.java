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

package evl.pass.check.type.specific;

import error.ErrorType;
import error.RError;
import evl.data.Evl;
import evl.data.component.composition.SubCallbacks;
import evl.data.component.hfsm.ImplHfsm;
import evl.data.component.hfsm.State;
import evl.data.component.hfsm.Transition;
import evl.data.function.header.FuncCtrlInDataIn;
import evl.data.function.header.FuncCtrlInDataOut;
import evl.data.function.header.FuncPrivateRet;
import evl.data.function.header.FuncPrivateVoid;
import evl.data.variable.StateVariable;
import evl.knowledge.KnowBaseItem;
import evl.knowledge.KnowledgeBase;
import evl.traverser.NullTraverser;

//TODO check for unused states
//TODO check if a transition is never used
//TODO check that all queries are defined
//TODO check that no event is handled within a state
public class HfsmModelChecker extends NullTraverser<Void, Void> {
  private KnowledgeBase kb;
  private KnowBaseItem kbi;

  public HfsmModelChecker(KnowledgeBase kb) {
    super();
    this.kb = kb;
    kbi = kb.getEntry(KnowBaseItem.class);
  }

  public static void process(ImplHfsm obj, KnowledgeBase kb) {
    HfsmModelChecker check = new HfsmModelChecker(kb);
    check.traverse(obj, null);
  }

  @Override
  protected Void visitDefault(Evl obj, Void sym) {
    throw new RuntimeException("not yet implemented: " + obj.getClass().getSimpleName());
  }

  @Override
  protected Void visitSubCallbacks(SubCallbacks obj, Void param) {
    visitList(obj.func, param);
    return null;
  }

  @Override
  protected Void visitImplHfsm(ImplHfsm obj, Void param) {
    visit(obj.topstate, param);
    return null;
  }

  @Override
  protected Void visitState(State obj, Void param) {
    visitList(obj.item, param);
    return null;
  }

  @Override
  protected Void visitTransition(Transition obj, Void param) {
    // TODO check that guard does not write state
    if (!(obj.eventFunc.link instanceof FuncCtrlInDataIn)) {
      RError.err(ErrorType.Error, obj.getInfo(), "transition can only be triggered by slot");
    }
    return null;
  }

  @Override
  protected Void visitFuncIfaceInRet(FuncCtrlInDataOut obj, Void param) {
    // TODO check that state is not written
    return null;
  }

  @Override
  protected Void visitFuncPrivateVoid(FuncPrivateVoid obj, Void param) {
    return null;
  }

  @Override
  protected Void visitFuncPrivateRet(FuncPrivateRet obj, Void param) {
    return null;
  }

  @Override
  protected Void visitStateVariable(StateVariable obj, Void param) {
    return null;
  }

}
