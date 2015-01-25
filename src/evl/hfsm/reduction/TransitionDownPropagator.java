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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import common.Designator;
import common.ElementInfo;

import evl.Evl;
import evl.NullTraverser;
import evl.copy.Copy;
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
import evl.hfsm.Transition;
import evl.knowledge.KnowBaseItem;
import evl.knowledge.KnowParent;
import evl.knowledge.KnowledgeBase;
import evl.other.EvlList;
import evl.statement.Assignment;
import evl.statement.Block;
import evl.statement.CallStmt;
import evl.statement.Statement;
import evl.type.Type;
import evl.variable.FuncVariable;
import evl.variable.StateVariable;
import evl.variable.Variable;

/**
 * adds transitions to the children until leaf states also adds calls to exit and entry function
 *
 * @author urs
 *
 */
public class TransitionDownPropagator extends NullTraverser<Void, TransitionParam> {

  private static final ElementInfo info = ElementInfo.NO;
  private final KnowParent kp;
  private final Map<Transition, State> tsrc;
  private final Map<Transition, State> tdst;
  private final Map<Transition, State> ttop;
  private final Map<Transition, FuncPrivateVoid> tfunc;

  public TransitionDownPropagator(KnowledgeBase kb, Map<Transition, State> tsrc, Map<Transition, State> tdst, Map<Transition, State> ttop, Map<Transition, FuncPrivateVoid> tfunc) {
    super();
    this.tsrc = tsrc;
    this.tdst = tdst;
    this.ttop = ttop;
    this.tfunc = tfunc;
    kp = kb.getEntry(KnowParent.class);
  }

  public static void process(ImplHfsm hfsm, KnowledgeBase kb) {
    KnowBaseItem kbi = kb.getEntry(KnowBaseItem.class);

    TransitionEndpointCollector tec = new TransitionEndpointCollector();
    tec.traverse(hfsm.getTopstate(), null);

    assert (tec.getTdst().size() == tec.getTsrc().size());
    assert (tec.getTdst().size() == tec.getTtop().size());
    Map<Transition, FuncPrivateVoid> tfunc = new HashMap<Transition, FuncPrivateVoid>();
    int nr = 0;
    for (Transition trans : tec.getTdst().keySet()) {
      String name = Designator.NAME_SEP + "trans" + nr;
      nr++;
      FuncPrivateVoid func = makeTransBodyFunc(trans, name, kbi);
      hfsm.getTopstate().getItem().add(func);
      tfunc.put(trans, func);
    }

    TransitionDownPropagator redirecter = new TransitionDownPropagator(kb, tec.getTsrc(), tec.getTdst(), tec.getTtop(), tfunc);
    redirecter.traverse(hfsm.getTopstate(), new TransitionParam());
  }

  /**
   * Extracts a function out of the transition body
   *
   * @param name
   */
  private static FuncPrivateVoid makeTransBodyFunc(Transition trans, String name, KnowBaseItem kbi) {
    EvlList<FuncVariable> params = Copy.copy(trans.getParam());
    FuncPrivateVoid func = new FuncPrivateVoid(info, name, params, new SimpleRef<Type>(info, kbi.getVoidType()), trans.getBody());
    trans.setBody(new Block(info));

    FsmReduction.relinkActualParameterRef(trans.getParam(), func.getParam(), func.getBody());

    return func;
  }

  @Override
  protected Void visitDefault(Evl obj, TransitionParam param) {
    throw new RuntimeException("not yet implemented: " + obj.getClass().getCanonicalName());
  }

  @Override
  protected Void visitStateSimple(StateSimple obj, TransitionParam param) {
    EvlList<Transition> transList = obj.getItem().getItems(Transition.class);
    obj.getItem().removeAll(transList);

    filter(obj, param.before);
    filter(obj, param.after);

    for (Transition trans : param.before) {
      addTrans(obj, trans);
    }
    for (Transition trans : transList) {
      trans.getSrc().setLink(obj);
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
    trans.getSrc().setLink(src);

    makeExitCalls(src, os, trans.getBody().getStatements());
    {
      FuncPrivateVoid func = tfunc.get(otrans);
      assert (func != null);
      Reference ref = new Reference(info, func);

      EvlList<Expression> param = new EvlList<Expression>();
      for (Variable acpar : trans.getParam()) {
        Reference parref = new Reference(info, acpar);
        param.add(parref);
      }

      ref.getOffset().add(new RefCall(info, param));
      CallStmt call = new CallStmt(info, ref);
      trans.getBody().getStatements().add(call);
    }
    makeVarInit(dst, os, trans.getBody().getStatements());
    makeEntryCalls(dst, os, trans.getBody().getStatements());

    src.getItem().add(trans);
  }

  private void makeVarInit(State start, State top, List<Statement> list) {
    if (start == top) {
      return;
    }
    StateComposite par = getParent(start);
    assert (par != null);

    makeVarInit(par, top, list);

    for (StateVariable var : start.getItem().getItems(StateVariable.class)) {
      Assignment init = new Assignment(var.getDef().getInfo(), new Reference(info, var), Copy.copy(var.getDef()));
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
    list.add(makeCall(start.getEntryFunc().getLink()));
  }

  private StateComposite getParent(State start) {
    Evl parent = kp.getParent(start);
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

    list.add(makeCall(start.getExitFunc().getLink()));
    makeExitCalls(par, top, list);
  }

  private CallStmt makeCall(Function func) {
    Reference ref = new Reference(info, func);
    ref.getOffset().add(new RefCall(info, new EvlList<Expression>()));
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

    for (StateItem itr : obj.getItem()) {
      if (itr instanceof Transition) {
        Transition trans = (Transition) itr;
        transList.add(trans);
      } else if (itr instanceof State) {
        spos.put((State) itr, transList.size());
        stateList.add((State) itr);
      }
    }

    obj.getItem().removeAll(transList);

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
  protected Void visitDefault(Evl obj, State param) {
    if (obj instanceof StateItem) {
      return null;
    } else {
      throw new RuntimeException("not yet implemented: " + obj.getClass().getCanonicalName());
    }
  }

  @Override
  protected Void visitTransition(Transition obj, State param) {
    ttop.put(obj, param);
    tsrc.put(obj, obj.getSrc().getLink());
    tdst.put(obj, obj.getDst().getLink());
    return null;
  }

  @Override
  protected Void visitState(State obj, State param) {
    visitList(obj.getItem(), obj);
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
