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

package ast.pass.reduction.hfsm;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import ast.Designator;
import ast.ElementInfo;
import ast.copy.Copy;
import ast.data.Ast;
import ast.data.AstList;
import ast.data.Namespace;
import ast.data.component.hfsm.ImplHfsm;
import ast.data.component.hfsm.State;
import ast.data.component.hfsm.StateComposite;
import ast.data.component.hfsm.StateContent;
import ast.data.component.hfsm.StateSimple;
import ast.data.component.hfsm.Transition;
import ast.data.expression.Expression;
import ast.data.expression.TupleValue;
import ast.data.expression.reference.RefCall;
import ast.data.expression.reference.Reference;
import ast.data.expression.reference.SimpleRef;
import ast.data.function.Function;
import ast.data.function.header.FuncProcedure;
import ast.data.function.ret.FuncReturnNone;
import ast.data.statement.Assignment;
import ast.data.statement.AssignmentSingle;
import ast.data.statement.Block;
import ast.data.statement.CallStmt;
import ast.data.statement.Statement;
import ast.data.variable.FuncVariable;
import ast.data.variable.StateVariable;
import ast.data.variable.Variable;
import ast.knowledge.KnowBaseItem;
import ast.knowledge.KnowParent;
import ast.knowledge.KnowledgeBase;
import ast.pass.AstPass;
import ast.repository.Collector;
import ast.repository.TypeFilter;
import ast.specification.IsClass;
import ast.traverser.NullTraverser;

/**
 * adds transitions to the children until leaf states also adds calls to exit and entry function
 *
 * @author urs
 *
 */
public class TransitionDownPropagator extends AstPass {

  @Override
  public void process(Namespace ast, KnowledgeBase kb) {
    for (ImplHfsm hfsm : Collector.select(ast, new IsClass(ImplHfsm.class)).castTo(ImplHfsm.class)) {
      process(hfsm, kb);
    }
  }

  private static void process(ImplHfsm hfsm, KnowledgeBase kb) {
    KnowBaseItem kbi = kb.getEntry(KnowBaseItem.class);

    TransitionEndpointCollector tec = new TransitionEndpointCollector();
    tec.traverse(hfsm.topstate, null);

    assert (tec.getTdst().size() == tec.getTsrc().size());
    assert (tec.getTdst().size() == tec.getTtop().size());
    Map<Transition, FuncProcedure> tfunc = new HashMap<Transition, FuncProcedure>();
    int nr = 0;
    for (Transition trans : tec.getTdst().keySet()) {
      String name = Designator.NAME_SEP + "trans" + nr;
      nr++;
      FuncProcedure func = makeTransBodyFunc(trans, name, kbi);
      hfsm.topstate.item.add(func);
      tfunc.put(trans, func);
    }

    TransitionDownPropagatorWorker redirecter = new TransitionDownPropagatorWorker(kb, tec.getTsrc(), tec.getTdst(), tec.getTtop(), tfunc);
    redirecter.traverse(hfsm.topstate, new TransitionParam());
  }

  /**
   * Extracts a function out of the transition body
   *
   * @param name
   */
  private static FuncProcedure makeTransBodyFunc(Transition trans, String name, KnowBaseItem kbi) {
    AstList<FuncVariable> params = Copy.copy(trans.param);
    FuncProcedure func = new FuncProcedure(ElementInfo.NO, name, params, new FuncReturnNone(ElementInfo.NO), trans.body);
    trans.body = new Block(ElementInfo.NO);

    FsmReduction.relinkActualParameterRef(trans.param, func.param, func.body);

    return func;
  }

}

class TransitionDownPropagatorWorker extends NullTraverser<Void, TransitionParam> {
  private static final ElementInfo info = ElementInfo.NO;
  private final KnowParent kp;
  private final Map<Transition, State> tsrc;
  private final Map<Transition, State> tdst;
  private final Map<Transition, State> ttop;
  private final Map<Transition, FuncProcedure> tfunc;

  public TransitionDownPropagatorWorker(KnowledgeBase kb, Map<Transition, State> tsrc, Map<Transition, State> tdst, Map<Transition, State> ttop, Map<Transition, FuncProcedure> tfunc) {
    super();
    this.tsrc = tsrc;
    this.tdst = tdst;
    this.ttop = ttop;
    this.tfunc = tfunc;
    kp = kb.getEntry(KnowParent.class);
  }

  @Override
  protected Void visitDefault(Ast obj, TransitionParam param) {
    throw new RuntimeException("not yet implemented: " + obj.getClass().getCanonicalName());
  }

  @Override
  protected Void visitStateSimple(StateSimple obj, TransitionParam param) {
    AstList<Transition> transList = TypeFilter.select(obj.item, Transition.class);
    obj.item.removeAll(transList);

    filter(obj, param.before);
    filter(obj, param.after);

    for (Transition trans : param.before) {
      addTrans(obj, trans);
    }
    for (Transition trans : transList) {
      trans.src = new SimpleRef<State>(trans.src.getInfo(), obj);
      // obj.getItem().add(trans);
      addTrans(obj, trans);
    }
    for (Transition trans : param.after) {
      addTrans(obj, trans);
    }
    return null;
  }

  private void addTrans(StateSimple src, Transition otrans) {
    State os = ttop.get(otrans);
    assert (os != null);
    State dst = tdst.get(otrans);
    assert (dst != null);
    Transition trans = Copy.copy(otrans);
    trans.src = new SimpleRef<State>(trans.src.getInfo(), src);

    makeExitCalls(src, os, trans.body.statements);
    {
      FuncProcedure func = tfunc.get(otrans);
      assert (func != null);
      Reference ref = new Reference(info, func);

      TupleValue param = new TupleValue(info, new AstList<Expression>());
      for (Variable acpar : trans.param) {
        Reference parref = new Reference(info, acpar);
        param.value.add(parref);
      }

      ref.offset.add(new RefCall(info, param));
      CallStmt call = new CallStmt(info, ref);
      trans.body.statements.add(call);
    }
    makeVarInit(dst, os, trans.body.statements);
    makeEntryCalls(dst, os, trans.body.statements);

    src.item.add(trans);
  }

  private void makeVarInit(State start, State top, List<Statement> list) {
    if (start == top) {
      return;
    }
    StateComposite par = getParent(start);
    assert (par != null);

    makeVarInit(par, top, list);

    for (StateVariable var : TypeFilter.select(start.item, StateVariable.class)) {
      Assignment init = new AssignmentSingle(var.def.getInfo(), new Reference(info, var), Copy.copy(var.def));
      list.add(init);
    }
  }

  private void makeEntryCalls(State start, State top, List<Statement> list) {
    if (start == top) {
      return;
    }
    StateComposite par = getParent(start);
    assert (par != null);

    makeEntryCalls(par, top, list);
    list.add(makeCall(start.entryFunc.link));
  }

  private StateComposite getParent(State start) {
    Ast parent = kp.get(start);
    if (parent instanceof StateComposite) {
      return (StateComposite) parent;
    } else {
      return null;
    }
  }

  private void makeExitCalls(State start, State top, List<Statement> list) {
    if (start == top) {
      return;
    }
    StateComposite par = getParent(start);
    assert (par != null);

    list.add(makeCall(start.exitFunc.link));
    makeExitCalls(par, top, list);
  }

  private CallStmt makeCall(Function func) {
    Reference ref = new Reference(info, func);
    ref.offset.add(new RefCall(info, new TupleValue(info, new AstList<Expression>())));
    return new CallStmt(info, ref);
  }

  private boolean isChildState(State test, State root) {
    if (test == null) {
      return false;
    } else if (test == root) {
      return true;
    } else {
      return isChildState(getParent(test), root);
    }
  }

  @Override
  protected Void visitStateComposite(StateComposite obj, TransitionParam param) {
    /*
     * for all states in obj.getItem() we want to know, which transitions are before and which ones are after the
     * specific state.
     * 
     * the map spos contains the position of the state in the transition array
     */

    Map<State, Integer> spos = new HashMap<State, Integer>();
    ArrayList<Transition> transList = new ArrayList<Transition>();
    ArrayList<State> stateList = new ArrayList<State>();

    for (StateContent itr : obj.item) {
      if (itr instanceof Transition) {
        Transition trans = (Transition) itr;
        transList.add(trans);
      } else if (itr instanceof State) {
        spos.put((State) itr, transList.size());
        stateList.add((State) itr);
      }
    }

    obj.item.removeAll(transList);

    // build parameter for every substate
    Map<State, TransitionParam> spar = new HashMap<State, TransitionParam>();
    for (State itr : stateList) {
      int idx = spos.get(itr);

      TransitionParam cpar = new TransitionParam(param);
      cpar.before.addAll(transList.subList(0, idx));
      cpar.after.addAll(0, transList.subList(idx, transList.size()));

      spar.put(itr, cpar);
    }

    for (State itr : stateList) {
      visit(itr, spar.get(itr));
    }

    return null;
  }

  // removes transitions which does not come from this state or a parent of it
  private void filter(State state, List<Transition> list) {
    Set<Transition> remove = new HashSet<Transition>();
    for (Transition trans : list) {
      if (!isChildState(state, tsrc.get(trans))) {
        remove.add(trans);
      }
    }
    list.removeAll(remove);
  }
}

class TransitionParam {

  final public ArrayList<Transition> before;
  final public ArrayList<Transition> after;

  public TransitionParam(List<Transition> before, List<Transition> after) {
    super();
    this.before = new ArrayList<Transition>(before);
    this.after = new ArrayList<Transition>(after);
  }

  public TransitionParam() {
    super();
    this.before = new ArrayList<Transition>();
    this.after = new ArrayList<Transition>();
  }

  public TransitionParam(TransitionParam parent) {
    super();
    this.before = new ArrayList<Transition>(parent.before);
    this.after = new ArrayList<Transition>(parent.after);
  }
}

class TransitionEndpointCollector extends NullTraverser<Void, State> {

  final private Map<Transition, State> tsrc = new HashMap<Transition, State>();
  final private Map<Transition, State> tdst = new HashMap<Transition, State>();
  final private Map<Transition, State> ttop = new HashMap<Transition, State>();

  @Override
  protected Void visitDefault(Ast obj, State param) {
    if (obj instanceof StateContent) {
      return null;
    } else {
      throw new RuntimeException("not yet implemented: " + obj.getClass().getCanonicalName());
    }
  }

  @Override
  protected Void visitTransition(Transition obj, State param) {
    ttop.put(obj, param);
    tsrc.put(obj, (State) obj.src.getTarget());
    tdst.put(obj, (State) obj.dst.getTarget());
    return null;
  }

  @Override
  protected Void visitState(State obj, State param) {
    visitList(obj.item, obj);
    return null;
  }

  public Map<Transition, State> getTsrc() {
    return tsrc;
  }

  public Map<Transition, State> getTdst() {
    return tdst;
  }

  public Map<Transition, State> getTtop() {
    return ttop;
  }
}
