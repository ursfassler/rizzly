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

import java.util.LinkedList;

import common.ElementInfo;

import evl.Evl;
import evl.NullTraverser;
import evl.expression.Expression;
import evl.expression.reference.RefCall;
import evl.expression.reference.Reference;
import evl.expression.reference.SimpleRef;
import evl.function.Function;
import evl.function.header.FuncPrivateVoid;
import evl.hfsm.ImplHfsm;
import evl.hfsm.State;
import evl.hfsm.StateComposite;
import evl.hfsm.StateItem;
import evl.hfsm.StateSimple;
import evl.knowledge.KnowBaseItem;
import evl.knowledge.KnowledgeBase;
import evl.other.EvlList;
import evl.statement.Block;
import evl.statement.CallStmt;
import evl.statement.Statement;
import evl.type.Type;
import evl.variable.FuncVariable;

/**
 * For every state, a new entry and exit function is added. This new function contains calls to the parent entry or exit
 * function. After this pass, it is safe to move the leaf states up.
 *
 * @author urs
 *
 */
public class EntryExitUpdater extends NullTraverser<Void, EePar> {
  private final KnowBaseItem kbi;

  public EntryExitUpdater(KnowledgeBase kb) {
    super();
    this.kbi = kb.getEntry(KnowBaseItem.class);
  }

  static public void process(ImplHfsm obj, KnowledgeBase kb) {
    EntryExitUpdater know = new EntryExitUpdater(kb);
    know.traverse(obj, null);
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
    visit(obj.getTopstate(), new EePar());
    return null;
  }

  @Override
  protected Void visitStateSimple(StateSimple obj, EePar param) {
    changeEe(obj, param);
    return null;
  }

  public void changeEe(State obj, EePar param) {
    FuncPrivateVoid entry = makeFunc(param.entry, "_centry");
    obj.getItem().add(entry);
    obj.getEntryFunc().setLink(entry);

    FuncPrivateVoid exit = makeFunc(param.exit, "_cexit");
    obj.getItem().add(exit);
    obj.getExitFunc().setLink(exit);
  }

  public FuncPrivateVoid makeFunc(LinkedList<Function> list, String name) {
    FuncPrivateVoid func = new FuncPrivateVoid(ElementInfo.NO, name, new EvlList<FuncVariable>(), new SimpleRef<Type>(ElementInfo.NO, kbi.getVoidType()), new Block(ElementInfo.NO));

    for (Function cf : list) {
      Statement stmt = makeCall(cf);
      func.getBody().getStatements().add(stmt);
    }

    return func;
  }

  private CallStmt makeCall(Function func) {
    assert (func.getParam().isEmpty());
    Reference ref = new Reference(ElementInfo.NO, func);
    ref.getOffset().add(new RefCall(ElementInfo.NO, new EvlList<Expression>()));
    return new CallStmt(ElementInfo.NO, ref);
  }

  @Override
  protected Void visitStateComposite(StateComposite obj, EePar param) {
    visitList(obj.getItem(), param);
    changeEe(obj, param);
    return null;
  }

  @Override
  protected Void visitState(State obj, EePar param) {
    param = new EePar(param);
    param.entry.addLast(obj.getEntryFunc().getLink());
    param.exit.addFirst(obj.getExitFunc().getLink());
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
