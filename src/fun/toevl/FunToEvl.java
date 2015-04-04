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

package fun.toevl;

import java.util.HashMap;
import java.util.Map;

import common.Designator;
import common.Direction;
import common.ElementInfo;

import error.ErrorType;
import error.RError;
import evl.data.Evl;
import evl.data.EvlList;
import evl.data.Named;
import evl.data.component.composition.CompUse;
import evl.data.component.composition.Endpoint;
import evl.data.component.composition.EndpointSelf;
import evl.data.component.composition.EndpointSub;
import evl.data.component.hfsm.State;
import evl.data.component.hfsm.StateItem;
import evl.data.expression.reference.RefName;
import evl.data.expression.reference.Reference;
import evl.data.expression.reference.SimpleRef;
import evl.data.function.Function;
import evl.data.function.InterfaceFunction;
import evl.data.function.header.FuncCtrlInDataIn;
import evl.data.function.header.FuncPrivateVoid;
import evl.data.statement.Block;
import evl.data.statement.CaseOptEntry;
import evl.data.type.Type;
import evl.data.type.special.VoidType;
import fun.Fun;
import fun.NullTraverser;
import fun.composition.Connection;
import fun.composition.ImplComposition;
import fun.expression.Expression;
import fun.expression.NamedValue;
import fun.expression.reference.DummyLinkTarget;
import fun.expression.reference.RefItem;
import fun.function.FuncHeader;
import fun.function.FuncImpl;
import fun.function.FuncReturnNone;
import fun.function.FuncReturnTuple;
import fun.function.FuncReturnType;
import fun.hfsm.ImplHfsm;
import fun.hfsm.StateComposite;
import fun.hfsm.StateContent;
import fun.hfsm.StateSimple;
import fun.hfsm.Transition;
import fun.other.ImplElementary;
import fun.other.Namespace;
import fun.statement.CaseOpt;
import fun.statement.Statement;
import fun.type.base.EnumElement;
import fun.type.composed.NamedElement;
import fun.variable.FuncVariable;
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

  public FunToEvl() {
    super();
    func = new FunToEvlFunc(this, map);
  }

  public Evl map(Fun fun) {
    return map.get(fun);
  }

  private VoidType getVoidType() {
    VoidType voidType = (VoidType) visit(fun.type.base.VoidType.INSTANCE, null);
    return voidType;
  }

  public static SimpleRef<Type> toTypeRef(Reference ref) {
    assert (ref.offset.isEmpty());
    assert (ref.link instanceof Type);
    SimpleRef<Type> typeRef = new SimpleRef<Type>(ref.getInfo(), (Type) ref.link);
    return typeRef;
  }

  public static <T extends Named> SimpleRef<T> toSimple(Reference ref) {
    assert (ref.offset.isEmpty());
    SimpleRef<T> typeRef = new SimpleRef<T>(ref.getInfo(), (T) ref.link);
    return typeRef;
  }

  @Override
  protected Evl visit(Fun obj, Void param) {
    Evl cobj = map.get(obj);
    if (cobj == null) {
      cobj = super.visit(obj, null);
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
  protected Evl visitDummyLinkTarget(DummyLinkTarget obj, Void param) {
    RError.err(ErrorType.Fatal, obj.getInfo(), "DummyLinkTarget should no longer exist");
    return null;
  }

  @Override
  protected Evl visitSimpleRef(fun.expression.reference.SimpleRef obj, Void param) {
    return new SimpleRef<Named>(obj.getInfo(), (Named) visit(obj.getLink(), param));
  }

  @Override
  protected Evl visitNamespace(Namespace obj, Void param) {
    evl.data.Namespace ret = new evl.data.Namespace(obj.getInfo(), obj.getName());
    map.put(obj, ret);
    for (Fun src : obj.getChildren()) {
      Evl ni = visit(src, null);
      ret.children.add(ni);
    }
    return ret;
  }

  @Override
  protected Evl visitType(fun.type.Type obj, Void param) {
    return type.traverse(obj, param);
  }

  @Override
  protected Evl visitExpression(Expression obj, Void param) {
    return expr.traverse(obj, null);
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
    return var.traverse(obj, param);
  }

  @Override
  protected Evl visitCaseOptEntry(fun.statement.CaseOptEntry obj, Void param) {
    return caoe.traverse(obj, null);
  }

  @Override
  protected Evl visitFuncImpl(FuncImpl obj, Void param) {
    return func.traverse(obj, param);
  }

  @Override
  protected Evl visitFunctionHeader(FuncHeader obj, Void param) {
    return func.traverse(obj, param);
  }

  @Override
  protected Evl visitNamedElement(NamedElement obj, Void param) {
    Reference ref = (Reference) traverse(obj.getType(), null);
    return new evl.data.type.composed.NamedElement(obj.getInfo(), obj.getName(), FunToEvl.toTypeRef(ref));
  }

  @Override
  protected Evl visitEnumElement(EnumElement obj, Void param) {
    return new evl.data.type.base.EnumElement(obj.getInfo(), obj.getName());
  }

  @Override
  protected Evl visitConnection(Connection obj, Void param) {
    Reference srcref = (Reference) traverse(obj.getEndpoint(Direction.in), null);
    Reference dstref = (Reference) traverse(obj.getEndpoint(Direction.out), null);
    switch (obj.getType()) {
      case async:
        return new evl.data.component.composition.AsynchroniusConnection(obj.getInfo(), refToEnp(srcref), refToEnp(dstref));
      case sync:
        return new evl.data.component.composition.SynchroniusConnection(obj.getInfo(), refToEnp(srcref), refToEnp(dstref));
      default:
        RError.err(ErrorType.Fatal, obj.getInfo(), "Unknown message type: " + obj.getType());
        return null;
    }
  }

  private Endpoint refToEnp(Reference ref) {
    switch (ref.offset.size()) {
      case 0: {
        Named link = ref.link;
        RError.ass(link instanceof Function, ref.getInfo(), "expected function for: " + link.name);
        return new EndpointSelf(ref.getInfo(), new SimpleRef<Function>(ref.getInfo(), (Function) link));
      }
      case 1: {
        Named link = ref.link;
        RError.ass(link instanceof CompUse, ref.getInfo(), "expected compuse for: " + link.name);
        String name = ((RefName) ref.offset.get(0)).name;
        return new EndpointSub(ref.getInfo(), (CompUse) link, name);
      }
      default: {
        RError.err(ErrorType.Fatal, ref.getInfo(), "Unknown connection endpoint");
        return null;
      }
    }
  }

  @Override
  protected Evl visitImplElementary(ImplElementary obj, Void param) {
    ElementInfo info = obj.getInfo();
    FuncPrivateVoid entryFunc = new FuncPrivateVoid(info, "_entry", new EvlList<evl.data.variable.FuncVariable>(), new evl.data.function.ret.FuncReturnNone(info), (Block) visit(obj.getEntryFunc(), null));
    FuncPrivateVoid exitFunc = new FuncPrivateVoid(info, "_exit", new EvlList<evl.data.variable.FuncVariable>(), new evl.data.function.ret.FuncReturnNone(info), (Block) visit(obj.getExitFunc(), null));
    // if this makes problems like loops, convert the body of the functions after the component

    evl.data.component.elementary.ImplElementary comp = new evl.data.component.elementary.ImplElementary(obj.getInfo(), obj.getName(), new SimpleRef<FuncPrivateVoid>(info, entryFunc), new SimpleRef<FuncPrivateVoid>(info, exitFunc));
    map.put(obj, comp);

    comp.function.add(entryFunc);
    comp.function.add(exitFunc);
    RError.ass(obj.getDeclaration().isEmpty(), obj.getInfo());

    for (Fun itr : obj.getIface()) {
      Evl evl = visit(itr, param);
      comp.iface.add((InterfaceFunction) evl);
    }

    for (Fun itr : obj.getInstantiation()) {
      Evl ni = visit(itr, null);
      if (ni instanceof evl.data.variable.Constant) {
        comp.constant.add((evl.data.variable.Constant) ni);
      } else if (ni instanceof evl.data.variable.Variable) {
        comp.variable.add((evl.data.variable.Variable) ni);
      } else if (ni instanceof Function) {
        comp.function.add((Function) ni);
      } else if (ni instanceof evl.data.type.Type) {
        comp.type.add((evl.data.type.Type) ni);
      } else {
        throw new RuntimeException("Not yet implemented: " + ni.getClass().getCanonicalName());
      }
    }

    return comp;
  }

  private evl.data.variable.FuncVariable mvr(String name) {
    VoidType voidType = getVoidType();
    return new evl.data.variable.FuncVariable(ElementInfo.NO, name, new SimpleRef<Type>(ElementInfo.NO, voidType));
  }

  @Override
  protected Evl visitImplHfsm(ImplHfsm obj, Void param) {
    evl.data.component.hfsm.ImplHfsm comp = new evl.data.component.hfsm.ImplHfsm(obj.getInfo(), obj.getName());
    map.put(obj, comp);

    for (Fun itr : obj.getIface()) {
      Evl evl = visit(itr, param);
      comp.iface.add((InterfaceFunction) evl);
    }

    comp.topstate = (evl.data.component.hfsm.StateComposite) visit(obj.getTopstate(), null);
    comp.topstate.name = Designator.NAME_SEP + "top";
    return comp;
  }

  @Override
  protected Evl visitImplComposition(ImplComposition obj, Void param) {
    evl.data.component.composition.ImplComposition comp = new evl.data.component.composition.ImplComposition(obj.getInfo(), obj.getName());
    map.put(obj, comp);

    for (Fun itr : obj.getIface()) {
      Evl evl = visit(itr, param);
      comp.iface.add((InterfaceFunction) evl);
    }

    for (Fun itr : obj.getInstantiation()) {
      Evl ni = visit(itr, null);
      if (ni instanceof CompUse) {
        comp.component.add((CompUse) ni);
      } else if (ni instanceof InterfaceFunction) {
        comp.iface.add((InterfaceFunction) ni);
      } else {
        throw new RuntimeException("Not yet implemented: " + ni.getClass().getCanonicalName());
      }
    }

    for (Connection con : obj.getConnection()) {
      comp.connection.add((evl.data.component.composition.Connection) visit(con, null));
    }
    return comp;
  }

  @Override
  protected Evl visitCaseOpt(CaseOpt obj, Void param) {
    EvlList<CaseOptEntry> value = new EvlList<CaseOptEntry>();
    for (fun.statement.CaseOptEntry entry : obj.getValue()) {
      value.add((CaseOptEntry) visit(entry, null));
    }
    return new evl.data.statement.CaseOpt(obj.getInfo(), value, (Block) visit(obj.getCode(), null));
  }

  // ----------------

  private void convertContent(fun.hfsm.State obj, evl.data.component.hfsm.State state) {
    for (StateContent use : obj.getItemList()) {
      Evl evl = traverse(use, null);
      if (evl instanceof StateItem) {
        state.item.add((StateItem) evl);
      } else {
        RError.err(ErrorType.Fatal, evl.getInfo(), "Unhandled state item: " + evl.getClass().getCanonicalName());
      }
    }
  }

  @Override
  protected Evl visitStateComposite(StateComposite obj, Void param) {
    ElementInfo info = obj.getInfo();

    SimpleRef<State> initref = toSimple((Reference) traverse(obj.getInitial(), null));

    FuncPrivateVoid entryFunc = new FuncPrivateVoid(info, "_entry", new EvlList<evl.data.variable.FuncVariable>(), new evl.data.function.ret.FuncReturnNone(info), new Block(info));
    FuncPrivateVoid exitFunc = new FuncPrivateVoid(info, "_exit", new EvlList<evl.data.variable.FuncVariable>(), new evl.data.function.ret.FuncReturnNone(info), new Block(info));

    evl.data.component.hfsm.StateComposite state = new evl.data.component.hfsm.StateComposite(obj.getInfo(), obj.getName(), new SimpleRef<FuncPrivateVoid>(info, entryFunc), new SimpleRef<FuncPrivateVoid>(info, exitFunc), initref);
    state.item.add(entryFunc);
    state.item.add(exitFunc);
    map.put(obj, state);

    convertContent(obj, state);

    // it is here to break dependency cycle
    entryFunc.body.statements.addAll(((Block) traverse(obj.getEntryFunc(), null)).statements);
    exitFunc.body.statements.addAll(((Block) traverse(obj.getExitFunc(), null)).statements);

    return state;
  }

  @Override
  protected Evl visitStateSimple(StateSimple obj, Void param) {
    ElementInfo info = obj.getInfo();
    FuncPrivateVoid entryFunc = new FuncPrivateVoid(info, "_entry", new EvlList<evl.data.variable.FuncVariable>(), new evl.data.function.ret.FuncReturnNone(info), new Block(info));
    FuncPrivateVoid exitFunc = new FuncPrivateVoid(info, "_exit", new EvlList<evl.data.variable.FuncVariable>(), new evl.data.function.ret.FuncReturnNone(info), new Block(info));

    evl.data.component.hfsm.StateSimple state = new evl.data.component.hfsm.StateSimple(obj.getInfo(), obj.getName(), new SimpleRef<FuncPrivateVoid>(info, entryFunc), new SimpleRef<FuncPrivateVoid>(info, exitFunc));
    state.item.add(entryFunc);
    state.item.add(exitFunc);
    map.put(obj, state);

    convertContent(obj, state);

    // it is here to break dependency cycle
    entryFunc.body.statements.addAll(((Block) traverse(obj.getEntryFunc(), null)).statements);
    exitFunc.body.statements.addAll(((Block) traverse(obj.getExitFunc(), null)).statements);

    return state;
  }

  @Override
  protected Evl visitTransition(Transition obj, Void param) {
    EvlList<evl.data.variable.FuncVariable> args = new EvlList<evl.data.variable.FuncVariable>();
    for (FuncVariable itr : obj.getParam()) {
      evl.data.variable.FuncVariable var = (evl.data.variable.FuncVariable) traverse(itr, null);
      args.add(var);
    }
    SimpleRef<State> src = toSimple((evl.data.expression.reference.Reference) traverse(obj.getSrc(), null));
    SimpleRef<State> dst = toSimple((evl.data.expression.reference.Reference) traverse(obj.getDst(), null));
    SimpleRef<FuncCtrlInDataIn> evt = toSimple((evl.data.expression.reference.Reference) traverse(obj.getEvent(), null));

    evl.data.expression.Expression guard = (evl.data.expression.Expression) traverse(obj.getGuard(), null);

    Block nbody = (Block) traverse(obj.getBody(), null);

    return new evl.data.component.hfsm.Transition(obj.getInfo(), src, dst, evt, guard, args, nbody);
  }

  @Override
  protected Evl visitFuncReturnTuple(FuncReturnTuple obj, Void param) {
    EvlList<evl.data.variable.FuncVariable> arg = new EvlList<evl.data.variable.FuncVariable>();
    for (FuncVariable itr : obj.getParam()) {
      evl.data.variable.FuncVariable var = (evl.data.variable.FuncVariable) traverse(itr, null);
      arg.add(var);
    }
    return new evl.data.function.ret.FuncReturnTuple(obj.getInfo(), arg);
  }

  @Override
  protected Evl visitFuncReturnType(FuncReturnType obj, Void param) {
    Type nt = (Type) traverse(FunToEvl.getRefType(obj.getType()), null);
    return new evl.data.function.ret.FuncReturnType(obj.getInfo(), new SimpleRef<Type>(obj.getInfo(), nt));
  }

  @Override
  protected Evl visitFuncReturnNone(FuncReturnNone obj, Void param) {
    return new evl.data.function.ret.FuncReturnNone(obj.getInfo());
  }

  @Override
  protected Evl visitNamedValue(NamedValue obj, Void param) {
    return new evl.data.expression.NamedValue(obj.getInfo(), obj.getName(), (evl.data.expression.Expression) traverse(obj.getValue(), null));
  }

  static public fun.type.Type getRefType(Fun type) {
    RError.ass(type instanceof fun.expression.reference.Reference, type.getInfo(), "Expected reference, got: " + type.getClass().getName());
    fun.expression.reference.Reference ref = (fun.expression.reference.Reference) type;
    if (ref.getLink() instanceof fun.type.Type) {
      if (!ref.getOffset().isEmpty()) {
        RError.err(ErrorType.Error, ref.getOffset().get(0).getInfo(), "Type reference should not have offset anymore");
      }
      return (fun.type.Type) ref.getLink();
    } else {
      RError.err(ErrorType.Hint, ref.getLink().getInfo(), "Definition was here");
      RError.err(ErrorType.Error, ref.getInfo(), "Need type");
      return null;
    }
  }

}
