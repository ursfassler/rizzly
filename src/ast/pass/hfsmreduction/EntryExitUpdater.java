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

package ast.pass.hfsmreduction;

import java.util.LinkedList;

import ast.ElementInfo;
import ast.data.Ast;
import ast.data.AstList;
import ast.data.Namespace;
import ast.data.component.hfsm.ImplHfsm;
import ast.data.component.hfsm.State;
import ast.data.component.hfsm.StateComposite;
import ast.data.component.hfsm.StateContent;
import ast.data.component.hfsm.StateSimple;
import ast.data.expression.Expression;
import ast.data.expression.TupleValue;
import ast.data.expression.reference.RefCall;
import ast.data.expression.reference.Reference;
import ast.data.function.Function;
import ast.data.function.header.FuncProcedure;
import ast.data.function.ret.FuncReturnNone;
import ast.data.statement.Block;
import ast.data.statement.CallStmt;
import ast.data.statement.Statement;
import ast.data.variable.FuncVariable;
import ast.knowledge.KnowBaseItem;
import ast.knowledge.KnowledgeBase;
import ast.pass.AstPass;
import ast.traverser.NullTraverser;
import ast.traverser.other.ClassGetter;

/**
 * For every state, a new entry and exit function is added. This new function contains calls to the parent entry or exit
 * function. After this pass, it is safe to move the leaf states up.
 *
 * @author urs
 *
 */
public class EntryExitUpdater extends AstPass {

  @Override
  public void process(Namespace ast, KnowledgeBase kb) {
    for (ImplHfsm hfsm : ClassGetter.getRecursive(ImplHfsm.class, ast)) {
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
  protected Void visitDefault(Ast obj, EePar param) {
    if (obj instanceof StateContent) {
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
    FuncProcedure entry = makeFunc(param.entry, "_centry");
    obj.item.add(entry);
    obj.entryFunc.link = entry;

    FuncProcedure exit = makeFunc(param.exit, "_cexit");
    obj.item.add(exit);
    obj.exitFunc.link = exit;
  }

  public FuncProcedure makeFunc(LinkedList<Function> list, String name) {
    FuncProcedure func = new FuncProcedure(ElementInfo.NO, name, new AstList<FuncVariable>(), new FuncReturnNone(ElementInfo.NO), new Block(ElementInfo.NO));

    for (Function cf : list) {
      Statement stmt = makeCall(cf);
      func.body.statements.add(stmt);
    }

    return func;
  }

  private CallStmt makeCall(Function func) {
    assert (func.param.isEmpty());
    Reference ref = new Reference(ElementInfo.NO, func);
    ref.offset.add(new RefCall(ElementInfo.NO, new TupleValue(ElementInfo.NO, new AstList<Expression>())));
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
