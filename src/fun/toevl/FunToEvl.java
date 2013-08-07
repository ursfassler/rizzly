package fun.toevl;

import java.util.HashMap;
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
import evl.function.FunctionBase;
import evl.other.CompUse;
import evl.other.IfaceUse;
import evl.type.Type;
import fun.Fun;
import fun.NullTraverser;
import fun.composition.Connection;
import fun.composition.ImplComposition;
import fun.expression.Expression;
import fun.expression.reference.RefItem;
import fun.function.FunctionHeader;
import fun.generator.TypeGenerator;
import fun.hfsm.ImplHfsm;
import fun.hfsm.StateItem;
import fun.other.ImplElementary;
import fun.other.Interface;
import fun.other.ListOfNamed;
import fun.other.Named;
import fun.other.NamedComponent;
import fun.other.NamedInterface;
import fun.other.Namespace;
import fun.statement.Statement;
import fun.type.NamedType;
import fun.type.base.EnumElement;
import fun.type.composed.NamedElement;
import fun.variable.Constant;
import fun.variable.Variable;

public class FunToEvl extends NullTraverser<Evl, String> {
  private Map<Fun, Evl> map = new HashMap<Fun, Evl>();
  private FunToEvlType type = new FunToEvlType(this, map);
  private FunToEvlExpr expr = new FunToEvlExpr(this, map);
  private FunToEvlRef ref = new FunToEvlRef(this, map);
  private FunToEvlFunc func;
  private FunToEvlStmt stmt = new FunToEvlStmt(this, map);
  private FunToEvlVariable var = new FunToEvlVariable(this, map);
  private FunToEvlStateItem state = new FunToEvlStateItem(this, map);
  private FunToEvlCaseOptEntry caoe = new FunToEvlCaseOptEntry(this, map);

  public FunToEvl(Map<FunctionHeader, Class<? extends FunctionBase>> funType) {
    super();
    func = new FunToEvlFunc(this, map, funType);
  }

  public static evl.other.Namespace process(Namespace classes, String debugdir) {
    Map<FunctionHeader, Class<? extends FunctionBase>> funType = FuncTypeCollector.process(classes);
    FunToEvl funToAst = new FunToEvl(funType);
    return (evl.other.Namespace) funToAst.traverse(classes, null);
  }

  @Override
  protected Evl visit(Fun obj, String param) {
    Evl cobj = map.get(obj);
    if (cobj == null) {
      cobj = super.visit(obj, param);
      assert (cobj != null);
      map.put(obj, cobj);
    }
    return cobj;
  }

  @Override
  protected Evl visitDefault(Fun obj, String param) {
    throw new RuntimeException("not yet implemented: " + obj.getClass().getCanonicalName());
  }

  @Override
  protected Evl visitNamespace(Namespace obj, String param) {
    evl.other.Namespace ret = new evl.other.Namespace(obj.getInfo(), obj.getName());
    map.put(obj, ret);
    for (Named src : obj) {
      evl.other.Named ni = (evl.other.Named) visit(src, param);
      ret.add(ni);
    }
    return ret;
  }

  @Override
  protected Evl visitNamedType(NamedType obj, String param) {
    return type.traverse(obj, obj.getName());
  }

  @Override
  protected Evl visitTypeGenerator(TypeGenerator obj, String param) {
    RError.err(ErrorType.Fatal, obj.getInfo(), "unresolved functional type: " + obj);
    return null;
  }

  @Override
  protected Evl visitExpression(Expression obj, String param) {
    return expr.traverse(obj, param);
  }

  @Override
  protected Evl visitRefItem(RefItem obj, String param) {
    return ref.traverse(obj, null);
  }

  @Override
  protected Evl visitStatement(Statement obj, String param) {
    return stmt.traverse(obj, null);
  }

  @Override
  protected Evl visitVariable(Variable obj, String param) {
    return var.traverse(obj, null);
  }

  @Override
  protected Evl visitStateItem(StateItem obj, String param) {
    return state.traverse(obj, null);
  }

  @Override
  protected Evl visitCaseOptEntry(fun.statement.CaseOptEntry obj, String param) {
    return caoe.traverse(obj, null);
  }

  @Override
  protected Evl visitFunctionHeader(FunctionHeader obj, String param) {
    return func.traverse(obj, null);
  }

  @Override
  protected Evl visitEnumElement(EnumElement obj, String param) {
    return new evl.type.base.EnumElement(obj.getInfo(), obj.getName());
  }

  @Override
  protected Evl visitNamedElement(NamedElement obj, String param) {
    Reference ref = (Reference) traverse(obj.getType(), null);
    assert (ref.getOffset().isEmpty());
    return new evl.type.composed.NamedElement(obj.getInfo(), obj.getName(), (Type) ref.getLink());
  }

  @Override
  protected Evl visitConnection(Connection obj, String param) {
    Reference srcref = (Reference) traverse(obj.getEndpoint(Direction.in), null);
    Reference dstref = (Reference) traverse(obj.getEndpoint(Direction.out), null);
    return new evl.composition.Connection(obj.getInfo(), refToEnp(srcref), refToEnp(dstref), obj.getType());
  }

  private Endpoint refToEnp(Reference ref) {
    switch (ref.getOffset().size()) {
    case 0: {
      return new EndpointSelf(ref.getInfo(), (IfaceUse) ref.getLink());
    }
    case 1: {
      String iface = ((RefName) ref.getOffset().get(0)).getName();
      return new EndpointSub(ref.getInfo(), (CompUse) ref.getLink(), iface);
    }
    default: {
      RError.err(ErrorType.Fatal, ref.getInfo(), "Unknown connection endpoint");
      return null;
    }
    }
  }

  @Override
  protected Evl visitNamedComponent(NamedComponent obj, String param) {
    return visit(obj.getComp(), obj.getName());
  }

  @Override
  protected Evl visitImplElementary(ImplElementary obj, String param) {
    evl.other.ImplElementary comp = new evl.other.ImplElementary(obj.getInfo(), param);
    map.put(obj, comp);
    for (fun.variable.IfaceUse use : obj.getIface(Direction.out)) {
      comp.getIface(Direction.out).add((evl.other.IfaceUse) visit(use, null));
    }
    for (fun.variable.IfaceUse use : obj.getIface(Direction.in)) {
      comp.getIface(Direction.in).add((evl.other.IfaceUse) visit(use, null));
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
  protected Evl visitImplHfsm(ImplHfsm obj, String param) {
    evl.hfsm.ImplHfsm comp = new evl.hfsm.ImplHfsm(obj.getInfo(), param);
    map.put(obj, comp);
    for (fun.variable.IfaceUse use : obj.getIface(Direction.out)) {
      comp.getIface(Direction.out).add((evl.other.IfaceUse) visit(use, null));
    }
    for (fun.variable.IfaceUse use : obj.getIface(Direction.in)) {
      comp.getIface(Direction.in).add((evl.other.IfaceUse) visit(use, null));
    }
    comp.setTopstate((evl.hfsm.StateComposite) visit(obj.getTopstate(), null));
    return comp;
  }

  @Override
  protected Evl visitImplComposition(ImplComposition obj, String param) {
    evl.composition.ImplComposition comp = new evl.composition.ImplComposition(obj.getInfo(), param);
    map.put(obj, comp);
    for (fun.variable.IfaceUse use : obj.getIface(Direction.out)) {
      comp.getIface(Direction.out).add((evl.other.IfaceUse) visit(use, null));
    }
    for (fun.variable.IfaceUse use : obj.getIface(Direction.in)) {
      comp.getIface(Direction.in).add((evl.other.IfaceUse) visit(use, null));
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
  protected Evl visitNamedInterface(NamedInterface obj, String param) {
    return visit(obj.getIface(), obj.getName());
  }

  @Override
  protected Evl visitInterface(Interface obj, String param) {
    assert (param != null);
    evl.other.Interface ret = new evl.other.Interface(obj.getInfo(), param);
    for (FunctionHeader func : obj.getPrototype()) {
      ret.getPrototype().add((evl.function.FunctionBase) visit(func, null));
    }
    return ret;
  }
}
