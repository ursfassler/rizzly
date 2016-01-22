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

import main.Configuration;
import ast.Designator;
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
import ast.data.expression.ReferenceExpression;
import ast.data.function.FuncRefFactory;
import ast.data.function.Function;
import ast.data.function.header.Procedure;
import ast.data.function.header.Response;
import ast.data.function.header.Signal;
import ast.data.function.header.Slot;
import ast.data.function.ret.FuncReturnNone;
import ast.data.reference.RefFactory;
import ast.data.reference.LinkedReferenceWithOffset_Implementation;
import ast.data.statement.Assignment;
import ast.data.statement.AssignmentSingle;
import ast.data.statement.Block;
import ast.data.statement.CallStmt;
import ast.data.statement.CaseOpt;
import ast.data.statement.CaseOptEntry;
import ast.data.statement.CaseOptValue;
import ast.data.statement.CaseStmt;
import ast.data.statement.ExpressionReturn;
import ast.data.statement.IfOption;
import ast.data.statement.IfStatement;
import ast.data.statement.Statement;
import ast.data.type.Type;
import ast.data.type.TypeRefFactory;
import ast.data.type.base.EnumElement;
import ast.data.type.base.EnumType;
import ast.data.type.base.EnumTypeFactory;
import ast.data.variable.FunctionVariable;
import ast.data.variable.StateVariable;
import ast.data.variable.Variable;
import ast.dispatcher.NullDispatcher;
import ast.knowledge.KnowLlvmLibrary;
import ast.knowledge.KnowledgeBase;
import ast.pass.AstPass;
import ast.repository.query.TypeFilter;
import error.ErrorType;
import error.RError;

public class FsmReduction extends AstPass {
  public FsmReduction(Configuration configuration) {
    super(configuration);
  }

  @Override
  public void process(Namespace ast, KnowledgeBase kb) {
    FsmReductionWorker reduction = new FsmReductionWorker(kb);
    reduction.traverse(ast, null);
    Relinker.relink(ast, reduction.getMap());
  }

  /**
   * relinking parameter references to new parameter
   */
  public static void relinkActualParameterRef(AstList<FunctionVariable> oldParam, AstList<FunctionVariable> newParam, Ast body) {
    assert (newParam.size() == oldParam.size());
    Map<Variable, Variable> map = new HashMap<Variable, Variable>();
    for (int i = 0; i < newParam.size(); i++) {
      map.put(oldParam.get(i), newParam.get(i));
    }
    Relinker.relink(body, map);
  }

}

class FsmReductionWorker extends NullDispatcher<Ast, Namespace> {
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
  final private KnowLlvmLibrary kll;
  final private Map<ImplHfsm, ImplElementary> map = new HashMap<ImplHfsm, ImplElementary>();

  public Reduction(KnowledgeBase kb) {
    kll = kb.getEntry(KnowLlvmLibrary.class);
  }

  public ImplElementary reduce(ImplHfsm obj, Namespace param) {
    ImplElementary elem = new ImplElementary(obj.getName(), FuncRefFactory.create(null), FuncRefFactory.create(null));
    elem.metadata().add(obj.metadata());
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
        RError.err(ErrorType.Fatal, "Unhandled StateItem: " + item.getClass().getCanonicalName(), item.metadata());
      }
    }

    EnumType states = EnumTypeFactory.create(obj.topstate.metadata(), obj.getName() + Designator.NAME_SEP + "State");
    HashMap<StateSimple, EnumElement> enumMap = makeEnumElem(obj.topstate, states);

    param.children.add(states);
    // String ena = (String)
    // enumMap.get(obj.getTopstate().getInitial()).properties().get(Property.NAME);
    EnumElement ena = enumMap.get(obj.topstate.initial.getTarget());
    LinkedReferenceWithOffset_Implementation initState = makeEnumElemRef(states, ena);
    StateVariable stateVariable = new StateVariable("_statevar", TypeRefFactory.create(states), new ReferenceExpression(initState));
    stateVariable.metadata().add(obj.metadata());
    elem.variable.add(stateVariable);

    TransitionDict dict = new TransitionDict();
    dict.traverse(obj.topstate, null);

    // create event handler
    for (Response func : TypeFilter.select(elem.iface, Response.class)) {
      assert (func.body.statements.isEmpty());
      Statement code = addQueryCode(enumMap.keySet(), enumMap, states, stateVariable, func, func.param);
      Block bbl = new Block();
      bbl.statements.add(code);
      func.body = bbl;
    }

    for (Slot func : TypeFilter.select(elem.iface, Slot.class)) {
      assert (func.body.statements.isEmpty());
      Statement code = addTransitionCode(enumMap.keySet(), enumMap, states, stateVariable, func, dict, func.param);
      Block bbl = new Block();
      bbl.statements.add(code);
      func.body = bbl;
    }

    {
      Procedure fEntry = makeEntryFunc(obj.topstate.initial.getTarget());
      elem.function.add(fEntry);
      elem.entryFunc = FuncRefFactory.create(fEntry.metadata(), fEntry);

      Procedure fExit = makeExitFunc(states, enumMap, stateVariable);
      elem.function.add(fExit);
      elem.exitFunc = FuncRefFactory.create(fExit.metadata(), fExit);
    }

    getMap().put(obj, elem);
    return elem;
  }

  private Procedure makeEntryFunc(State initial) {
    Block body = new Block();

    body.statements.add(makeCall(initial.entryFunc.getTarget()));

    Procedure rfunc = new Procedure(Designator.NAME_SEP + "stateentry", new AstList<FunctionVariable>(), new FuncReturnNone(), body);
    return rfunc;
  }

  private Block makeErrorBb() {
    Block bberror = new Block();
    Signal trap = kll.getTrap();
    bberror.statements.add(new CallStmt(RefFactory.call(trap)));
    return bberror;
  }

  private Procedure makeExitFunc(EnumType etype, HashMap<StateSimple, EnumElement> enumMap, StateVariable stateVariable) {

    AstList<CaseOpt> option = new AstList<CaseOpt>();
    for (State src : enumMap.keySet()) {
      LinkedReferenceWithOffset_Implementation eref = makeEnumElemRef(etype, enumMap.get(src));
      Block obb = new Block();
      CaseOpt opt = makeCaseOption(eref, obb);
      obb.statements.add(makeCall(src.exitFunc.getTarget()));
      option.add(opt);
    }

    CaseStmt caseStmt = new CaseStmt(new ReferenceExpression(RefFactory.full(stateVariable)), option, makeErrorBb());

    Block body = new Block();
    body.statements.add(caseStmt);

    Procedure rfunc = new Procedure("_exit", new AstList<FunctionVariable>(), new FuncReturnNone(), body);
    rfunc.body = body;

    return rfunc;
  }

  static private CallStmt makeCall(Function func) {
    assert (func.param.isEmpty());
    LinkedReferenceWithOffset_Implementation call = RefFactory.call(func);
    return new CallStmt(call);
  }

  static private HashMap<StateSimple, EnumElement> makeEnumElem(StateComposite topstate, EnumType stateEnum) {
    HashMap<StateSimple, EnumElement> ret = new HashMap<StateSimple, EnumElement>();
    for (State state : TypeFilter.select(topstate.item, State.class)) {
      assert (state instanceof StateSimple);

      EnumElement element = new EnumElement(state.getName());
      stateEnum.element.add(element);

      ret.put((StateSimple) state, element);
    }
    return ret;
  }

  private CaseStmt addQueryCode(Collection<StateSimple> leafes, HashMap<StateSimple, EnumElement> enumMap, EnumType enumType, StateVariable stateVariable, Response func, AstList<FunctionVariable> param) {
    AstList<CaseOpt> copt = new AstList<CaseOpt>();

    for (State state : leafes) {

      Response query = getQuery(state, func.getName());

      // from QueryDownPropagator
      assert (query.body.statements.size() == 1);
      ExpressionReturn retcall = (ExpressionReturn) query.body.statements.get(0);

      FsmReduction.relinkActualParameterRef(query.param, param, retcall.expression);

      Block stateBb = new Block();
      stateBb.statements.add(retcall);
      CaseOpt opt = makeCaseOption(makeEnumElemRef(enumType, enumMap.get(state)), stateBb);

      copt.add(opt);
    }

    CaseStmt caseStmt = new CaseStmt(new ReferenceExpression(RefFactory.full(stateVariable)), copt, makeErrorBb());

    return caseStmt;
  }

  static private Response getQuery(State state, String funcName) {
    assert (funcName != null);
    for (Response itr : TypeFilter.select(state.item, Response.class)) {
      if (funcName.equals(itr.getName())) {
        return itr;
      }
    }
    RError.err(ErrorType.Fatal, "Response not found: " + funcName, state.metadata());
    return null;
  }

  private CaseStmt addTransitionCode(Collection<StateSimple> leafes, HashMap<StateSimple, EnumElement> enumMap, EnumType enumType, StateVariable stateVariable, Slot funcName, TransitionDict getter, AstList<FunctionVariable> param) {

    AstList<CaseOpt> options = new AstList<CaseOpt>();

    for (State src : leafes) {
      Block body = new Block();
      LinkedReferenceWithOffset_Implementation label = makeEnumElemRef(enumType, enumMap.get(src));
      CaseOpt opt = makeCaseOption(label, body);

      AstList<Transition> transList = getter.get(src, funcName);
      if (!transList.isEmpty()) {
        body.statements.add(makeGuardedTrans(transList, param, stateVariable, enumMap));
      }

      options.add(opt);
    }

    Block bberror = makeErrorBb();

    CaseStmt caseStmt = new CaseStmt(new ReferenceExpression(RefFactory.full(stateVariable)), options, bberror);

    return caseStmt;
  }

  static private IfStatement makeGuardedTrans(AstList<Transition> transList, AstList<FunctionVariable> newparam, StateVariable stateVariable, HashMap<StateSimple, EnumElement> enumMap) {
    Block def = new Block();

    AstList<IfOption> option = new AstList<IfOption>();

    for (Transition trans : transList) {
      Block blockThen = makeTransition(trans, newparam, stateVariable, enumMap);

      FsmReduction.relinkActualParameterRef(trans.param, newparam, trans.guard); // relink
      // references
      // to
      // arguments to the
      // new ones

      IfOption ifo = new IfOption(trans.guard, blockThen);
      ifo.metadata().add(trans.metadata());
      option.add(ifo);
    }

    IfStatement entry = new IfStatement(option, def);
    return entry;
  }

  private static LinkedReferenceWithOffset_Implementation makeEnumElemRef(EnumType type, EnumElement enumElement) {
    assert (type.element.contains(enumElement));
    EnumElement elem = enumElement;
    // EnumElement elem = type.find(enumElement);

    assert (elem != null);
    LinkedReferenceWithOffset_Implementation eval = RefFactory.full(elem);
    return eval;
  }

  private static CaseOpt makeCaseOption(LinkedReferenceWithOffset_Implementation label, Block code) {
    AstList<CaseOptEntry> list = new AstList<CaseOptEntry>();
    list.add(new CaseOptValue(new ReferenceExpression(label)));
    CaseOpt opt = new CaseOpt(list, code);
    return opt;
  }

  /**
   * Checks if transition is built like we expect and makes a transition bb out of it.
   */
  static private Block makeTransition(Transition trans, AstList<FunctionVariable> param, StateVariable stateVariable, HashMap<StateSimple, EnumElement> enumMap) {
    Block transCode = new Block();

    Block body = trans.body;
    FsmReduction.relinkActualParameterRef(trans.param, param, body);

    transCode.statements.addAll(body.statements);

    EnumElement src = enumMap.get(trans.dst.getTarget());
    assert (src != null);
    Assignment setState = new AssignmentSingle(RefFactory.full(stateVariable), new ReferenceExpression(RefFactory.full(src)));
    transCode.statements.add(setState);

    return transCode;
  }

  public Map<ImplHfsm, ImplElementary> getMap() {
    return map;
  }
}
