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

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import pass.EvlPass;

import common.Designator;
import common.ElementInfo;

import error.ErrorType;
import error.RError;
import evl.copy.Relinker;
import evl.data.Evl;
import evl.data.EvlList;
import evl.data.Named;
import evl.data.Namespace;
import evl.data.component.elementary.ImplElementary;
import evl.data.component.hfsm.ImplHfsm;
import evl.data.component.hfsm.State;
import evl.data.component.hfsm.StateComposite;
import evl.data.component.hfsm.StateItem;
import evl.data.component.hfsm.StateSimple;
import evl.data.component.hfsm.Transition;
import evl.data.expression.Expression;
import evl.data.expression.TupleValue;
import evl.data.expression.reference.RefCall;
import evl.data.expression.reference.Reference;
import evl.data.expression.reference.SimpleRef;
import evl.data.function.Function;
import evl.data.function.header.FuncCtrlInDataIn;
import evl.data.function.header.FuncCtrlInDataOut;
import evl.data.function.header.FuncCtrlOutDataOut;
import evl.data.function.header.FuncPrivateVoid;
import evl.data.function.ret.FuncReturnNone;
import evl.data.statement.Assignment;
import evl.data.statement.AssignmentSingle;
import evl.data.statement.Block;
import evl.data.statement.CallStmt;
import evl.data.statement.CaseOpt;
import evl.data.statement.CaseOptEntry;
import evl.data.statement.CaseOptValue;
import evl.data.statement.CaseStmt;
import evl.data.statement.IfOption;
import evl.data.statement.IfStmt;
import evl.data.statement.ReturnExpr;
import evl.data.statement.Statement;
import evl.data.type.Type;
import evl.data.type.base.EnumElement;
import evl.data.type.base.EnumType;
import evl.data.variable.FuncVariable;
import evl.data.variable.StateVariable;
import evl.data.variable.Variable;
import evl.knowledge.KnowBaseItem;
import evl.knowledge.KnowLlvmLibrary;
import evl.knowledge.KnowledgeBase;
import evl.traverser.NullTraverser;

public class FsmReduction extends EvlPass {

  @Override
  public void process(Namespace evl, KnowledgeBase kb) {
    FsmReductionWorker reduction = new FsmReductionWorker(kb);
    reduction.traverse(evl, null);
    Relinker.relink(evl, reduction.getMap());
  }

  /**
   * relinking parameter references to new parameter
   */
  public static void relinkActualParameterRef(EvlList<FuncVariable> oldParam, EvlList<FuncVariable> newParam, Evl body) {
    assert (newParam.size() == oldParam.size());
    Map<Variable, Variable> map = new HashMap<Variable, Variable>();
    for (int i = 0; i < newParam.size(); i++) {
      map.put(oldParam.get(i), newParam.get(i));
    }
    Relinker.relink(body, map);
  }

}

class FsmReductionWorker extends NullTraverser<Evl, Namespace> {
  final private Reduction reduction;

  public FsmReductionWorker(KnowledgeBase kb) {
    super();
    reduction = new Reduction(kb);
  }

  public Map<? extends Named, ? extends Named> getMap() {
    return reduction.getMap();
  }

  @Override
  protected Evl visitDefault(Evl obj, Namespace param) {
    return obj;
  }

  @Override
  protected Evl visitNamespace(Namespace obj, Namespace param) {
    EvlList<Evl> list = obj.children;
    for (int i = 0; i < list.size(); i++) {
      Evl item = list.get(i);
      item = visit(item, obj);
      assert (item != null);
      list.set(i, item);
    }
    return obj;
  }

  @Override
  protected Evl visitImplHfsm(ImplHfsm obj, Namespace param) {
    return reduction.reduce(obj, param);
  }
}

class Reduction {
  static final private ElementInfo info = ElementInfo.NO;
  final private KnowLlvmLibrary kll;
  final private KnowBaseItem kbi;
  final private Map<ImplHfsm, ImplElementary> map = new HashMap<ImplHfsm, ImplElementary>();

  public Reduction(KnowledgeBase kb) {
    kll = kb.getEntry(KnowLlvmLibrary.class);
    kbi = kb.getEntry(KnowBaseItem.class);
  }

  public ImplElementary reduce(ImplHfsm obj, Namespace param) {
    ImplElementary elem = new ImplElementary(obj.getInfo(), obj.name, new SimpleRef<FuncPrivateVoid>(info, null), new SimpleRef<FuncPrivateVoid>(info, null));
    elem.iface.addAll(obj.iface);
    elem.function.addAll(obj.function);
    for (StateItem item : obj.topstate.item) {
      if (item instanceof Variable) {
        elem.variable.add((Variable) item);
      } else if (item instanceof Function) {
        elem.function.add((Function) item);
      } else if (item instanceof Type) {
        elem.type.add((Type) item);
      } else if (item instanceof Transition) {
      } else if (item instanceof StateSimple) {
      } else {
        RError.err(ErrorType.Fatal, item.getInfo(), "Unhandled StateItem: " + item.getClass().getCanonicalName());
      }
    }

    EnumType states = new EnumType(obj.topstate.getInfo(), obj.name + Designator.NAME_SEP + "State");
    HashMap<StateSimple, EnumElement> enumMap = makeEnumElem(obj.topstate, states);

    param.children.add(states);
    // String ena = (String) enumMap.get(obj.getTopstate().getInitial()).properties().get(Property.NAME);
    EnumElement ena = enumMap.get(obj.topstate.initial.link);
    Reference initState = makeEnumElemRef(states, ena);
    StateVariable stateVariable = new StateVariable(obj.getInfo(), "_statevar", new SimpleRef<Type>(info, states), initState);
    elem.variable.add(stateVariable);

    TransitionDict dict = new TransitionDict();
    dict.traverse(obj.topstate, null);

    // create event handler
    for (FuncCtrlInDataOut func : elem.iface.getItems(FuncCtrlInDataOut.class)) {
      assert (func.body.statements.isEmpty());
      Statement code = addQueryCode(enumMap.keySet(), enumMap, states, stateVariable, func, func.param);
      Block bbl = new Block(info);
      bbl.statements.add(code);
      func.body = bbl;
    }

    for (FuncCtrlInDataIn func : elem.iface.getItems(FuncCtrlInDataIn.class)) {
      assert (func.body.statements.isEmpty());
      Statement code = addTransitionCode(enumMap.keySet(), enumMap, states, stateVariable, func, dict, func.param);
      Block bbl = new Block(info);
      bbl.statements.add(code);
      func.body = bbl;
    }

    {
      FuncPrivateVoid fEntry = makeEntryFunc(obj.topstate.initial.link);
      elem.function.add(fEntry);
      elem.entryFunc.link = fEntry;

      FuncPrivateVoid fExit = makeExitFunc(states, enumMap, stateVariable);
      elem.function.add(fExit);
      elem.exitFunc.link = fExit;
    }

    getMap().put(obj, elem);
    return elem;
  }

  private FuncPrivateVoid makeEntryFunc(State initial) {
    Block body = new Block(info);

    body.statements.add(makeCall(initial.entryFunc.link));

    FuncPrivateVoid rfunc = new FuncPrivateVoid(info, Designator.NAME_SEP + "stateentry", new EvlList<FuncVariable>(), new FuncReturnNone(ElementInfo.NO), body);
    return rfunc;
  }

  private Block makeErrorBb() {
    Block bberror = new Block(info);
    FuncCtrlOutDataOut trap = kll.getTrap();
    bberror.statements.add(new CallStmt(info, new Reference(info, trap, new RefCall(info, new TupleValue(info, new EvlList<Expression>())))));
    return bberror;
  }

  // TODO ok?
  private FuncPrivateVoid makeExitFunc(EnumType etype, HashMap<StateSimple, EnumElement> enumMap, StateVariable stateVariable) {

    EvlList<CaseOpt> option = new EvlList<CaseOpt>();
    for (State src : enumMap.keySet()) {
      Reference eref = makeEnumElemRef(etype, enumMap.get(src));
      Block obb = new Block(info);
      CaseOpt opt = makeCaseOption(eref, obb);
      obb.statements.add(makeCall(src.exitFunc.link)); // TODO ok?
      option.add(opt);
    }

    CaseStmt caseStmt = new CaseStmt(info, new Reference(info, stateVariable), option, makeErrorBb());

    Block body = new Block(info);
    body.statements.add(caseStmt);

    FuncPrivateVoid rfunc = new FuncPrivateVoid(info, "_exit", new EvlList<FuncVariable>(), new FuncReturnNone(ElementInfo.NO), body);
    rfunc.body = body;

    return rfunc;
  }

  static private CallStmt makeCall(Function func) {
    assert (func.param.isEmpty());
    Reference call = new Reference(info, func);
    call.offset.add(new RefCall(info, new TupleValue(info, new EvlList<Expression>())));

    return new CallStmt(info, call);
  }

  static private HashMap<StateSimple, EnumElement> makeEnumElem(StateComposite topstate, EnumType stateEnum) {
    HashMap<StateSimple, EnumElement> ret = new HashMap<StateSimple, EnumElement>();
    for (State state : topstate.item.getItems(State.class)) {
      assert (state instanceof StateSimple);

      EnumElement element = new EnumElement(info, state.name);
      stateEnum.getElement().add(element);

      ret.put((StateSimple) state, element);
    }
    return ret;
  }

  private CaseStmt addQueryCode(Collection<StateSimple> leafes, HashMap<StateSimple, EnumElement> enumMap, EnumType enumType, StateVariable stateVariable, FuncCtrlInDataOut func, EvlList<FuncVariable> param) {
    EvlList<CaseOpt> copt = new EvlList<CaseOpt>();

    for (State state : leafes) {

      FuncCtrlInDataOut query = getQuery(state, func.name);

      // from QueryDownPropagator
      assert (query.body.statements.size() == 1);
      ReturnExpr retcall = (ReturnExpr) query.body.statements.get(0);

      FsmReduction.relinkActualParameterRef(query.param, param, retcall.expr);

      Block stateBb = new Block(info);
      stateBb.statements.add(retcall);
      CaseOpt opt = makeCaseOption(makeEnumElemRef(enumType, enumMap.get(state)), stateBb);

      copt.add(opt);
    }

    CaseStmt caseStmt = new CaseStmt(info, new Reference(info, stateVariable), copt, makeErrorBb());

    return caseStmt;
  }

  static private FuncCtrlInDataOut getQuery(State state, String funcName) {
    assert (funcName != null);
    for (FuncCtrlInDataOut itr : state.item.getItems(FuncCtrlInDataOut.class)) {
      if (funcName.equals(itr.name)) {
        return itr;
      }
    }
    RError.err(ErrorType.Fatal, state.getInfo(), "Response not found: " + funcName);
    return null;
  }

  private CaseStmt addTransitionCode(Collection<StateSimple> leafes, HashMap<StateSimple, EnumElement> enumMap, EnumType enumType, StateVariable stateVariable, FuncCtrlInDataIn funcName, TransitionDict getter, EvlList<FuncVariable> param) {

    EvlList<CaseOpt> options = new EvlList<CaseOpt>();

    for (State src : leafes) {
      Block body = new Block(info);
      Reference label = makeEnumElemRef(enumType, enumMap.get(src));
      CaseOpt opt = makeCaseOption(label, body);

      EvlList<Transition> transList = getter.get(src, funcName);
      if (!transList.isEmpty()) {
        body.statements.add(makeGuardedTrans(transList, param, stateVariable, enumMap));
      }

      options.add(opt);
    }

    Block bberror = makeErrorBb();

    CaseStmt caseStmt = new CaseStmt(info, new Reference(info, stateVariable), options, bberror);

    return caseStmt;
  }

  static private IfStmt makeGuardedTrans(EvlList<Transition> transList, EvlList<FuncVariable> newparam, StateVariable stateVariable, HashMap<StateSimple, EnumElement> enumMap) {
    Block def = new Block(info);

    EvlList<IfOption> option = new EvlList<IfOption>();

    for (Transition trans : transList) {
      Block blockThen = makeTransition(trans, newparam, stateVariable, enumMap);

      FsmReduction.relinkActualParameterRef(trans.param, newparam, trans.guard); // relink references to
      // arguments to the
      // new ones

      IfOption ifo = new IfOption(trans.getInfo(), trans.guard, blockThen);
      option.add(ifo);
    }

    IfStmt entry = new IfStmt(info, option, def);
    return entry;
  }

  private static Reference makeEnumElemRef(EnumType type, EnumElement enumElement) {
    assert (type.getElement().contains(enumElement));
    EnumElement elem = enumElement;
    // EnumElement elem = type.find(enumElement);

    assert (elem != null);
    Reference eval = new Reference(info, elem);
    return eval;
  }

  private static CaseOpt makeCaseOption(Expression label, Block code) {
    EvlList<CaseOptEntry> list = new EvlList<CaseOptEntry>();
    list.add(new CaseOptValue(info, label));
    CaseOpt opt = new CaseOpt(info, list, code);
    return opt;
  }

  /**
   * Checks if transition is built like we expect and makes a transition bb out of it.
   */
  static private Block makeTransition(Transition trans, EvlList<FuncVariable> param, StateVariable stateVariable, HashMap<StateSimple, EnumElement> enumMap) {
    Block transCode = new Block(info);

    Block body = trans.body;
    FsmReduction.relinkActualParameterRef(trans.param, param, body);

    transCode.statements.addAll(body.statements);

    EnumElement src = enumMap.get(trans.dst.link);
    assert (src != null);
    Assignment setState = new AssignmentSingle(info, new Reference(info, stateVariable), new Reference(info, src));
    transCode.statements.add(setState);

    return transCode;
  }

  public Map<ImplHfsm, ImplElementary> getMap() {
    return map;
  }
}
