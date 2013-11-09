package evl.hfsm.reduction;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import common.Designator;
import common.ElementInfo;

import error.ErrorType;
import error.RError;
import evl.Evl;
import evl.NullTraverser;
import evl.copy.Copy;
import evl.copy.Relinker;
import evl.expression.Expression;
import evl.expression.Number;
import evl.expression.reference.RefCall;
import evl.expression.reference.Reference;
import evl.function.FuncIfaceIn;
import evl.function.FuncWithBody;
import evl.function.FuncWithReturn;
import evl.function.FunctionBase;
import evl.function.impl.FuncInputHandlerEvent;
import evl.function.impl.FuncInputHandlerQuery;
import evl.function.impl.FuncPrivateVoid;
import evl.function.impl.FuncProtoVoid;
import evl.hfsm.HfsmQueryFunction;
import evl.hfsm.ImplHfsm;
import evl.hfsm.State;
import evl.hfsm.StateComposite;
import evl.hfsm.StateSimple;
import evl.hfsm.Transition;
import evl.knowledge.KnowBaseItem;
import evl.knowledge.KnowLlvmLibrary;
import evl.knowledge.KnowledgeBase;
import evl.other.ImplElementary;
import evl.other.ListOfNamed;
import evl.other.Named;
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
import evl.type.TypeRef;
import evl.type.base.EnumDefRef;
import evl.type.base.EnumElement;
import evl.type.base.EnumType;
import evl.variable.FuncVariable;
import evl.variable.StateVariable;
import evl.variable.Variable;

public class HfsmReduction extends NullTraverser<Named, Namespace> {

  static final private ElementInfo info = new ElementInfo();
  private KnowBaseItem kbi;
  private KnowLlvmLibrary kll;
  private Map<ImplHfsm, ImplElementary> map = new HashMap<ImplHfsm, ImplElementary>();

  public HfsmReduction(KnowledgeBase kb) {
    this.kbi = kb.getEntry(KnowBaseItem.class);
    this.kll = kb.getEntry(KnowLlvmLibrary.class);
  }

  public static Map<ImplHfsm, ImplElementary> process(Namespace classes, KnowledgeBase kb) {
    HfsmReduction reduction = new HfsmReduction(kb);
    reduction.traverse(classes, null);
    // reduction.visitItr(classes, null);
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
    elem.getInput().addAll(obj.getInput());
    elem.getOutput().addAll(obj.getOutput());
    elem.getVariable().addAll(obj.getTopstate().getVariable());
    elem.getFunction().addAll(obj.getTopstate().getFunction());

    EnumType states = new EnumType(obj.getTopstate().getInfo(), obj.getName() + Designator.NAME_SEP + "State");
    HashMap<StateSimple, EnumElement> enumMap = makeEnumElem(obj.getTopstate(), states, param);

    param.add(states);
    StateVariable stateVariable = new StateVariable(obj.getInfo(), "state", new TypeRef(info, states));
    elem.getVariable().add(stateVariable);

    TransitionDict dict = new TransitionDict();
    dict.traverse(obj.getTopstate(), null);

    // create event handler
    for (FuncIfaceIn header : elem.getInput()) {
      FuncIfaceIn ptoto = Copy.copy(header);
      FunctionBase func;
      if (ptoto instanceof FuncWithReturn) {
        func = new FuncInputHandlerQuery(obj.getInfo(), header.getName(), ptoto.getParam());
        ((FuncWithReturn) func).setRet(((FuncWithReturn) ptoto).getRet());
      } else {
        func = new FuncInputHandlerEvent(obj.getInfo(), header.getName(), ptoto.getParam());
      }
      elem.getFunction().add(func);

      Statement code;
      if (func instanceof FuncWithReturn) {
        code = addQueryCode(enumMap.keySet(), enumMap, states, stateVariable, (FuncInputHandlerQuery) func, func.getParam().getList());
      } else {
        code = addTransitionCode(enumMap.keySet(), enumMap, states, stateVariable, func.getName(), dict, func.getParam().getList());
      }
      Block bbl = new Block(info);
      bbl.getStatements().add(code);
      ((FuncWithBody) func).setBody(bbl);
    }

    {
      FuncPrivateVoid fEntry = makeEntryFunc(obj.getTopstate().getInitial(), states, enumMap, stateVariable);
      elem.getFunction().add(fEntry);
      elem.setEntryFunc(new Reference(info, fEntry));
      FuncPrivateVoid fExit = makeExitFunc(states, enumMap, stateVariable);
      elem.getFunction().add(fExit);
      elem.setExitFunc(new Reference(info, fExit));
    }

    map.put(obj, elem);
    return elem;
  }

  /**
   * relinking parameter references to new parameter
   */
  public static void relinkActualParameterRef(ListOfNamed<FuncVariable> oldParam, List<FuncVariable> newParam, Evl body) {
    assert (newParam.size() == oldParam.size());
    Map<Variable, Variable> map = new HashMap<Variable, Variable>();
    for (int i = 0; i < newParam.size(); i++) {
      map.put(oldParam.getList().get(i), newParam.get(i));
    }
    Relinker.relink(body, map);
  }

  static private FuncPrivateVoid makeEntryFunc(State initial, EnumType etype, HashMap<StateSimple, EnumElement> enumMap, StateVariable stateVariable) {
    Block body = new Block(info);

    // set initial state
    Reference dst = new Reference(info, stateVariable);
    Reference src = makeEnumElemRef(etype, enumMap.get(initial).getName());
    Assignment newState = new Assignment(info, dst, src);
    body.getStatements().add(newState);

    body.getStatements().add(makeCall(initial.getEntryFunc()));

    FuncPrivateVoid rfunc = new FuncPrivateVoid(info, fun.hfsm.State.ENTRY_FUNC_NAME, new ListOfNamed<FuncVariable>());
    rfunc.setBody(body);

    return rfunc;
  }

  private Block makeErrorBb() {
    Block bberror = new Block(info);
    FuncProtoVoid trap = kll.getTrap();
    bberror.getStatements().add(new CallStmt(info, new Reference(info, trap, new RefCall(info, new ArrayList<Expression>()))));
    return bberror;
  }

  // TODO ok?
  private FuncPrivateVoid makeExitFunc(EnumType etype, HashMap<StateSimple, EnumElement> enumMap, StateVariable stateVariable) {

    List<CaseOpt> option = new ArrayList<CaseOpt>();
    for (State src : enumMap.keySet()) {
      Reference eref = makeEnumElemRef(etype, enumMap.get(src).getName());
      Block obb = new Block(info);
      CaseOpt opt = makeCaseOption(eref, obb);
      obb.getStatements().add(makeCall(src.getExitFunc())); // TODO ok?
      option.add(opt);
    }

    CaseStmt caseStmt = new CaseStmt(info, new Reference(info, stateVariable), option, makeErrorBb());

    Block body = new Block(info);
    body.getStatements().add(caseStmt);

    FuncPrivateVoid rfunc = new FuncPrivateVoid(info, fun.hfsm.State.EXIT_FUNC_NAME, new ListOfNamed<FuncVariable>());
    rfunc.setBody(body);

    return rfunc;
  }

  static private CallStmt makeCall(Reference funcRef) {
    assert (funcRef.getLink() instanceof FunctionBase);
    assert (funcRef.getOffset().isEmpty());

    FunctionBase func = (FunctionBase) funcRef.getLink();
    return new CallStmt(info, makeCall(func));
  }

  static private Reference makeCall(FunctionBase func) {
    assert (func.getParam().isEmpty());
    Reference call = new Reference(info, func);
    call.getOffset().add(new RefCall(info, new ArrayList<Expression>()));
    return call;
  }

  static private HashMap<StateSimple, EnumElement> makeEnumElem(StateComposite topstate, EnumType stateEnum, Namespace param) {
    HashMap<StateSimple, EnumElement> ret = new HashMap<StateSimple, EnumElement>();
    for (State state : topstate.getItemList(State.class)) {
      assert (state instanceof StateSimple);

      EnumElement element = new EnumElement(info, state.getName(), new TypeRef(info, stateEnum), new Number(info, BigInteger.valueOf(stateEnum.getElement().size())));
      param.add(element);

      stateEnum.getElement().add(new EnumDefRef(info, element));

      ret.put((StateSimple) state, element);
    }
    return ret;
  }

  private CaseStmt addQueryCode(Collection<StateSimple> leafes, HashMap<StateSimple, EnumElement> enumMap, EnumType enumType, StateVariable stateVariable, FuncInputHandlerQuery func, List<FuncVariable> param) {
    List<CaseOpt> copt = new ArrayList<CaseOpt>();

    for (State state : leafes) {

      HfsmQueryFunction query = getQuery(state, func.getName());

      // from QueryDownPropagator
      assert (query.getBody().getStatements().size() == 1);
      ReturnExpr retcall = (ReturnExpr) query.getBody().getStatements().get(0);

      relinkActualParameterRef(query.getParam(), param, retcall.getExpr());

      Block stateBb = new Block(info);
      stateBb.getStatements().add(retcall);
      CaseOpt opt = makeCaseOption(makeEnumElemRef(enumType, enumMap.get(state).getName()), stateBb);

      copt.add(opt);
    }

    CaseStmt caseStmt = new CaseStmt(info, new Reference(info, stateVariable), copt, makeErrorBb());

    return caseStmt;
  }

  static private HfsmQueryFunction getQuery(State state, String funcName) {
    for (HfsmQueryFunction itr : state.getItemList(HfsmQueryFunction.class)) {
      if (itr.getName().equals(funcName)) {
        return itr;
      }
    }
    RError.err(ErrorType.Fatal, state.getInfo(), "Query not found: " + funcName);
    return null;
  }

  private CaseStmt addTransitionCode(Collection<StateSimple> leafes, HashMap<StateSimple, EnumElement> enumMap, EnumType enumType, StateVariable stateVariable, String funcName, TransitionDict getter, List<FuncVariable> param) {

    List<CaseOpt> options = new ArrayList<CaseOpt>();

    for (State src : leafes) {
      Block body = new Block(info);
      Reference label = makeEnumElemRef(enumType, enumMap.get(src).getName());
      CaseOpt opt = makeCaseOption(label, body);

      List<Transition> transList = getter.get(src, funcName);
      if (!transList.isEmpty()) {
        body.getStatements().add(makeGuardedTrans(transList, param, stateVariable, enumMap));
      }

      options.add(opt);
    }

    Block bberror = makeErrorBb();

    CaseStmt caseStmt = new CaseStmt(info, new Reference(info, stateVariable), options, bberror);

    return caseStmt;
  }

  /**
   * For every transition, it adds a basic block with the guard in it and for the "then" part another basic block with
   * code if the transition is taken. The "else" part points to the next basic block or exit if there is no more
   * transition.
   * 
   * @return
   */
  static private IfStmt makeGuardedTrans(List<Transition> transList, List<FuncVariable> newparam, StateVariable stateVariable, HashMap<StateSimple, EnumElement> enumMap) {
    Block def = new Block(info);

    Collection<IfOption> option = new ArrayList<IfOption>();

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

  private static Reference makeEnumElemRef(EnumType type, String value) {
    EnumElement elem = type.find(value);
    assert (elem != null);
    Reference eval = new Reference(info, elem);
    return eval;
  }

  private static CaseOpt makeCaseOption(Expression label, Block code) {
    List<CaseOptEntry> list = new ArrayList<CaseOptEntry>();
    list.add(new CaseOptValue(info, label));
    CaseOpt opt = new CaseOpt(info, list, code);
    return opt;
  }

  /**
   * Checks if transition is built like we expect and makes a transition bb out of it.
   */
  static private Block makeTransition(Transition trans, List<FuncVariable> param, StateVariable stateVariable, HashMap<StateSimple, EnumElement> enumMap) {
    Block transCode = new Block(info);

    Block body = trans.getBody();
    relinkActualParameterRef(trans.getParam(), param, body);

    transCode.getStatements().addAll(body.getStatements());

    Assignment setState = new Assignment(info, new Reference(info, stateVariable), new Reference(info, enumMap.get(trans.getDst())));
    transCode.getStatements().add(setState);

    return transCode;
  }
}
