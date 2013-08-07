package evl.hfsm.reduction;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import common.Designator;
import common.Direction;
import common.ElementInfo;

import error.ErrorType;
import error.RError;
import evl.Evl;
import evl.NullTraverser;
import evl.cfg.CaseOptEntry;
import evl.cfg.CaseOptValue;
import evl.cfg.ReturnExpr;
import evl.copy.Copy;
import evl.copy.Relinker;
import evl.expression.Expression;
import evl.expression.reference.RefCall;
import evl.expression.reference.RefItem;
import evl.expression.reference.Reference;
import evl.function.FuncWithBody;
import evl.function.FuncWithReturn;
import evl.function.FunctionBase;
import evl.function.impl.FuncInputHandlerEvent;
import evl.function.impl.FuncInputHandlerQuery;
import evl.function.impl.FuncPrivateVoid;
import evl.hfsm.HfsmQueryFunction;
import evl.hfsm.ImplHfsm;
import evl.hfsm.QueryItem;
import evl.hfsm.State;
import evl.hfsm.StateComposite;
import evl.hfsm.StateSimple;
import evl.hfsm.Transition;
import evl.knowledge.KnowBaseItem;
import evl.knowledge.KnowledgeBase;
import evl.other.IfaceUse;
import evl.other.ImplElementary;
import evl.other.Interface;
import evl.other.ListOfNamed;
import evl.other.Named;
import evl.other.Namespace;
import evl.statement.Assignment;
import evl.statement.Block;
import evl.statement.CallStmt;
import evl.statement.CaseOpt;
import evl.statement.CaseStmt;
import evl.statement.IfOption;
import evl.statement.IfStmt;
import evl.statement.Statement;
import evl.type.base.EnumElement;
import evl.type.base.EnumType;
import evl.variable.FuncVariable;
import evl.variable.StateVariable;

//TODO in here is the problem, that _fentry functions from deeper states are not moved upwards

public class HfsmReduction extends NullTraverser<Named, Namespace> {
  private ElementInfo info = new ElementInfo();
  private KnowBaseItem kbi;
  private Map<ImplHfsm, ImplElementary> map = new HashMap<ImplHfsm, ImplElementary>();

  public HfsmReduction(KnowledgeBase kb) {
    this.kbi = kb.getEntry(KnowBaseItem.class);
  }

  public static Map<ImplHfsm, ImplElementary> process(Namespace classes, KnowledgeBase kb) {
    HfsmReduction reduction = new HfsmReduction(kb);
    reduction.visitItr(classes, null);
    return reduction.map;
  }

  @Override
  protected Named visitDefault(Evl obj, Namespace param) {
    assert (obj instanceof Named);
    return (Named) obj;
  }

  @Override
  protected Named visitNamespace(Namespace obj, Namespace param) {
    for (int i = 0; i < obj.getList().size(); i++) {
      Named item = obj.getList().get(i);
      item = visit(item, obj);
      assert (item != null);
      obj.getList().set(i, item);
    }
    return obj;
  }

  @Override
  protected Named visitImplHfsm(ImplHfsm obj, Namespace param) {
    ImplElementary elem = new ImplElementary(obj.getInfo(), obj.getName());
    elem.getIface(Direction.in).addAll(obj.getIface(Direction.in));
    elem.getIface(Direction.out).addAll(obj.getIface(Direction.out));
    elem.getVariable().addAll(obj.getTopstate().getVariable());
    elem.getInternalFunction().addAll(obj.getTopstate().getFunction());

    elem.setEntryFunc(makeEntryExitFunc(fun.hfsm.State.ENTRY_FUNC_NAME, elem.getInternalFunction()));
    elem.setExitFunc(makeEntryExitFunc(fun.hfsm.State.EXIT_FUNC_NAME, elem.getInternalFunction()));

    EnumType states = new EnumType(obj.getTopstate().getInfo(), obj.getName() + Designator.NAME_SEP + "State");
    HashMap<StateSimple, EnumElement> enumMap = makeEnumElem(obj.getTopstate());
    states.getElement().addAll(enumMap.values());

    param.add(states);
    StateVariable stateVariable = new StateVariable(obj.getInfo(), "state", states);
    elem.getVariable().add(stateVariable);

    TransitionDict dict = new TransitionDict();
    dict.traverse(obj.getTopstate(), null);

    // create event handler
    for (IfaceUse use : elem.getIface(Direction.in)) {
      Interface iface = use.getLink();
      List<String> ns = new ArrayList<String>();
      ns.add(use.getName());
      for (FunctionBase header : iface.getPrototype()) {
        FunctionBase ptoto = Copy.copy(header);
        FunctionBase func;
        if (ptoto instanceof FuncWithReturn) {
          func = new FuncInputHandlerQuery(obj.getInfo(), header.getName(), ptoto.getParam());
          ((FuncWithReturn) func).setRet(((FuncWithReturn) ptoto).getRet());
        } else {
          func = new FuncInputHandlerEvent(obj.getInfo(), header.getName(), ptoto.getParam());
        }
        ((FuncWithBody) func).setBody(new Block(new ElementInfo()));
        elem.addFunction(ns, func);

        if (func instanceof FuncWithReturn) {
          List<HfsmQueryFunction> queryList = addQueryCode(obj, enumMap.keySet(), enumMap, stateVariable, use.getName(), (FuncInputHandlerQuery) func);
          elem.getInternalFunction().addAll(queryList);
        } else {
          Statement caseStmt = addTransitionCode(obj, enumMap.keySet(), enumMap, stateVariable, use.getName(), func.getName(), dict, func.getParam().getList());
          ((FuncWithBody) func).getBody().getStatements().add(caseStmt);
        }
      }
    }

    // create constructor and destructor
    {
      Interface type = kbi.get(Interface.class, SystemIfaceAdder.IFACE_TYPE_NAME);
      IfaceUse debIface = new IfaceUse(info, SystemIfaceAdder.IFACE_USE_NAME, type);
      elem.getIface(Direction.in).add(debIface);

      List<String> ns = new ArrayList<String>();
      ns.add(SystemIfaceAdder.IFACE_USE_NAME);
      elem.addFunction(ns, makeConstructor(obj.getTopstate().getInitial(), enumMap, stateVariable));
      elem.addFunction(ns, makeDestructor(enumMap, stateVariable));

    }

    map.put(obj, elem);
    return elem;
  }

  private Reference makeEntryExitFunc(String name, ListOfNamed<FunctionBase> list) {
    ElementInfo info = new ElementInfo();
    FuncPrivateVoid func = new FuncPrivateVoid(info, name, new ListOfNamed<FuncVariable>());
    func.setBody(new Block(info));
    list.add(func);
    return new Reference(info, func);
  }

  // TODO make new entry function out of the following code; maybe add constructor and destructor after reudction to
  // elementary
  private FuncInputHandlerEvent makeConstructor(State initial, HashMap<StateSimple, EnumElement> enumMap, StateVariable stateVariable) {
    Block body = new Block(info);

    // copy entry code
    body.getStatements().add(makeCall(initial.getEntryFunc()));
    // set initial state
    Assignment newState = makeNewStateCode(enumMap, stateVariable, initial);
    body.getStatements().add(newState);

    FuncInputHandlerEvent rfunc = new FuncInputHandlerEvent(info, SystemIfaceAdder.CONSTRUCT, new ListOfNamed<FuncVariable>());
    rfunc.setBody(body);

    return rfunc;
  }

  private FuncInputHandlerEvent makeDestructor(HashMap<StateSimple, EnumElement> enumMap, StateVariable stateVariable) {
    Block body = new Block(info);

    List<CaseOpt> option = new ArrayList<CaseOpt>();
    for (State src : enumMap.keySet()) {
      CaseOpt opt = makeCaseOption(src, enumMap);
      opt.getCode().getStatements().add(makeCall(src.getExitFunc()));
      option.add(opt);
    }

    CaseStmt caseStmt = new CaseStmt(new ElementInfo(), new Reference(new ElementInfo(), stateVariable), option, new Block(new ElementInfo()));
    body.getStatements().add(caseStmt);

    FuncInputHandlerEvent rfunc = new FuncInputHandlerEvent(info, SystemIfaceAdder.DESTRUCT, new ListOfNamed<FuncVariable>());
    rfunc.setBody(body);

    return rfunc;
  }

  private CallStmt makeCall(Reference funcRef) {
    assert (funcRef.getLink() instanceof FunctionBase);
    assert (funcRef.getOffset().isEmpty());

    FunctionBase func = (FunctionBase) funcRef.getLink();
    return new CallStmt(info, makeCall(func));
  }

  private Reference makeCall(FunctionBase func) {
    assert (func.getParam().isEmpty());
    Reference call = new Reference(info, func);
    call.getOffset().add(new RefCall(info, new ArrayList<Expression>()));
    return call;
  }

  private HashMap<StateSimple, EnumElement> makeEnumElem(StateComposite topstate) {
    HashMap<StateSimple, EnumElement> ret = new HashMap<StateSimple, EnumElement>();
    for (State state : topstate.getItemList(State.class)) {
      assert (state instanceof StateSimple);
      EnumElement element = new EnumElement(state.getInfo(), state.getName());
      ret.put((StateSimple) state, element);
    }
    return ret;
  }

  private List<HfsmQueryFunction> addQueryCode(ImplHfsm obj, Collection<StateSimple> leafes, HashMap<StateSimple, EnumElement> enumMap, StateVariable stateVariable, String ifaceName, FuncInputHandlerQuery func) {
    List<HfsmQueryFunction> queryList = new ArrayList<HfsmQueryFunction>();
    List<CaseOpt> option = new ArrayList<CaseOpt>();
    for (State state : leafes) {
      HfsmQueryFunction query = getQuery(state, ifaceName, func.getName());
      CaseOpt opt = makeCaseOption(state, enumMap);
      opt.getCode().getStatements().add(new ReturnExpr(new ElementInfo(), makeCall(query))); // TODO parameters?
      option.add(opt);

      query.setName(state.getName() + Designator.NAME_SEP + ifaceName + Designator.NAME_SEP + query.getName());
      queryList.add(query);
    }

    CaseStmt caseStmt = new CaseStmt(new ElementInfo(), new Reference(new ElementInfo(), stateVariable, new ArrayList<RefItem>()), option, new Block(new ElementInfo()));
    func.getBody().getStatements().add(caseStmt);

    return queryList;
  }

  private HfsmQueryFunction getQuery(State state, String ifaceName, String funcName) {
    for (QueryItem itr : state.getItemList(QueryItem.class)) {
      if (itr.getNamespace().equals(ifaceName) && itr.getFunc().getName().equals(funcName)) {
        return itr.getFunc();
      }
    }
    RError.err(ErrorType.Fatal, state.getInfo(), "Query not found: " + ifaceName + "." + funcName);
    return null;
  }

  private CaseStmt addTransitionCode(ImplHfsm obj, Collection<StateSimple> leafes, HashMap<StateSimple, EnumElement> enumMap, StateVariable stateVariable, String ifaceName, String funcName, TransitionDict getter, List<FuncVariable> newParam) {
    List<CaseOpt> option = new ArrayList<CaseOpt>();
    for (State src : leafes) {
      ArrayList<IfOption> ifopt = new ArrayList<IfOption>();
      List<Transition> transList = getter.get(src, ifaceName, funcName);
      for (Transition trans : transList) {
        State dst = trans.getDst();
        Block block = makeTransition(trans.getBody(), src, dst, enumMap, stateVariable);
        IfOption entry = new IfOption(trans.getInfo(), trans.getGuard(), block);
        ifopt.add(entry);

        relinkParamRef(newParam, trans.getParam().getList(), entry);
      }
      CaseOpt opt = makeCaseOption(src, enumMap);
      if (!ifopt.isEmpty()) {
        IfStmt ifStmt = new IfStmt(new ElementInfo(), ifopt, new Block(new ElementInfo()));
        opt.getCode().getStatements().add(ifStmt);
      }
      option.add(opt);
    }

    CaseStmt caseStmt = new CaseStmt(new ElementInfo(), new Reference(new ElementInfo(), stateVariable), option, new Block(new ElementInfo()));
    return caseStmt;
  }

  /**
   * Relink references to parameter to new parameter
   *
   * @param newParam
   * @param oldParam
   * @param code
   */
  public void relinkParamRef(List<FuncVariable> newParam, List<FuncVariable> oldParam, Evl code) {
    assert (newParam.size() == oldParam.size());
    Map<FuncVariable, FuncVariable> map = new HashMap<FuncVariable, FuncVariable>();
    for (int i = 0; i < newParam.size(); i++) {
      map.put(oldParam.get(i), newParam.get(i));
    }
    Relinker.relink(code, map);
  }

  private static CaseOpt makeCaseOption(State src, HashMap<StateSimple, EnumElement> enumMap) {
    List<CaseOptEntry> value = new ArrayList<CaseOptEntry>();
    EnumElement enumVal = enumMap.get(src);
    assert (enumVal != null);
    value.add(new CaseOptValue(new ElementInfo(), new Reference(new ElementInfo(), enumVal)));
    Block code = new Block(src.getInfo());
    CaseOpt opt = new CaseOpt(src.getInfo(), value, code);
    return opt;
  }

  private Block makeTransition(Block body, State sitr, State dst, HashMap<StateSimple, EnumElement> enumMap, StateVariable stateVariable) {
    Block block = new Block(new ElementInfo());

    // add transition body code
    block.getStatements().add(body);

    // set new state
    Assignment newState = makeNewStateCode(enumMap, stateVariable, dst);
    block.getStatements().add(newState);

    return block;
  }

  private static Assignment makeNewStateCode(HashMap<StateSimple, EnumElement> enumMap, StateVariable stateVariable, State dst) {
    assert (dst instanceof StateSimple);
    Named dstEnumVal = enumMap.get(dst);
    assert (dstEnumVal != null);
    Assignment newState = new Assignment(new ElementInfo(), new Reference(new ElementInfo(), stateVariable), new Reference(new ElementInfo(), dstEnumVal));
    return newState;
  }

}
