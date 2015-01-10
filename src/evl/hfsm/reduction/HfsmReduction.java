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

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import operation.EvlOperation;

import common.Designator;
import common.ElementInfo;

import error.ErrorType;
import error.RError;
import evl.Evl;
import evl.NullTraverser;
import evl.copy.Relinker;
import evl.expression.Expression;
import evl.expression.reference.RefCall;
import evl.expression.reference.Reference;
import evl.expression.reference.SimpleRef;
import evl.function.Function;
import evl.function.header.FuncCtrlInDataIn;
import evl.function.header.FuncCtrlInDataOut;
import evl.function.header.FuncCtrlOutDataOut;
import evl.function.header.FuncPrivateVoid;
import evl.hfsm.ImplHfsm;
import evl.hfsm.State;
import evl.hfsm.StateComposite;
import evl.hfsm.StateItem;
import evl.hfsm.StateSimple;
import evl.hfsm.Transition;
import evl.knowledge.KnowBaseItem;
import evl.knowledge.KnowLlvmLibrary;
import evl.knowledge.KnowledgeBase;
import evl.other.EvlList;
import evl.other.ImplElementary;
import evl.other.Namespace;
import evl.statement.Assignment;
import evl.statement.Block;
import evl.statement.CallStmt;
import evl.statement.CaseOpt;
import evl.statement.CaseOptEntry;
import evl.statement.CaseOptValue;
import evl.statement.CaseStmt;
import evl.statement.IfOption;
import evl.statement.IfStmt;
import evl.statement.ReturnExpr;
import evl.statement.Statement;
import evl.type.Type;
import evl.type.base.EnumElement;
import evl.type.base.EnumType;
import evl.variable.FuncVariable;
import evl.variable.StateVariable;
import evl.variable.Variable;

public class HfsmReduction extends EvlPass {

  {
    addDependency(HfsmToFsm.class);
  }

  @Override
  public void process(Evl evl, KnowledgeBase kb) {
    HfsmReductionWorker reduction = new HfsmReductionWorker(kb);
    reduction.traverse(evl, null);
    Relinker.relink(evl, reduction.getMap());
    // Linker.process(classes, knowledgeBase);

    // TODO reimplement
    // if (map.containsKey(root)) {
    // root = map.get(root);
    // }
    // return root;
  }

}

class HfsmReductionWorker extends NullTraverser<Evl, Namespace> {

  static final private ElementInfo info = ElementInfo.NO;
  final private KnowLlvmLibrary kll;
  final private KnowBaseItem kbi;
  final private Map<ImplHfsm, ImplElementary> map = new HashMap<ImplHfsm, ImplElementary>();

  public HfsmReductionWorker(KnowledgeBase kb) {
    kll = kb.getEntry(KnowLlvmLibrary.class);
    kbi = kb.getEntry(KnowBaseItem.class);
  }

  @Override
  protected Evl visitDefault(Evl obj, Namespace param) {
    return obj;
  }

  @Override
  protected Evl visitNamespace(Namespace obj, Namespace param) {
    EvlList<Evl> list = new EvlList<Evl>(obj.getChildren());
    obj.getChildren().clear();
    for (int i = 0; i < list.size(); i++) {
      Evl item = list.get(i);
      item = visit(item, obj);
      assert (item != null);
      obj.add(item);
    }
    return obj;
  }

  @Override
  protected Evl visitImplHfsm(ImplHfsm obj, Namespace param) {
    ImplElementary elem = new ImplElementary(obj.getInfo(), obj.getName(), new SimpleRef<FuncPrivateVoid>(info, null), new SimpleRef<FuncPrivateVoid>(info, null));
    elem.getIface().addAll(obj.getIface());
    elem.getFunction().addAll(obj.getFunction());
    for (StateItem item : obj.getTopstate().getItem()) {
      if (item instanceof Variable) {
        elem.getVariable().add((Variable) item);
      } else if (item instanceof Function) {
        elem.getFunction().add((Function) item);
      } else if (item instanceof Transition) {
      } else if (item instanceof StateSimple) {
      } else {
        RError.err(ErrorType.Fatal, item.getInfo(), "Unhandled StateItem: " + item.getClass().getCanonicalName());
      }
    }

    EnumType states = new EnumType(obj.getTopstate().getInfo(), obj.getName() + Designator.NAME_SEP + "State");
    HashMap<StateSimple, EnumElement> enumMap = makeEnumElem(obj.getTopstate(), states);

    param.add(states);
    // String ena = (String) enumMap.get(obj.getTopstate().getInitial()).properties().get(Property.NAME);
    EnumElement ena = enumMap.get(obj.getTopstate().getInitial().getLink());
    Reference initState = makeEnumElemRef(states, ena);
    StateVariable stateVariable = new StateVariable(obj.getInfo(), "_statevar", new SimpleRef<Type>(info, states), initState);
    elem.getVariable().add(stateVariable);

    TransitionDict dict = new TransitionDict();
    dict.traverse(obj.getTopstate(), null);

    // create event handler
    for (FuncCtrlInDataOut func : elem.getIface().getItems(FuncCtrlInDataOut.class)) {
      assert (func.getBody().getStatements().isEmpty());
      Statement code = addQueryCode(enumMap.keySet(), enumMap, states, stateVariable, func, func.getParam());
      Block bbl = new Block(info);
      bbl.getStatements().add(code);
      func.setBody(bbl);
    }

    for (FuncCtrlInDataIn func : elem.getIface().getItems(FuncCtrlInDataIn.class)) {
      assert (func.getBody().getStatements().isEmpty());
      Statement code = addTransitionCode(enumMap.keySet(), enumMap, states, stateVariable, func, dict, func.getParam());
      Block bbl = new Block(info);
      bbl.getStatements().add(code);
      func.setBody(bbl);
    }

    {
      FuncPrivateVoid fEntry = makeEntryFunc(obj.getTopstate().getInitial().getLink());
      elem.getFunction().add(fEntry);
      elem.getEntryFunc().setLink(fEntry);

      FuncPrivateVoid fExit = makeExitFunc(states, enumMap, stateVariable);
      elem.getFunction().add(fExit);
      elem.getExitFunc().setLink(fExit);
    }

    getMap().put(obj, elem);
    return elem;
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

  private FuncPrivateVoid makeEntryFunc(State initial) {
    Block body = new Block(info);

    body.getStatements().add(makeCall(initial.getEntryFunc().getLink()));

    FuncPrivateVoid rfunc = new FuncPrivateVoid(info, Designator.NAME_SEP + "stateentry", new EvlList<FuncVariable>(), new SimpleRef<Type>(ElementInfo.NO, kbi.getVoidType()), body);
    return rfunc;
  }

  private Block makeErrorBb() {
    Block bberror = new Block(info);
    FuncCtrlOutDataOut trap = kll.getTrap();
    bberror.getStatements().add(new CallStmt(info, new Reference(info, trap, new RefCall(info, new EvlList<Expression>()))));
    return bberror;
  }

  // TODO ok?
  private FuncPrivateVoid makeExitFunc(EnumType etype, HashMap<StateSimple, EnumElement> enumMap, StateVariable stateVariable) {

    EvlList<CaseOpt> option = new EvlList<CaseOpt>();
    for (State src : enumMap.keySet()) {
      Reference eref = makeEnumElemRef(etype, enumMap.get(src));
      Block obb = new Block(info);
      CaseOpt opt = makeCaseOption(eref, obb);
      obb.getStatements().add(makeCall(src.getExitFunc().getLink())); // TODO ok?
      option.add(opt);
    }

    CaseStmt caseStmt = new CaseStmt(info, new Reference(info, stateVariable), option, makeErrorBb());

    Block body = new Block(info);
    body.getStatements().add(caseStmt);

    FuncPrivateVoid rfunc = new FuncPrivateVoid(info, "_exit", new EvlList<FuncVariable>(), new SimpleRef<Type>(ElementInfo.NO, kbi.getVoidType()), body);
    rfunc.setBody(body);

    return rfunc;
  }

  static private CallStmt makeCall(Function func) {
    assert (func.getParam().isEmpty());
    Reference call = new Reference(info, func);
    call.getOffset().add(new RefCall(info, new EvlList<Expression>()));

    return new CallStmt(info, call);
  }

  static private HashMap<StateSimple, EnumElement> makeEnumElem(StateComposite topstate, EnumType stateEnum) {
    HashMap<StateSimple, EnumElement> ret = new HashMap<StateSimple, EnumElement>();
    for (State state : topstate.getItem().getItems(State.class)) {
      assert (state instanceof StateSimple);

      EnumElement element = new EnumElement(info, state.getName());
      stateEnum.getElement().add(element);

      ret.put((StateSimple) state, element);
    }
    return ret;
  }

  private CaseStmt addQueryCode(Collection<StateSimple> leafes, HashMap<StateSimple, EnumElement> enumMap, EnumType enumType, StateVariable stateVariable, FuncCtrlInDataOut func, EvlList<FuncVariable> param) {
    EvlList<CaseOpt> copt = new EvlList<CaseOpt>();

    for (State state : leafes) {

      FuncCtrlInDataOut query = getQuery(state, func.getName());

      // from QueryDownPropagator
      assert (query.getBody().getStatements().size() == 1);
      ReturnExpr retcall = (ReturnExpr) query.getBody().getStatements().get(0);

      relinkActualParameterRef(query.getParam(), param, retcall.getExpr());

      Block stateBb = new Block(info);
      stateBb.getStatements().add(retcall);
      CaseOpt opt = makeCaseOption(makeEnumElemRef(enumType, enumMap.get(state)), stateBb);

      copt.add(opt);
    }

    CaseStmt caseStmt = new CaseStmt(info, new Reference(info, stateVariable), copt, makeErrorBb());

    return caseStmt;
  }

  static private FuncCtrlInDataOut getQuery(State state, String funcName) {
    assert (funcName != null);
    for (FuncCtrlInDataOut itr : state.getItem().getItems(FuncCtrlInDataOut.class)) {
      if (funcName.equals(itr.getName())) {
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
        body.getStatements().add(makeGuardedTrans(transList, param, stateVariable, enumMap));
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

      relinkActualParameterRef(trans.getParam(), newparam, trans.getGuard()); // relink references to arguments to the
      // new ones

      IfOption ifo = new IfOption(trans.getInfo(), trans.getGuard(), blockThen);
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

    Block body = trans.getBody();
    relinkActualParameterRef(trans.getParam(), param, body);

    transCode.getStatements().addAll(body.getStatements());

    EnumElement src = enumMap.get(trans.getDst().getLink());
    assert (src != null);
    Assignment setState = new Assignment(info, new Reference(info, stateVariable), new Reference(info, src));
    transCode.getStatements().add(setState);

    return transCode;
  }

  public Map<ImplHfsm, ImplElementary> getMap() {
    return map;
  }
}
