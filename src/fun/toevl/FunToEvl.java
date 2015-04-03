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
import evl.Evl;
import evl.composition.Endpoint;
import evl.composition.EndpointSelf;
import evl.composition.EndpointSub;
import evl.expression.reference.RefName;
import evl.expression.reference.Reference;
import evl.expression.reference.SimpleRef;
import evl.function.Function;
import evl.function.InterfaceFunction;
import evl.function.header.FuncCtrlInDataIn;
import evl.function.header.FuncPrivateVoid;
import evl.hfsm.State;
import evl.hfsm.StateItem;
import evl.other.CompUse;
import evl.other.EvlList;
import evl.other.Named;
import evl.statement.Block;
import evl.statement.CaseOptEntry;
import evl.type.Type;
import evl.type.special.VoidType;
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
    assert (ref.getOffset().isEmpty());
    assert (ref.getLink() instanceof Type);
    SimpleRef<Type> typeRef = new SimpleRef<Type>(ref.getInfo(), (Type) ref.getLink());
    return typeRef;
  }

  public static <T extends Named> SimpleRef<T> toSimple(Reference ref) {
    assert (ref.getOffset().isEmpty());
    SimpleRef<T> typeRef = new SimpleRef<T>(ref.getInfo(), (T) ref.getLink());
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
    evl.other.Namespace ret = new evl.other.Namespace(obj.getInfo(), obj.getName());
    map.put(obj, ret);
    for (Fun src : obj.getChildren()) {
      Evl ni = visit(src, null);
      ret.getChildren().add(ni);
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
    return new evl.type.composed.NamedElement(obj.getInfo(), obj.getName(), FunToEvl.toTypeRef(ref));
  }

  @Override
  protected Evl visitEnumElement(EnumElement obj, Void param) {
    return new evl.type.base.EnumElement(obj.getInfo(), obj.getName());
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
        Named link = ref.getLink();
        RError.ass(link instanceof Function, ref.getInfo(), "expected function for: " + link.getName());
        return new EndpointSelf(ref.getInfo(), (Function) link);
      }
      case 1: {
        Named link = ref.getLink();
        RError.ass(link instanceof CompUse, ref.getInfo(), "expected compuse for: " + link.getName());
        String name = ((RefName) ref.getOffset().get(0)).getName();
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
    FuncPrivateVoid entryFunc = new FuncPrivateVoid(info, "_entry", new EvlList<evl.variable.FuncVariable>(), new evl.function.ret.FuncReturnNone(info), (Block) visit(obj.getEntryFunc(), null));
    FuncPrivateVoid exitFunc = new FuncPrivateVoid(info, "_exit", new EvlList<evl.variable.FuncVariable>(), new evl.function.ret.FuncReturnNone(info), (Block) visit(obj.getExitFunc(), null));
    // if this makes problems like loops, convert the body of the functions after the component

    evl.other.ImplElementary comp = new evl.other.ImplElementary(obj.getInfo(), obj.getName(), new SimpleRef<FuncPrivateVoid>(info, entryFunc), new SimpleRef<FuncPrivateVoid>(info, exitFunc));
    map.put(obj, comp);

    comp.getFunction().add(entryFunc);
    comp.getFunction().add(exitFunc);
    RError.ass(obj.getDeclaration().isEmpty(), obj.getInfo());

    for (Fun itr : obj.getIface()) {
      Evl evl = visit(itr, param);
      comp.getIface().add((InterfaceFunction) evl);
    }

    for (Fun itr : obj.getInstantiation()) {
      Evl ni = visit(itr, null);
      if (ni instanceof evl.variable.Constant) {
        comp.getConstant().add((evl.variable.Constant) ni);
      } else if (ni instanceof evl.variable.Variable) {
        comp.getVariable().add((evl.variable.Variable) ni);
      } else if (ni instanceof Function) {
        comp.getFunction().add((Function) ni);
      } else if (ni instanceof evl.type.Type) {
        comp.getType().add((evl.type.Type) ni);
      } else {
        throw new RuntimeException("Not yet implemented: " + ni.getClass().getCanonicalName());
      }
    }

    return comp;
  }

  private evl.variable.FuncVariable mvr(String name) {
    VoidType voidType = getVoidType();
    return new evl.variable.FuncVariable(ElementInfo.NO, name, new SimpleRef<Type>(ElementInfo.NO, voidType));
  }

  @Override
  protected Evl visitImplHfsm(ImplHfsm obj, Void param) {
    evl.hfsm.ImplHfsm comp = new evl.hfsm.ImplHfsm(obj.getInfo(), obj.getName());
    map.put(obj, comp);

    for (Fun itr : obj.getIface()) {
      Evl evl = visit(itr, param);
      comp.getIface().add((InterfaceFunction) evl);
    }

    comp.setTopstate((evl.hfsm.StateComposite) visit(obj.getTopstate(), null));
    comp.getTopstate().setName(Designator.NAME_SEP + "top");
    return comp;
  }

  @Override
  protected Evl visitImplComposition(ImplComposition obj, Void param) {
    evl.composition.ImplComposition comp = new evl.composition.ImplComposition(obj.getInfo(), obj.getName());
    map.put(obj, comp);

    for (Fun itr : obj.getIface()) {
      Evl evl = visit(itr, param);
      comp.getIface().add((InterfaceFunction) evl);
    }

    for (Fun itr : obj.getInstantiation()) {
      Evl ni = visit(itr, null);
      if (ni instanceof CompUse) {
        comp.getComponent().add((CompUse) ni);
      } else if (ni instanceof InterfaceFunction) {
        comp.getIface().add((InterfaceFunction) ni);
      } else {
        throw new RuntimeException("Not yet implemented: " + ni.getClass().getCanonicalName());
      }
    }

    for (Connection con : obj.getConnection()) {
      comp.getConnection().add((evl.composition.Connection) visit(con, null));
    }
    return comp;
  }

  @Override
  protected Evl visitCaseOpt(CaseOpt obj, Void param) {
    EvlList<CaseOptEntry> value = new EvlList<CaseOptEntry>();
    for (fun.statement.CaseOptEntry entry : obj.getValue()) {
      value.add((CaseOptEntry) visit(entry, null));
    }
    return new evl.statement.CaseOpt(obj.getInfo(), value, (Block) visit(obj.getCode(), null));
  }

  // ----------------

  private void convertContent(fun.hfsm.State obj, evl.hfsm.State state) {
    for (StateContent use : obj.getItemList()) {
      Evl evl = traverse(use, null);
      if (evl instanceof StateItem) {
        state.getItem().add((StateItem) evl);
      } else {
        RError.err(ErrorType.Fatal, evl.getInfo(), "Unhandled state item: " + evl.getClass().getCanonicalName());
      }
    }
  }

  @Override
  protected Evl visitStateComposite(StateComposite obj, Void param) {
    ElementInfo info = obj.getInfo();
    
    SimpleRef<State> initref = toSimple((Reference) traverse(obj.getInitial(), null));

    FuncPrivateVoid entryFunc = new FuncPrivateVoid(info, "_entry", new EvlList<evl.variable.FuncVariable>(), new evl.function.ret.FuncReturnNone(info), new Block(info));
    FuncPrivateVoid exitFunc = new FuncPrivateVoid(info, "_exit", new EvlList<evl.variable.FuncVariable>(), new evl.function.ret.FuncReturnNone(info), new Block(info));

    evl.hfsm.StateComposite state = new evl.hfsm.StateComposite(obj.getInfo(), obj.getName(), new SimpleRef<FuncPrivateVoid>(info, entryFunc), new SimpleRef<FuncPrivateVoid>(info, exitFunc), initref);
    state.getItem().add(entryFunc);
    state.getItem().add(exitFunc);
    map.put(obj, state);

    convertContent(obj, state);

    // it is here to break dependency cycle
    entryFunc.getBody().getStatements().addAll(((Block) traverse(obj.getEntryFunc(), null)).getStatements());
    exitFunc.getBody().getStatements().addAll(((Block) traverse(obj.getExitFunc(), null)).getStatements());

    return state;
  }

  @Override
  protected Evl visitStateSimple(StateSimple obj, Void param) {
    ElementInfo info = obj.getInfo();
    FuncPrivateVoid entryFunc = new FuncPrivateVoid(info, "_entry", new EvlList<evl.variable.FuncVariable>(), new evl.function.ret.FuncReturnNone(info), new Block(info));
    FuncPrivateVoid exitFunc = new FuncPrivateVoid(info, "_exit", new EvlList<evl.variable.FuncVariable>(), new evl.function.ret.FuncReturnNone(info), new Block(info));

    evl.hfsm.StateSimple state = new evl.hfsm.StateSimple(obj.getInfo(), obj.getName(), new SimpleRef<FuncPrivateVoid>(info, entryFunc), new SimpleRef<FuncPrivateVoid>(info, exitFunc));
    state.getItem().add(entryFunc);
    state.getItem().add(exitFunc);
    map.put(obj, state);

    convertContent(obj, state);

    // it is here to break dependency cycle
    entryFunc.getBody().getStatements().addAll(((Block) traverse(obj.getEntryFunc(), null)).getStatements());
    exitFunc.getBody().getStatements().addAll(((Block) traverse(obj.getExitFunc(), null)).getStatements());

    return state;
  }

  @Override
  protected Evl visitTransition(Transition obj, Void param) {
    EvlList<evl.variable.FuncVariable> args = new EvlList<evl.variable.FuncVariable>();
    for (FuncVariable itr : obj.getParam()) {
      evl.variable.FuncVariable var = (evl.variable.FuncVariable) traverse(itr, null);
      args.add(var);
    }
    SimpleRef<State> src = toSimple((evl.expression.reference.Reference) traverse(obj.getSrc(), null));
    SimpleRef<State> dst = toSimple((evl.expression.reference.Reference) traverse(obj.getDst(), null));
    SimpleRef<FuncCtrlInDataIn> evt = toSimple((evl.expression.reference.Reference) traverse(obj.getEvent(), null));

    evl.expression.Expression guard = (evl.expression.Expression) traverse(obj.getGuard(), null);

    Block nbody = (Block) traverse(obj.getBody(), null);

    return new evl.hfsm.Transition(obj.getInfo(), src, dst, evt, guard, args, nbody);
  }

  @Override
  protected Evl visitFuncReturnTuple(FuncReturnTuple obj, Void param) {
    EvlList<evl.variable.FuncVariable> arg = new EvlList<evl.variable.FuncVariable>();
    for (FuncVariable itr : obj.getParam()) {
      evl.variable.FuncVariable var = (evl.variable.FuncVariable) traverse(itr, null);
      arg.add(var);
    }
    return new evl.function.ret.FuncReturnTuple(obj.getInfo(), arg);
  }

  @Override
  protected Evl visitFuncReturnType(FuncReturnType obj, Void param) {
    Type nt = (Type) traverse(FunToEvl.getRefType(obj.getType()), null);
    return new evl.function.ret.FuncReturnType(obj.getInfo(), new SimpleRef<Type>(obj.getInfo(), nt));
  }

  @Override
  protected Evl visitFuncReturnNone(FuncReturnNone obj, Void param) {
    return new evl.function.ret.FuncReturnNone(obj.getInfo());
  }

  @Override
  protected Evl visitNamedValue(NamedValue obj, Void param) {
    return new evl.expression.NamedValue(obj.getInfo(), obj.getName(), (evl.expression.Expression) traverse(obj.getValue(), null));
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
