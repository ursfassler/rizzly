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

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import ast.Designator;
import ast.ElementInfo;
import ast.copy.Relinker;
import ast.data.Ast;
import ast.data.AstList;
import ast.data.Named;
import ast.data.Namespace;
import ast.data.component.elementary.ImplElementary;
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
import ast.data.function.header.FuncResponse;
import ast.data.function.header.FuncSignal;
import ast.data.function.header.FuncSlot;
import ast.data.function.ret.FuncReturnNone;
import ast.data.statement.Assignment;
import ast.data.statement.AssignmentSingle;
import ast.data.statement.Block;
import ast.data.statement.CallStmt;
import ast.data.statement.CaseOpt;
import ast.data.statement.CaseOptEntry;
import ast.data.statement.CaseOptValue;
import ast.data.statement.CaseStmt;
import ast.data.statement.IfOption;
import ast.data.statement.IfStmt;
import ast.data.statement.ReturnExpr;
import ast.data.statement.Statement;
import ast.data.type.Type;
import ast.data.type.base.EnumElement;
import ast.data.type.base.EnumType;
import ast.data.variable.FuncVariable;
import ast.data.variable.StateVariable;
import ast.data.variable.Variable;
import ast.knowledge.KnowBaseItem;
import ast.knowledge.KnowLlvmLibrary;
import ast.knowledge.KnowledgeBase;
import ast.pass.AstPass;
import ast.specification.TypeFilter;
import ast.traverser.NullTraverser;
import error.ErrorType;
import error.RError;

public class FsmReduction extends AstPass {

  @Override
  public void process(Namespace ast, KnowledgeBase kb) {
    FsmReductionWorker reduction = new FsmReductionWorker(kb);
    reduction.traverse(ast, null);
    Relinker.relink(ast, reduction.getMap());
  }

  /**
   * relinking parameter references to new parameter
   */
  public static void relinkActualParameterRef(AstList<FuncVariable> oldParam, AstList<FuncVariable> newParam, Ast body) {
    assert (newParam.size() == oldParam.size());
    Map<Variable, Variable> map = new HashMap<Variable, Variable>();
    for (int i = 0; i < newParam.size(); i++) {
      map.put(oldParam.get(i), newParam.get(i));
    }
    Relinker.relink(body, map);
  }

}

class FsmReductionWorker extends NullTraverser<Ast, Namespace> {
  final private Reduction reduction;

  public FsmReductionWorker(KnowledgeBase kb) {
    super();
    reduction = new Reduction(kb);
  }

  public Map<? extends Named, ? extends Named> getMap() {
    return reduction.getMap();
  }

  @Override
  protected Ast visitDefault(Ast obj, Namespace param) {
    return obj;
  }

  @Override
  protected Ast visitNamespace(Namespace obj, Namespace param) {
    AstList<Ast> list = obj.children;
    for (int i = 0; i < list.size(); i++) {
      Ast item = list.get(i);
      item = visit(item, obj);
      assert (item != null);
      list.set(i, item);
    }
    return obj;
  }

  @Override
  protected Ast visitImplHfsm(ImplHfsm obj, Namespace param) {
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
    ImplElementary elem = new ImplElementary(obj.getInfo(), obj.name, new SimpleRef<FuncProcedure>(info, null), new SimpleRef<FuncProcedure>(info, null));
    elem.iface.addAll(obj.iface);
    elem.function.addAll(obj.function);
    for (StateContent item : obj.topstate.item) {
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
    // String ena = (String)
    // enumMap.get(obj.getTopstate().getInitial()).properties().get(Property.NAME);
    EnumElement ena = enumMap.get(obj.topstate.initial.getTarget());
    Reference initState = makeEnumElemRef(states, ena);
    StateVariable stateVariable = new StateVariable(obj.getInfo(), "_statevar", new SimpleRef<Type>(info, states), initState);
    elem.variable.add(stateVariable);

    TransitionDict dict = new TransitionDict();
    dict.traverse(obj.topstate, null);

    // create event handler
    for (FuncResponse func : TypeFilter.select(elem.iface, FuncResponse.class)) {
      assert (func.body.statements.isEmpty());
      Statement code = addQueryCode(enumMap.keySet(), enumMap, states, stateVariable, func, func.param);
      Block bbl = new Block(info);
      bbl.statements.add(code);
      func.body = bbl;
    }

    for (FuncSlot func : TypeFilter.select(elem.iface, FuncSlot.class)) {
      assert (func.body.statements.isEmpty());
      Statement code = addTransitionCode(enumMap.keySet(), enumMap, states, stateVariable, func, dict, func.param);
      Block bbl = new Block(info);
      bbl.statements.add(code);
      func.body = bbl;
    }

    {
      FuncProcedure fEntry = makeEntryFunc((State) obj.topstate.initial.getTarget());
      elem.function.add(fEntry);
      elem.entryFunc.link = fEntry;

      FuncProcedure fExit = makeExitFunc(states, enumMap, stateVariable);
      elem.function.add(fExit);
      elem.exitFunc.link = fExit;
    }

    getMap().put(obj, elem);
    return elem;
  }

  private FuncProcedure makeEntryFunc(State initial) {
    Block body = new Block(info);

    body.statements.add(makeCall(initial.entryFunc.link));

    FuncProcedure rfunc = new FuncProcedure(info, Designator.NAME_SEP + "stateentry", new AstList<FuncVariable>(), new FuncReturnNone(ElementInfo.NO), body);
    return rfunc;
  }

  private Block makeErrorBb() {
    Block bberror = new Block(info);
    FuncSignal trap = kll.getTrap();
    bberror.statements.add(new CallStmt(info, new Reference(info, trap, new RefCall(info, new TupleValue(info, new AstList<Expression>())))));
    return bberror;
  }

  private FuncProcedure makeExitFunc(EnumType etype, HashMap<StateSimple, EnumElement> enumMap, StateVariable stateVariable) {

    AstList<CaseOpt> option = new AstList<CaseOpt>();
    for (State src : enumMap.keySet()) {
      Reference eref = makeEnumElemRef(etype, enumMap.get(src));
      Block obb = new Block(info);
      CaseOpt opt = makeCaseOption(eref, obb);
      obb.statements.add(makeCall(src.exitFunc.link));
      option.add(opt);
    }

    CaseStmt caseStmt = new CaseStmt(info, new Reference(info, stateVariable), option, makeErrorBb());

    Block body = new Block(info);
    body.statements.add(caseStmt);

    FuncProcedure rfunc = new FuncProcedure(info, "_exit", new AstList<FuncVariable>(), new FuncReturnNone(ElementInfo.NO), body);
    rfunc.body = body;

    return rfunc;
  }

  static private CallStmt makeCall(Function func) {
    assert (func.param.isEmpty());
    Reference call = new Reference(info, func);
    call.offset.add(new RefCall(info, new TupleValue(info, new AstList<Expression>())));

    return new CallStmt(info, call);
  }

  static private HashMap<StateSimple, EnumElement> makeEnumElem(StateComposite topstate, EnumType stateEnum) {
    HashMap<StateSimple, EnumElement> ret = new HashMap<StateSimple, EnumElement>();
    for (State state : TypeFilter.select(topstate.item, State.class)) {
      assert (state instanceof StateSimple);

      EnumElement element = new EnumElement(info, state.name);
      stateEnum.element.add(element);

      ret.put((StateSimple) state, element);
    }
    return ret;
  }

  private CaseStmt addQueryCode(Collection<StateSimple> leafes, HashMap<StateSimple, EnumElement> enumMap, EnumType enumType, StateVariable stateVariable, FuncResponse func, AstList<FuncVariable> param) {
    AstList<CaseOpt> copt = new AstList<CaseOpt>();

    for (State state : leafes) {

      FuncResponse query = getQuery(state, func.name);

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

  static private FuncResponse getQuery(State state, String funcName) {
    assert (funcName != null);
    for (FuncResponse itr : TypeFilter.select(state.item, FuncResponse.class)) {
      if (funcName.equals(itr.name)) {
        return itr;
      }
    }
    RError.err(ErrorType.Fatal, state.getInfo(), "Response not found: " + funcName);
    return null;
  }

  private CaseStmt addTransitionCode(Collection<StateSimple> leafes, HashMap<StateSimple, EnumElement> enumMap, EnumType enumType, StateVariable stateVariable, FuncSlot funcName, TransitionDict getter, AstList<FuncVariable> param) {

    AstList<CaseOpt> options = new AstList<CaseOpt>();

    for (State src : leafes) {
      Block body = new Block(info);
      Reference label = makeEnumElemRef(enumType, enumMap.get(src));
      CaseOpt opt = makeCaseOption(label, body);

      AstList<Transition> transList = getter.get(src, funcName);
      if (!transList.isEmpty()) {
        body.statements.add(makeGuardedTrans(transList, param, stateVariable, enumMap));
      }

      options.add(opt);
    }

    Block bberror = makeErrorBb();

    CaseStmt caseStmt = new CaseStmt(info, new Reference(info, stateVariable), options, bberror);

    return caseStmt;
  }

  static private IfStmt makeGuardedTrans(AstList<Transition> transList, AstList<FuncVariable> newparam, StateVariable stateVariable, HashMap<StateSimple, EnumElement> enumMap) {
    Block def = new Block(info);

    AstList<IfOption> option = new AstList<IfOption>();

    for (Transition trans : transList) {
      Block blockThen = makeTransition(trans, newparam, stateVariable, enumMap);

      FsmReduction.relinkActualParameterRef(trans.param, newparam, trans.guard); // relink
      // references
      // to
      // arguments to the
      // new ones

      IfOption ifo = new IfOption(trans.getInfo(), trans.guard, blockThen);
      option.add(ifo);
    }

    IfStmt entry = new IfStmt(info, option, def);
    return entry;
  }

  private static Reference makeEnumElemRef(EnumType type, EnumElement enumElement) {
    assert (type.element.contains(enumElement));
    EnumElement elem = enumElement;
    // EnumElement elem = type.find(enumElement);

    assert (elem != null);
    Reference eval = new Reference(info, elem);
    return eval;
  }

  private static CaseOpt makeCaseOption(Expression label, Block code) {
    AstList<CaseOptEntry> list = new AstList<CaseOptEntry>();
    list.add(new CaseOptValue(info, label));
    CaseOpt opt = new CaseOpt(info, list, code);
    return opt;
  }

  /**
   * Checks if transition is built like we expect and makes a transition bb out of it.
   */
  static private Block makeTransition(Transition trans, AstList<FuncVariable> param, StateVariable stateVariable, HashMap<StateSimple, EnumElement> enumMap) {
    Block transCode = new Block(info);

    Block body = trans.body;
    FsmReduction.relinkActualParameterRef(trans.param, param, body);

    transCode.statements.addAll(body.statements);

    EnumElement src = enumMap.get(trans.dst.getTarget());
    assert (src != null);
    Assignment setState = new AssignmentSingle(info, new Reference(info, stateVariable), new Reference(info, src));
    transCode.statements.add(setState);

    return transCode;
  }

  public Map<ImplHfsm, ImplElementary> getMap() {
    return map;
  }
}
