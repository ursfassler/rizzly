package evl.hfsm.reduction;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import common.ElementInfo;

import evl.Evl;
import evl.NullTraverser;
import evl.copy.Copy;
import evl.expression.Expression;
import evl.expression.reference.RefCall;
import evl.expression.reference.RefItem;
import evl.expression.reference.Reference;
import evl.function.FuncWithBody;
import evl.hfsm.ImplHfsm;
import evl.hfsm.QueryItem;
import evl.hfsm.State;
import evl.hfsm.StateComposite;
import evl.hfsm.StateItem;
import evl.hfsm.StateSimple;
import evl.hfsm.Transition;
import evl.knowledge.KnowParent;
import evl.knowledge.KnowledgeBase;
import evl.statement.normal.CallStmt;
import evl.statement.Statement;
import evl.statement.normal.NormalStmt;

/**
 * adds transitions to the children until leaf states also adds calls to exit and entry function
 *
 * @author urs
 *
 */
public class TransitionDownPropagator extends NullTraverser<Void, TransitionParam> {

  private KnowParent kp;
  private Map<Transition, State> tsrc;
  private Map<Transition, State> tdst;
  private Map<Transition, State> ttop;

  public TransitionDownPropagator(KnowledgeBase kb, Map<Transition, State> tsrc, Map<Transition, State> tdst, Map<Transition, State> ttop) {
    super();
    this.tsrc = tsrc;
    this.tdst = tdst;
    this.ttop = ttop;
    kp = kb.getEntry(KnowParent.class);
  }

  public static void process(ImplHfsm hfsm, KnowledgeBase kb) {
    TransitionEndpointCollector tec = new TransitionEndpointCollector();
    tec.traverse(hfsm.getTopstate(), null);

    TransitionDownPropagator redirecter = new TransitionDownPropagator(kb, tec.getTsrc(), tec.getTdst(), tec.getTtop());
    redirecter.traverse(hfsm.getTopstate(), new TransitionParam());
  }

  @Override
  protected Void visitDefault(Evl obj, TransitionParam param) {
    throw new RuntimeException("not yet implemented: " + obj.getClass().getCanonicalName());
  }

  @Override
  protected Void visitStateSimple(StateSimple obj, TransitionParam param) {
    List<Transition> transList = obj.getItemList(Transition.class);
    obj.getItem().removeAll(transList);

    filter(obj, param.before);
    filter(obj, param.after);

    for( Transition trans : param.before ) {
      addTrans(obj, trans);
    }
    for( Transition trans : transList ) {
      trans.setSrc(obj);
      obj.getItem().add(trans);
    }
    for( Transition trans : param.after ) {
      addTrans(obj, trans);
    }
    return null;
  }

  public void addTrans(StateSimple src, Transition trans) {
    State os = ttop.get(trans);
    assert ( os != null );
    State dst = tdst.get(trans);
    assert ( dst != null );
    trans = Copy.copy(trans);
    trans.setSrc(src);

    {
      List<NormalStmt> list = new ArrayList<NormalStmt>();
      makeExitCalls(src, os, list);
      trans.getBody().insertCodeAfterEntry(list, "exitCalls");    //TODO check
    }
    {
      List<NormalStmt> list = new ArrayList<NormalStmt>();
      makeEntryCalls(dst, os, list);
      trans.getBody().insertCodeAfterEntry(list, "entryCalls");//TODO check
    }

    src.getItem().add(trans);
  }

  private void makeEntryCalls(State start, State top, List<NormalStmt> list) {
    if( start == top ) {
      return;
    }
    StateComposite par = getParent(start);
    assert ( par != null );

    makeEntryCalls(par, top, list);
    list.add(makeCall(start.getEntryFunc()));
  }

  private StateComposite getParent(State start) {
    Evl parent = kp.getParent(start);
    if( parent instanceof StateComposite ) {
      return (StateComposite) parent;
    } else {
      return null;
    }
  }

  private void makeExitCalls(State start, State top, List<NormalStmt> list) {
    if( start == top ) {
      return;
    }
    StateComposite par = getParent(start);
    assert ( par != null );

    list.add(makeCall(start.getExitFunc()));
    makeExitCalls(par, top, list);
  }

  private NormalStmt makeCall(Reference reference) {
    assert ( reference.getLink() instanceof FuncWithBody );
    assert ( reference.getOffset().isEmpty() );
    Reference ref = new Reference(new ElementInfo(), reference.getLink(), new ArrayList<RefItem>());
    ref.getOffset().add(new RefCall(new ElementInfo(), new ArrayList<Expression>()));
    return new CallStmt(new ElementInfo(), ref);
  }

  private boolean isChildState(State test, State root) {
    if( test == null ) {
      return false;
    } else if( test == root ) {
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

    for( StateItem itr : obj.getItem() ) {
      if( itr instanceof Transition ) {
        Transition trans = (Transition) itr;
        transList.add(trans);
      } else if( itr instanceof State ) {
        spos.put((State) itr, transList.size());
        stateList.add((State) itr);
      } else {
        assert ( itr instanceof QueryItem );
      }
    }

    obj.getItem().removeAll(transList);

    // build parameter for every substate
    Map<State, TransitionParam> spar = new HashMap<State, TransitionParam>();
    for( State itr : stateList ) {
      int idx = spos.get(itr);

      TransitionParam cpar = new TransitionParam(param);
      cpar.before.addAll(transList.subList(0, idx));
      cpar.after.addAll(0, transList.subList(idx, transList.size()));

      spar.put(itr, cpar);
    }

    for( State itr : stateList ) {
      visit(itr, spar.get(itr));
    }

    return null;
  }

  // removes transitions which does not come from this state or a parent of it
  private void filter(State state, List<Transition> list) {
    Set<Transition> remove = new HashSet<Transition>();
    for( Transition trans : list ) {
      if( !isChildState(state, tsrc.get(trans)) ) {
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
    throw new RuntimeException("not yet implemented: " + obj.getClass().getCanonicalName());
  }

  @Override
  protected Void visitQueryItem(QueryItem obj, State param) {
    return null;
  }

  @Override
  protected Void visitTransition(Transition obj, State param) {
    ttop.put(obj, param);
    tsrc.put(obj, obj.getSrc());
    tdst.put(obj, obj.getDst());
    return null;
  }

  @Override
  protected Void visitState(State obj, State param) {
    visitItr(obj.getItem(), obj);
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
