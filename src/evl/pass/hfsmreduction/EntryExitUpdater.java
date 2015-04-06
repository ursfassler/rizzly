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

package evl.pass.hfsmreduction;

import java.util.LinkedList;

import pass.EvlPass;

import common.ElementInfo;

import evl.data.Evl;
import evl.data.EvlList;
import evl.data.Namespace;
import evl.data.component.hfsm.ImplHfsm;
import evl.data.component.hfsm.State;
import evl.data.component.hfsm.StateComposite;
import evl.data.component.hfsm.StateItem;
import evl.data.component.hfsm.StateSimple;
import evl.data.expression.Expression;
import evl.data.expression.TupleValue;
import evl.data.expression.reference.RefCall;
import evl.data.expression.reference.Reference;
import evl.data.function.Function;
import evl.data.function.header.FuncPrivateVoid;
import evl.data.function.ret.FuncReturnNone;
import evl.data.statement.Block;
import evl.data.statement.CallStmt;
import evl.data.statement.Statement;
import evl.data.variable.FuncVariable;
import evl.knowledge.KnowBaseItem;
import evl.knowledge.KnowledgeBase;
import evl.traverser.NullTraverser;
import evl.traverser.other.ClassGetter;

/**
 * For every state, a new entry and exit function is added. This new function contains calls to the parent entry or exit
 * function. After this pass, it is safe to move the leaf states up.
 *
 * @author urs
 *
 */
public class EntryExitUpdater extends EvlPass {

  @Override
  public void process(Namespace evl, KnowledgeBase kb) {
    for (ImplHfsm hfsm : ClassGetter.getRecursive(ImplHfsm.class, evl)) {
      EntryExitUpdaterWorker know = new EntryExitUpdaterWorker(kb);
      know.traverse(hfsm, null);
    }
  }

}

class EntryExitUpdaterWorker extends NullTraverser<Void, EePar> {
  private final KnowBaseItem kbi;

  public EntryExitUpdaterWorker(KnowledgeBase kb) {
    super();
    this.kbi = kb.getEntry(KnowBaseItem.class);
  }

  @Override
  protected Void visitDefault(Evl obj, EePar param) {
    if (obj instanceof StateItem) {
      return null;
    } else {
      throw new RuntimeException("not yet implemented: " + obj.getClass().getCanonicalName());
    }
  }

  @Override
  protected Void visitImplHfsm(ImplHfsm obj, EePar param) {
    visit(obj.topstate, new EePar());
    return null;
  }

  @Override
  protected Void visitStateSimple(StateSimple obj, EePar param) {
    changeEe(obj, param);
    return null;
  }

  public void changeEe(State obj, EePar param) {
    FuncPrivateVoid entry = makeFunc(param.entry, "_centry");
    obj.item.add(entry);
    obj.entryFunc.link = entry;

    FuncPrivateVoid exit = makeFunc(param.exit, "_cexit");
    obj.item.add(exit);
    obj.exitFunc.link = exit;
  }

  public FuncPrivateVoid makeFunc(LinkedList<Function> list, String name) {
    FuncPrivateVoid func = new FuncPrivateVoid(ElementInfo.NO, name, new EvlList<FuncVariable>(), new FuncReturnNone(ElementInfo.NO), new Block(ElementInfo.NO));

    for (Function cf : list) {
      Statement stmt = makeCall(cf);
      func.body.statements.add(stmt);
    }

    return func;
  }

  private CallStmt makeCall(Function func) {
    assert (func.param.isEmpty());
    Reference ref = new Reference(ElementInfo.NO, func);
    ref.offset.add(new RefCall(ElementInfo.NO, new TupleValue(ElementInfo.NO, new EvlList<Expression>())));
    return new CallStmt(ElementInfo.NO, ref);
  }

  @Override
  protected Void visitStateComposite(StateComposite obj, EePar param) {
    visitList(obj.item, param);
    changeEe(obj, param);
    return null;
  }

  @Override
  protected Void visitState(State obj, EePar param) {
    param = new EePar(param);
    param.entry.addLast(obj.entryFunc.link);
    param.exit.addFirst(obj.exitFunc.link);
    return super.visitState(obj, param);
  }

}

class EePar {
  final public LinkedList<Function> entry;
  final public LinkedList<Function> exit;

  public EePar() {
    entry = new LinkedList<Function>();
    exit = new LinkedList<Function>();
  }

  public EePar(EePar parent) {
    entry = new LinkedList<Function>(parent.entry);
    exit = new LinkedList<Function>(parent.exit);
  }

}
