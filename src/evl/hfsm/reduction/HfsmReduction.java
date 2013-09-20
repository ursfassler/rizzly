package evl.hfsm.reduction;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
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
import evl.cfg.BasicBlock;
import evl.cfg.BasicBlockList;
import evl.copy.Copy;
import evl.copy.Relinker;
import evl.expression.Expression;
import evl.expression.reference.RefCall;
import evl.expression.reference.RefName;
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
import evl.statement.bbend.CaseGoto;
import evl.statement.bbend.CaseGotoOpt;
import evl.statement.bbend.CaseOptEntry;
import evl.statement.bbend.CaseOptValue;
import evl.statement.bbend.Goto;
import evl.statement.bbend.IfGoto;
import evl.statement.bbend.ReturnExpr;
import evl.statement.bbend.Unreachable;
import evl.statement.normal.Assignment;
import evl.statement.normal.CallStmt;
import evl.statement.normal.NormalStmt;
import evl.statement.normal.VarDefInitStmt;
import evl.statement.phi.PhiStmt;
import evl.type.TypeRef;
import evl.type.base.EnumElement;
import evl.type.base.EnumType;
import evl.variable.SsaVariable;
import evl.variable.StateVariable;
import evl.variable.Variable;

public class HfsmReduction extends NullTraverser<Named, Namespace> {

  static final private ElementInfo info = new ElementInfo();
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
    assert ( obj instanceof Named );
    return (Named) obj;
  }

  @Override
  protected Named visitNamespace(Namespace obj, Namespace param) {
    for( int i = 0; i < obj.getList().size(); i++ ) {
      Named item = obj.getList().get(i);
      item = visit(item, obj);
      assert ( item != null );
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

    HashMap<StateSimple, EnumElement> enumMap = makeEnumElem(obj.getTopstate());
    EnumType states = new EnumType(obj.getTopstate().getInfo(), obj.getName() + Designator.NAME_SEP + "State", enumMap.values());

    param.add(states);
    StateVariable stateVariable = new StateVariable(obj.getInfo(), "state", new TypeRef(info, states));
    elem.getVariable().add(stateVariable);

    TransitionDict dict = new TransitionDict();
    dict.traverse(obj.getTopstate(), null);

    // create event handler
    for( IfaceUse use : elem.getIface(Direction.in) ) {
      Interface iface = use.getLink();
      List<String> ns = new ArrayList<String>();
      ns.add(use.getName());
      for( FunctionBase header : iface.getPrototype() ) {
        FunctionBase ptoto = Copy.copy(header);
        FunctionBase func;
        if( ptoto instanceof FuncWithReturn ) {
          func = new FuncInputHandlerQuery(obj.getInfo(), header.getName(), ptoto.getParam());
          ( (FuncWithReturn) func ).setRet(( (FuncWithReturn) ptoto ).getRet());
        } else {
          func = new FuncInputHandlerEvent(obj.getInfo(), header.getName(), ptoto.getParam());
        }
        elem.addFunction(ns, func);

        //TODO replace stateVariable with a cached SSA version of it

        BasicBlockList bbl;
        if( func instanceof FuncWithReturn ) {
          bbl = addQueryCode(enumMap.keySet(), enumMap, states, stateVariable, use.getName(), (FuncInputHandlerQuery) func, func.getParam().getList());
        } else {
          bbl = addTransitionCode(enumMap.keySet(), enumMap, states, stateVariable, use.getName(), func.getName(), dict, func.getParam().getList());
        }
        ( (FuncWithBody) func ).setBody(bbl);
      }
    }

    {
      FuncPrivateVoid fEntry = makeEntryFunc(obj.getTopstate().getInitial(), states, enumMap, stateVariable);
      elem.getInternalFunction().add(fEntry);
      elem.setEntryFunc(new Reference(info, fEntry));
      FuncPrivateVoid fExit = makeExitFunc(states, enumMap, stateVariable);
      elem.getInternalFunction().add(fExit);
      elem.setExitFunc(new Reference(info, fExit));
    }

    map.put(obj, elem);
    return elem;
  }

  /**
   * relinking parameter references to new parameter
   */
  public static void relinkActualParameterRef(ListOfNamed<Variable> oldParam, List<Variable> newParam, Evl body) {
    assert ( newParam.size() == oldParam.size() );
    Map<Variable, Variable> map = new HashMap<Variable, Variable>();
    for( int i = 0; i < newParam.size(); i++ ) {
      map.put(oldParam.getList().get(i), newParam.get(i));
    }
    Relinker.relink(body, map);
  }

  static private FuncPrivateVoid makeEntryFunc(State initial, EnumType etype, HashMap<StateSimple, EnumElement> enumMap, StateVariable stateVariable) {
    List<NormalStmt> code = new ArrayList<NormalStmt>();

    // set initial state
    Reference dst = new Reference(info, stateVariable);
    Reference src = makeEnumElemRef(etype, enumMap.get(initial).getName());
    Assignment newState = new Assignment(info, dst, src);
    code.add(newState);

    code.add(makeCall(initial.getEntryFunc()));

    FuncPrivateVoid rfunc = new FuncPrivateVoid(info, fun.hfsm.State.ENTRY_FUNC_NAME, new ListOfNamed<Variable>());
    BasicBlockList body = new BasicBlockList(info);
    body.insertCodeAfterEntry(code, "body");
    rfunc.setBody(body);

    return rfunc;
  }

  static private BasicBlock makeErrorBb() {
    BasicBlock bberror = new BasicBlock(info, "error");
    bberror.setEnd(new Unreachable(info));
    return bberror;
  }

  //TODO ok?
  static private FuncPrivateVoid makeExitFunc(EnumType etype, HashMap<StateSimple, EnumElement> enumMap, StateVariable stateVariable) {
    BasicBlockList body = new BasicBlockList(info);

    List<CaseGotoOpt> option = new ArrayList<CaseGotoOpt>();
    for( State src : enumMap.keySet() ) {
      Reference eref = makeEnumElemRef(etype, enumMap.get(src).getName());
      BasicBlock obb = new BasicBlock(info, "bb" + Designator.NAME_SEP + src.getName());
      body.getBasicBlocks().add(obb);
      CaseGotoOpt opt = makeCaseOption(eref, obb);  //TODO find name
      opt.getDst().getCode().add(makeCall(src.getExitFunc()));  //TODO ok?
      option.add(opt);
      assert ( opt.getDst().getEnd() == null );
      opt.getDst().setEnd(new Goto(info, body.getExit()));
    }

    CaseGoto caseStmt = new CaseGoto(info);
    caseStmt.setCondition(new Reference(info, stateVariable));
    caseStmt.getOption().addAll(option);
    BasicBlock otherwise = new BasicBlock(info, "bb_unknown_state");
    body.getBasicBlocks().add(otherwise);
    otherwise.setEnd(new Goto(info, body.getExit()));
    caseStmt.setOtherwise(otherwise);
    body.getEntry().setEnd(caseStmt);

    FuncPrivateVoid rfunc = new FuncPrivateVoid(info, fun.hfsm.State.EXIT_FUNC_NAME, new ListOfNamed<Variable>());
    rfunc.setBody(body);

    return rfunc;
  }

  static private CallStmt makeCall(Reference funcRef) {
    assert ( funcRef.getLink() instanceof FunctionBase );
    assert ( funcRef.getOffset().isEmpty() );

    FunctionBase func = (FunctionBase) funcRef.getLink();
    return new CallStmt(info, makeCall(func));
  }

  static private Reference makeCall(FunctionBase func) {
    assert ( func.getParam().isEmpty() );
    Reference call = new Reference(info, func);
    call.getOffset().add(new RefCall(info, new ArrayList<Expression>()));
    return call;
  }

  static private HashMap<StateSimple, EnumElement> makeEnumElem(StateComposite topstate) {
    HashMap<StateSimple, EnumElement> ret = new HashMap<StateSimple, EnumElement>();
    for( State state : topstate.getItemList(State.class) ) {
      assert ( state instanceof StateSimple );
      EnumElement element = new EnumElement(state.getInfo(), state.getName());
      ret.put((StateSimple) state, element);
    }
    return ret;
  }

  static private BasicBlockList addQueryCode(Collection<StateSimple> leafes, HashMap<StateSimple, EnumElement> enumMap, EnumType enumType, StateVariable stateVariable, String ifaceName, FuncInputHandlerQuery func, List<Variable> param) {
    BasicBlockList bbl = new BasicBlockList(info);

    BasicBlock bberror = makeErrorBb();
    bbl.getBasicBlocks().add(bberror);

    CaseGoto caseStmt = new CaseGoto(info);
    caseStmt.setCondition(new Reference(info, stateVariable));
    caseStmt.setOtherwise(bberror);

    bbl.getEntry().setEnd(caseStmt);

    SsaVariable retval = new SsaVariable(info, Designator.NAME_SEP + "retval", func.getRet().copy());
    PhiStmt phi = new PhiStmt(info, retval);
    bbl.getExit().getPhi().add(phi);
    bbl.getExit().setEnd(new ReturnExpr(info, new Reference(info, retval)));

    for( State state : leafes ) {
      BasicBlock stateBb = new BasicBlock(info, "bb" + Designator.NAME_SEP + state.getName());
      bbl.getBasicBlocks().add(stateBb);
      stateBb.setEnd(new Goto(info, bbl.getExit()));

      HfsmQueryFunction query = getQuery(state, ifaceName, func.getName());

      assert ( query.getBody().getEntry().getCode().isEmpty() );
      assert ( query.getBody().getExit().getCode().isEmpty() );
      assert ( query.getBody().getBasicBlocks().size() == 1 );
      BasicBlock bbb = query.getBody().getBasicBlocks().iterator().next();
      assert ( bbb.getCode().size() == 1 );
      VarDefInitStmt call = (VarDefInitStmt) bbb.getCode().get(0);  // because we defined it that way (in QueryDownPropagator)

      call.getVariable().setName("retval" + Designator.NAME_SEP + state.getName());
      stateBb.getCode().add(call);

      relinkActualParameterRef(query.getParam(), param, call.getInit());

      phi.addArg(stateBb, new Reference(info, call.getVariable()));

      CaseGotoOpt opt = makeCaseOption(makeEnumElemRef(enumType, enumMap.get(state).getName()), stateBb);
      caseStmt.getOption().add(opt);
    }

    return bbl;
  }

  static private HfsmQueryFunction getQuery(State state, String ifaceName, String funcName) {
    for( QueryItem itr : state.getItemList(QueryItem.class) ) {
      if( itr.getNamespace().equals(ifaceName) && itr.getFunc().getName().equals(funcName) ) {
        return itr.getFunc();
      }
    }
    RError.err(ErrorType.Fatal, state.getInfo(), "Query not found: " + ifaceName + "." + funcName);
    return null;
  }

  static private BasicBlockList addTransitionCode(Collection<StateSimple> leafes, HashMap<StateSimple, EnumElement> enumMap, EnumType enumType, StateVariable stateVariable, String ifaceName, String funcName, TransitionDict getter, List<Variable> param) {
    BasicBlockList bbl = new BasicBlockList(info);

    BasicBlock bberror = makeErrorBb();
    bbl.getBasicBlocks().add(bberror);

    CaseGoto caseStmt = new CaseGoto(info);
    caseStmt.setCondition(new Reference(info, stateVariable));
    caseStmt.setOtherwise(bberror);

    bbl.getEntry().setEnd(caseStmt);

    SsaVariable newStateVal = new SsaVariable(info, "newStateVal", stateVariable.getType().copy());
    PhiStmt phi = new PhiStmt(info, newStateVal);
    bbl.getExit().getPhi().add(phi);

    Assignment writeState = new Assignment(info, new Reference(info, stateVariable), new Reference(info, newStateVal));
    bbl.getExit().getCode().add(0, writeState);

    for( State src : leafes ) {
      BasicBlock stateBb = new BasicBlock(info, "bb" + Designator.NAME_SEP + src.getName());
      bbl.getBasicBlocks().add(stateBb);

      CaseGotoOpt opt = makeCaseOption(makeEnumElemRef(enumType, enumMap.get(src).getName()), stateBb);
      caseStmt.getOption().add(opt);

      List<Transition> transList = getter.get(src, ifaceName, funcName);
      if( transList.isEmpty() ) {
        stateBb.setEnd(new Goto(info, bbl.getExit()));

        // write phi value if no transition was taken
        phi.addArg(stateBb, new Reference(info, stateVariable));
      } else {
        Map<Transition, BasicBlock> guardMap = new HashMap<Transition, BasicBlock>();
        Map<Transition, BasicBlock> codeMap = new HashMap<Transition, BasicBlock>();
        makeGuardedTrans(transList, src, bbl, guardMap, codeMap, param);

        stateBb.setEnd(new Goto(info, guardMap.get(transList.get(0))));

        // write phi values for new state
        for( Transition trans : transList ) {
          BasicBlock bb = codeMap.get(trans);
          assert ( bb != null );
          phi.addArg(bb, makeEnumElemRef(enumType, enumMap.get(trans.getDst()).getName()));
        }

        // write phi value if no transition was taken
        BasicBlock bb = guardMap.get(transList.get(transList.size() - 1));
        assert ( bb != null );
        phi.addArg(bb, new Reference(info, stateVariable));
      }
    }

    return bbl;
  }

  /**
   * For every transition, it adds a basic block with the guard in it and for the "then" part another basic block with code if the transition is taken.
   * The "else" part points to the next basic block or exit if there is no more transition.
   */
  static private void makeGuardedTrans(List<Transition> transList, State src, BasicBlockList bbl, Map<Transition, BasicBlock> guardMap, Map<Transition, BasicBlock> codeMap, List<Variable> newparam) {
    BasicBlock blockElse = bbl.getExit();

    ArrayList<Transition> rl = new ArrayList<Transition>(transList);
    Collections.reverse(rl);
    for( Transition trans : rl ) {

      BasicBlock blockThen = makeTransition(trans, bbl.getExit(), newparam);
      bbl.getBasicBlocks().add(blockThen);

      relinkActualParameterRef(trans.getParam(), newparam, trans.getGuard());  // relink references to arguments to the new ones

      IfGoto entry = new IfGoto(trans.getInfo());
      entry.setCondition(trans.getGuard());
      entry.setThenBlock(blockThen);
      entry.setElseBlock(blockElse);

      BasicBlock guardBb = new BasicBlock(info, "bb" + Designator.NAME_SEP + src.getName() + Designator.NAME_SEP + trans.getName());
      bbl.getBasicBlocks().add(guardBb);
      guardBb.setEnd(entry);

      blockElse = guardBb;

      guardMap.put(trans, guardBb);
      codeMap.put(trans, blockThen);
    }
  }

  private static Reference makeEnumElemRef(EnumType type, String value) {
    EnumElement elem = type.find(value);
    assert ( elem != null );
    Reference eval = new Reference(info, type);
    eval.getOffset().add(new RefName(info, value));
    return eval;
  }

  private static CaseGotoOpt makeCaseOption(Reference caseval, BasicBlock code) {
    List<CaseOptEntry> value = new ArrayList<CaseOptEntry>();
    value.add(new CaseOptValue(info, caseval));
    CaseGotoOpt opt = new CaseGotoOpt(info, value, code);
    return opt;
  }

  /**
   * Checks if transition is built like we expect and makes a transition bb out of it.
   */
  static private BasicBlock makeTransition(Transition trans, BasicBlock nextBb, List<Variable> param) {
    BasicBlock transCode = new BasicBlock(info, "bb" + Designator.NAME_SEP + trans.getName());

    assert ( trans.getBody().getEntry().getCode().isEmpty() );
    assert ( trans.getBody().getExit().getCode().isEmpty() );
    assert ( trans.getBody().getExit().getPhi().isEmpty() );
    assert ( trans.getBody().getBasicBlocks().size() == 1 );

    BasicBlock body = trans.getBody().getBasicBlocks().iterator().next();
    relinkActualParameterRef(trans.getParam(), param, body);

    transCode.getCode().addAll(body.getCode());

    transCode.setEnd(new Goto(info, nextBb));

    return transCode;
  }
}
