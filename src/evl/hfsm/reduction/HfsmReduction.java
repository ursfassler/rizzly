package evl.hfsm.reduction;

import evl.variable.SsaVariable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import common.Designator;
import common.Direction;
import common.ElementInfo;

import common.NameFactory;
import error.ErrorType;
import error.RError;
import evl.Evl;
import evl.NullTraverser;
import evl.cfg.BasicBlock;
import evl.cfg.BasicBlockList;
import evl.statement.bbend.CaseGoto;
import evl.statement.bbend.CaseGotoOpt;
import evl.statement.bbend.CaseOptEntry;
import evl.statement.bbend.CaseOptValue;
import evl.statement.bbend.Goto;
import evl.statement.bbend.IfGoto;
import evl.statement.bbend.ReturnExpr;
import evl.copy.Copy;
import evl.copy.Relinker;
import evl.expression.Expression;
import evl.expression.reference.RefCall;
import evl.expression.reference.RefItem;
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
import evl.statement.normal.CallStmt;
import evl.statement.normal.Assignment;
import evl.statement.normal.NormalStmt;
import evl.statement.phi.PhiStmt;
import evl.type.TypeRef;
import evl.type.base.EnumElement;
import evl.type.base.EnumType;
import evl.variable.FuncVariable;
import evl.variable.StateVariable;
import evl.variable.Variable;
import java.util.Collections;

//TODO previous of this step: for every transition, create a function with the body content
//TODO in this step: call the transition function when the transition is taken
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

    elem.setEntryFunc(makeEntryExitFunc(fun.hfsm.State.ENTRY_FUNC_NAME, elem.getInternalFunction()));
    elem.setExitFunc(makeEntryExitFunc(fun.hfsm.State.EXIT_FUNC_NAME, elem.getInternalFunction()));

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
          List<HfsmQueryFunction> queryList = addQueryCode(obj, enumMap.keySet(), enumMap, stateVariable, use.getName(), (FuncInputHandlerQuery) func);
          elem.getInternalFunction().addAll(queryList);
          throw new RuntimeException("not yet implemented");
        } else {
          bbl = addTransitionCode(enumMap.keySet(), enumMap, states, stateVariable, use.getName(), func.getName(), dict, func.getParam().getList());
        }
        ( (FuncWithBody) func ).setBody(bbl);
      }
    }

    // create constructor and destructor
    {
      Interface type = kbi.get(Interface.class, SystemIfaceAdder.IFACE_TYPE_NAME);
      IfaceUse debIface = new IfaceUse(info, SystemIfaceAdder.IFACE_USE_NAME, type);
      elem.getIface(Direction.in).add(debIface);

      List<String> ns = new ArrayList<String>();
      ns.add(SystemIfaceAdder.IFACE_USE_NAME);
      elem.addFunction(ns, makeConstructor(obj.getTopstate().getInitial(), states, enumMap, stateVariable));
      elem.addFunction(ns, makeDestructor(states, enumMap, stateVariable));

    }

    map.put(obj, elem);
    return elem;
  }

  private Reference makeEntryExitFunc(String name, ListOfNamed<FunctionBase> list) {
    FuncPrivateVoid func = new FuncPrivateVoid(info, name, new ListOfNamed<Variable>());
    func.setBody(new BasicBlockList(info));
    list.add(func);
    return new Reference(info, func);
  }

  // TODO make new entry function out of the following code; maybe add constructor and destructor after reudction to
  // elementary
  private FuncInputHandlerEvent makeConstructor(State initial, EnumType etype, HashMap<StateSimple, EnumElement> enumMap, StateVariable stateVariable) {
    List<NormalStmt> code = new ArrayList<NormalStmt>();

    // copy entry code
    code.add(makeCall(initial.getEntryFunc()));
    // set initial state
    Reference dst = new Reference(new ElementInfo(), stateVariable);
    Reference src = makeEnumElemRef(etype,enumMap.get(initial).getName());
    Assignment newState = new Assignment(new ElementInfo(), dst, src);
    code.add(newState);

    FuncInputHandlerEvent rfunc = new FuncInputHandlerEvent(info, SystemIfaceAdder.CONSTRUCT, new ListOfNamed<Variable>());
    BasicBlockList body = new BasicBlockList(info);
    body.insertCodeAfterEntry(code, "body");
    rfunc.setBody(body);

    return rfunc;
  }

  //TODO ok?
  private FuncInputHandlerEvent makeDestructor( EnumType etype, HashMap<StateSimple, EnumElement> enumMap, StateVariable stateVariable) {
    BasicBlockList body = new BasicBlockList(info);

    List<CaseGotoOpt> option = new ArrayList<CaseGotoOpt>();
    for( State src : enumMap.keySet() ) {
      Reference eref = makeEnumElemRef(etype, enumMap.get(src).getName());
      CaseGotoOpt opt = makeCaseOption( eref, new BasicBlock(info, NameFactory.getNew()));  //TODO find name
      opt.getDst().getCode().add(makeCall(src.getExitFunc()));  //TODO ok?
      option.add(opt);
      assert ( opt.getDst().getEnd() == null );
      opt.getDst().setEnd(new Goto(info, body.getExit()));
    }

    CaseGoto caseStmt = new CaseGoto(new ElementInfo());
    caseStmt.setCondition(new Reference(new ElementInfo(), stateVariable));
    caseStmt.getOption().addAll(option);
    BasicBlock otherwise = new BasicBlock(info, NameFactory.getNew());
    otherwise.setEnd(new Goto(info, body.getExit()));
    caseStmt.setOtherwise(otherwise);
    body.getEntry().setEnd(caseStmt);

    FuncInputHandlerEvent rfunc = new FuncInputHandlerEvent(info, SystemIfaceAdder.DESTRUCT, new ListOfNamed<Variable>());
    rfunc.setBody(body);

    return rfunc;
  }

  private CallStmt makeCall(Reference funcRef) {
    assert ( funcRef.getLink() instanceof FunctionBase );
    assert ( funcRef.getOffset().isEmpty() );

    FunctionBase func = (FunctionBase) funcRef.getLink();
    return new CallStmt(info, makeCall(func));
  }

  private Reference makeCall(FunctionBase func) {
    assert ( func.getParam().isEmpty() );
    Reference call = new Reference(info, func);
    call.getOffset().add(new RefCall(info, new ArrayList<Expression>()));
    return call;
  }

  private HashMap<StateSimple, EnumElement> makeEnumElem(StateComposite topstate) {
    HashMap<StateSimple, EnumElement> ret = new HashMap<StateSimple, EnumElement>();
    for( State state : topstate.getItemList(State.class) ) {
      assert ( state instanceof StateSimple );
      EnumElement element = new EnumElement(state.getInfo(), state.getName());
      ret.put((StateSimple) state, element);
    }
    return ret;
  }

  private List<HfsmQueryFunction> addQueryCode(ImplHfsm obj, Collection<StateSimple> leafes, HashMap<StateSimple, EnumElement> enumMap, StateVariable stateVariable, String ifaceName, FuncInputHandlerQuery func) {
    List<HfsmQueryFunction> queryList = new ArrayList<HfsmQueryFunction>();
    List<CaseOpt> option = new ArrayList<CaseOpt>();
    for( State state : leafes ) {
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
    for( QueryItem itr : state.getItemList(QueryItem.class) ) {
      if( itr.getNamespace().equals(ifaceName) && itr.getFunc().getName().equals(funcName) ) {
        return itr.getFunc();
      }
    }
    RError.err(ErrorType.Fatal, state.getInfo(), "Query not found: " + ifaceName + "." + funcName);
    return null;
  }

  private BasicBlockList addTransitionCode(Collection<StateSimple> leafes, HashMap<StateSimple, EnumElement> enumMap, EnumType enumType, StateVariable stateVariable, String ifaceName, String funcName, TransitionDict getter, List<Variable> param) {
    BasicBlockList bbl = new BasicBlockList(info);

    CaseGoto caseStmt = new CaseGoto(info);
    caseStmt.setCondition(new Reference(info, stateVariable));
    caseStmt.setOtherwise(bbl.getExit());

    bbl.getEntry().setEnd(caseStmt);

    SsaVariable newStateVal = new SsaVariable(info, "newStateVal", stateVariable.getType().copy());
    PhiStmt phi = new PhiStmt(info, newStateVal);
    bbl.getExit().getPhi().add(phi);
    phi.addArg(bbl.getEntry(), new Reference(info, stateVariable));  // set phi arg if state was not in list (should not happen?)

    Assignment writeState = new Assignment(info, new Reference(info, stateVariable), new Reference(info, newStateVal));
    bbl.getExit().getCode().add(0, writeState);

    for( State src : leafes ) {
      BasicBlock stateBb = new BasicBlock(info, "bb" + Designator.NAME_SEP + src.getName());
      bbl.getBasicBlocks().add(stateBb);

      CaseGotoOpt opt = makeCaseOption( makeEnumElemRef(enumType, enumMap.get(src).getName()), stateBb);
      caseStmt.getOption().add(opt);

      List<Transition> transList = getter.get(src, ifaceName, funcName);
      if( transList.isEmpty() ) {
        stateBb.setEnd(new Goto(info, bbl.getExit()));

        // write phi value if no transition was taken
        phi.addArg(stateBb, new Reference(info, stateVariable));
      } else {
        Map<Transition, BasicBlock> guardMap = new HashMap<Transition, BasicBlock>();
        Map<Transition, BasicBlock> codeMap = new HashMap<Transition, BasicBlock>();
        makeGuardedTrans(transList, src, bbl, guardMap, codeMap);

        stateBb.setEnd(new Goto(info, guardMap.get(transList.get(0))));

        // write phi values for new state
        for( Transition trans : transList ) {
          BasicBlock bb = codeMap.get(trans);
          assert ( bb != null );
          phi.addArg(bb, makeEnumElemRef(enumType,enumMap.get(trans.getDst()).getName()));
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
  private void makeGuardedTrans(List<Transition> transList, State src, BasicBlockList bbl, Map<Transition, BasicBlock> guardMap, Map<Transition, BasicBlock> codeMap) {
    BasicBlock blockElse = bbl.getExit();

    ArrayList<Transition> rl = new ArrayList<Transition>(transList);
    Collections.reverse(rl);
    for( Transition trans : rl ) {

      BasicBlock blockThen = makeTransition(trans, bbl.getExit());
      bbl.getBasicBlocks().add(blockThen);

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

  /**
   * Relink references to parameter to new parameter
   *
   * @param newParam
   * @param oldParam
   * @param code
   */
  public void relinkParamRef(List<Variable> newParam, List<Variable> oldParam, Evl code) {
    assert ( newParam.size() == oldParam.size() );
    Map<FuncVariable, FuncVariable> map = new HashMap<FuncVariable, FuncVariable>();
    for( int i = 0; i < newParam.size(); i++ ) {
      map.put(oldParam.get(i), newParam.get(i));
    }
    Relinker.relink(code, map);
  }

  private static Reference makeEnumElemRef(EnumType type, String value) {
    EnumElement elem = type.find(value);
    assert(elem != null);
    Reference eval = new Reference(new ElementInfo(), type);
    eval.getOffset().add(new RefName(new ElementInfo(), value));
    return eval;
  }

  private static CaseGotoOpt makeCaseOption(Reference eref, BasicBlock code) {
    List<CaseOptEntry> value = new ArrayList<CaseOptEntry>();
    value.add(new CaseOptValue(new ElementInfo(), eref));
    CaseGotoOpt opt = new CaseGotoOpt(new ElementInfo(), value, code);
    return opt;
  }

  /**
   * Makes one BasicBlock with everithing a transition needs:
   * - call of all exit functions
   * - call of the transition body
   * - call of all entry functions
   */
  private BasicBlock makeTransition(Transition trans, BasicBlock nextBb) {
    BasicBlock transCode = new BasicBlock(info, "bb" + Designator.NAME_SEP + trans.getName());

    //TODO make exit calls;
    //TODO call transition function
    //TODO make entry calls

    transCode.setEnd(new Goto(info, nextBb));

    return transCode;
  }

  private static Assignment makeNewStateCode(Reference eref, StateVariable stateVariable) {
    Assignment newState = new Assignment(new ElementInfo(), new Reference(new ElementInfo(), stateVariable), eref);
    return newState;
  }

}
