package fun.toevl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import common.Designator;
import common.Direction;

import error.ErrorType;
import error.RError;
import evl.Evl;
import evl.composition.Endpoint;
import evl.composition.EndpointSelf;
import evl.composition.EndpointSub;
import evl.expression.reference.RefName;
import evl.expression.reference.Reference;
import evl.function.FuncIfaceIn;
import evl.function.FuncIfaceOut;
import evl.function.FunctionBase;
import evl.hfsm.State;
import evl.hfsm.StateItem;
import evl.other.CompUse;
import evl.statement.Block;
import evl.statement.CaseOptEntry;
import evl.type.Type;
import evl.type.TypeRef;
import fun.Fun;
import fun.NullTraverser;
import fun.composition.Connection;
import fun.composition.ImplComposition;
import fun.expression.Expression;
import fun.expression.reference.RefItem;
import fun.function.FunctionHeader;
import fun.hfsm.ImplHfsm;
import fun.hfsm.StateComposite;
import fun.hfsm.StateSimple;
import fun.hfsm.Transition;
import fun.other.ImplElementary;
import fun.other.ListOfNamed;
import fun.other.Named;
import fun.other.Namespace;
import fun.statement.CaseOpt;
import fun.statement.Statement;
import fun.type.composed.NamedElement;
import fun.type.composed.UnionSelector;
import fun.variable.Constant;
import fun.variable.Variable;

public class FunToEvl extends NullTraverser<Evl, Void> {
  Map<Fun, Evl> map = new HashMap<Fun, Evl>();
  private FunToEvlType type = new FunToEvlType(this);
  private FunToEvlExpr expr = new FunToEvlExpr(this);
  private FunToEvlRef ref = new FunToEvlRef(this);
  private FunToEvlFunc func;
  private FunToEvlStmt stmt = new FunToEvlStmt(this);
  private FunToEvlVariable var = new FunToEvlVariable(this);
  private FunToEvlCaseOptEntry caoe = new FunToEvlCaseOptEntry(this);

  public FunToEvl(Map<FunctionHeader, Class<? extends FunctionBase>> funType) {
    super();
    func = new FunToEvlFunc(this, map, funType);
  }

  public static evl.other.Namespace process(Namespace classes, String debugdir) {
    Map<FunctionHeader, Class<? extends FunctionBase>> funType = FuncTypeCollector.process(classes);
    FunToEvl funToAst = new FunToEvl(funType);
    return (evl.other.Namespace) funToAst.traverse(classes, null);
  }

  public static TypeRef toTypeRef(Reference ref) {
    assert (ref.getOffset().isEmpty());
    assert (ref.getLink() instanceof Type);
    TypeRef typeRef = new TypeRef(ref.getInfo(), (Type) ref.getLink());
    return typeRef;
  }

  @Override
  protected Evl visit(Fun obj, Void param) {
    Evl cobj = map.get(obj);
    if (cobj == null) {
      cobj = super.visit(obj, param);
      assert (cobj != null);
      map.put(obj, cobj);
    }
    return cobj;
  }

  @Override
  protected Evl visitDefault(Fun obj, Void param) {
    throw new RuntimeException("not yet implemented: " + obj.getClass().getCanonicalName());
  }

  @Override
  protected Evl visitNamespace(Namespace obj, Void param) {
    evl.other.Namespace ret = new evl.other.Namespace(obj.getInfo(), obj.getName());
    map.put(obj, ret);
    for (Named src : obj) {
      evl.other.Named ni = (evl.other.Named) visit(src, param);
      ret.add(ni);
    }
    return ret;
  }

  @Override
  protected Evl visitType(fun.type.Type obj, Void param) {
    return type.traverse(obj, obj.getName());
  }

  @Override
  protected Evl visitExpression(Expression obj, Void param) {
    return expr.traverse(obj, param);
  }

  @Override
  protected Evl visitRefItem(RefItem obj, Void param) {
    return ref.traverse(obj, null);
  }

  @Override
  protected Evl visitStatement(Statement obj, Void param) {
    return stmt.traverse(obj, null);
  }

  @Override
  protected Evl visitVariable(Variable obj, Void param) {
    return var.traverse(obj, null);
  }

  @Override
  protected Evl visitCaseOptEntry(fun.statement.CaseOptEntry obj, Void param) {
    return caoe.traverse(obj, null);
  }

  @Override
  protected Evl visitFunctionHeader(FunctionHeader obj, Void param) {
    return func.traverse(obj, null);
  }

  @Override
  protected Evl visitNamedElement(NamedElement obj, Void param) {
    Reference ref = (Reference) traverse(obj.getType(), null);
    return new evl.type.composed.NamedElement(obj.getInfo(), obj.getName(), FunToEvl.toTypeRef(ref));
  }

  @Override
  protected Evl visitUnionSelector(UnionSelector obj, Void param) {
    return new evl.type.composed.UnionSelector(obj.getInfo(), obj.getName());
  }

  @Override
  protected Evl visitConnection(Connection obj, Void param) {
    Reference srcref = (Reference) traverse(obj.getEndpoint(Direction.in), null);
    Reference dstref = (Reference) traverse(obj.getEndpoint(Direction.out), null);
    return new evl.composition.Connection(obj.getInfo(), refToEnp(srcref), refToEnp(dstref), obj.getType());
  }

  private Endpoint refToEnp(Reference ref) {
    switch (ref.getOffset().size()) {
    case 0: {
      return new EndpointSelf(ref.getInfo(), (evl.function.FuncIface) ref.getLink());
    }
    case 1: {
      CompUse comp = (CompUse) ref.getLink();
      String iface = ((RefName) ref.getOffset().get(0)).getName();
      return new EndpointSub(ref.getInfo(), comp, iface);
    }
    default: {
      RError.err(ErrorType.Fatal, ref.getInfo(), "Unknown connection endpoint");
      return null;
    }
    }
  }

  @Override
  protected Evl visitImplElementary(ImplElementary obj, Void param) {
    evl.other.ImplElementary comp = new evl.other.ImplElementary(obj.getInfo(), obj.getName());
    map.put(obj, comp);
    for (FunctionHeader use : obj.getIface(Direction.out)) {
      comp.getOutput().add((FuncIfaceOut) visit(use, null));
    }
    for (FunctionHeader use : obj.getIface(Direction.in)) {
      comp.getInput().add((FuncIfaceIn) visit(use, null));
    }
    for (Constant var : obj.getConstant()) {
      comp.getConstant().add((evl.variable.Constant) visit(var, null));
    }
    for (Variable var : obj.getVariable()) {
      comp.getVariable().add((evl.variable.Variable) visit(var, null));
    }
    for (fun.variable.CompUse use : obj.getComponent()) {
      comp.getComponent().add((evl.other.CompUse) visit(use, null));
    }
    comp.setEntryFunc((Reference) visit(obj.getEntryFunc(), null));
    comp.setExitFunc((Reference) visit(obj.getExitFunc(), null));
    trfunc(new Designator(), obj.getFunction(), comp);
    return comp;
  }

  private void trfunc(Designator ns, ListOfNamed<Named> func, evl.other.ImplElementary comp) {
    for (Named item : func) {
      if (item instanceof Namespace) {
        trfunc(new Designator(ns, item.getName()), (Namespace) item, comp);
      } else {
        fun.function.FunctionHeader cfun = (fun.function.FunctionHeader) item;
        comp.addFunction(ns.toList(), (FunctionBase) visit(cfun, null));
      }
    }
  }

  @Override
  protected Evl visitImplHfsm(ImplHfsm obj, Void param) {
    evl.hfsm.ImplHfsm comp = new evl.hfsm.ImplHfsm(obj.getInfo(), obj.getName());
    map.put(obj, comp);
    for (FunctionHeader use : obj.getIface(Direction.out)) {
      comp.getOutput().add((FuncIfaceOut) visit(use, null));
    }
    for (FunctionHeader use : obj.getIface(Direction.in)) {
      comp.getInput().add((FuncIfaceIn) visit(use, null));
    }
    comp.setTopstate((evl.hfsm.StateComposite) visit(obj.getTopstate(), null));
    return comp;
  }

  @Override
  protected Evl visitImplComposition(ImplComposition obj, Void param) {
    evl.composition.ImplComposition comp = new evl.composition.ImplComposition(obj.getInfo(), obj.getName());
    map.put(obj, comp);
    for (FunctionHeader use : obj.getIface(Direction.out)) {
      comp.getOutput().add((FuncIfaceOut) visit(use, null));
    }
    for (FunctionHeader use : obj.getIface(Direction.in)) {
      comp.getInput().add((FuncIfaceIn) visit(use, null));
    }
    for (fun.variable.CompUse use : obj.getComponent()) {
      comp.getComponent().add((evl.other.CompUse) visit(use, null));
    }
    for (Connection con : obj.getConnection()) {
      comp.getConnection().add((evl.composition.Connection) visit(con, null));
    }
    return comp;
  }

  @Override
  protected Evl visitCaseOpt(CaseOpt obj, Void param) {
    List<CaseOptEntry> value = new ArrayList<CaseOptEntry>();
    for (fun.statement.CaseOptEntry entry : obj.getValue()) {
      value.add((CaseOptEntry) visit(entry, null));
    }
    return new evl.statement.CaseOpt(obj.getInfo(), value, (Block) visit(obj.getCode(), null));
  }

  // ----------------

  @Override
  protected Evl visitStateComposite(StateComposite obj, Void param) {
    evl.hfsm.StateComposite state = new evl.hfsm.StateComposite(obj.getInfo(), obj.getName());
    map.put(obj, state);

    for (Variable var : obj.getVariable()) {
      state.getVariable().add((evl.variable.Variable) traverse(var, null));
    }
    for (Named use : obj.getItemList()) {
      Evl evl = traverse(use, null);
      if (evl instanceof StateItem) {
        state.getItem().add((StateItem) evl);
      } else {
        assert (evl instanceof evl.function.FunctionHeader);
        state.getFunction().add((evl.function.FunctionHeader) evl);
      }
    }

    // it is here to break dependency cycle
    state.setEntryFunc((Reference) traverse(obj.getEntryFuncRef(), null));
    state.setExitFunc((Reference) traverse(obj.getExitFuncRef(), null));
    Reference initref = (Reference) traverse(obj.getInitial(), null);
    assert (initref.getOffset().isEmpty());
    state.setInitial((State) initref.getLink());

    return state;
  }

  @Override
  protected Evl visitStateSimple(StateSimple obj, Void param) {
    evl.hfsm.StateSimple state = new evl.hfsm.StateSimple(obj.getInfo(), obj.getName());
    map.put(obj, state);
    for (Variable var : obj.getVariable()) {
      state.getVariable().add((evl.variable.Variable) traverse(var, null));
    }
    for (Named use : obj.getItemList()) {
      Evl evl = traverse(use, null);
      if (evl instanceof StateItem) {
        state.getItem().add((StateItem) evl);
      } else {
        assert (evl instanceof evl.function.FunctionHeader);
        state.getFunction().add((evl.function.FunctionHeader) evl);
      }
    }

    // it is here to break dependency cycle
    state.setEntryFunc((Reference) traverse(obj.getEntryFuncRef(), null));
    state.setExitFunc((Reference) traverse(obj.getExitFuncRef(), null));

    return state;
  }

  @Override
  protected Evl visitTransition(Transition obj, Void param) {
    List<evl.variable.FuncVariable> args = new ArrayList<evl.variable.FuncVariable>(obj.getParam().size());
    for (fun.variable.FuncVariable itr : obj.getParam()) {
      args.add((evl.variable.FuncVariable) traverse(itr, null));
    }
    evl.expression.reference.Reference src = (evl.expression.reference.Reference) traverse(obj.getSrc(), null);
    evl.expression.reference.Reference dst = (evl.expression.reference.Reference) traverse(obj.getDst(), null);
    evl.expression.reference.Reference evt = (evl.expression.reference.Reference) traverse(obj.getEvent(), null);
    assert (src.getOffset().isEmpty());
    assert (dst.getOffset().isEmpty());
    assert (evt.getOffset().isEmpty());

    evl.expression.Expression guard = (evl.expression.Expression) traverse(obj.getGuard(), null);

    Block nbody = (Block) traverse(obj.getBody(), null);

    return new evl.hfsm.Transition(obj.getInfo(), obj.getName(), (State) src.getLink(), (State) dst.getLink(), evt, guard, args, nbody);
  }
}
