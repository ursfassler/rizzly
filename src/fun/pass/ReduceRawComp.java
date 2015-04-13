package fun.pass;

import java.util.HashMap;
import java.util.Map;

import pass.EvlPass;

import common.Designator;
import common.Direction;
import common.ElementInfo;

import error.ErrorType;
import error.RError;
import evl.copy.Relinker;
import evl.data.Evl;
import evl.data.EvlList;
import evl.data.Named;
import evl.data.Namespace;
import evl.data.component.Component;
import evl.data.component.composition.CompUse;
import evl.data.component.composition.Connection;
import evl.data.component.composition.Endpoint;
import evl.data.component.composition.EndpointRaw;
import evl.data.component.composition.EndpointSelf;
import evl.data.component.composition.EndpointSub;
import evl.data.expression.reference.RefName;
import evl.data.expression.reference.Reference;
import evl.data.expression.reference.SimpleRef;
import evl.data.function.Function;
import evl.data.function.InterfaceFunction;
import evl.data.function.header.FuncProcedure;
import evl.data.function.ret.FuncReturnNone;
import evl.data.type.Type;
import evl.data.variable.Constant;
import evl.data.variable.FuncVariable;
import evl.data.variable.Variable;
import evl.knowledge.KnowledgeBase;
import evl.traverser.DefTraverser;
import evl.traverser.NullTraverser;
import fun.other.RawComposition;
import fun.other.RawElementary;
import fun.other.RawHfsm;

public class ReduceRawComp extends EvlPass {
  @Override
  public void process(Namespace evl, KnowledgeBase kb) {
    ReduceRawCompWorker worker = new ReduceRawCompWorker();
    worker.traverse(evl, null);

    Relinker.relink(evl, worker.getMap());
  }
}

class ReduceRawCompWorker extends DefTraverser<Component, Void> {
  final private Map<Named, Named> map = new HashMap<Named, Named>();

  final static ReduceEndpoint endpointReduction = new ReduceEndpoint();

  @Override
  protected Component visitNamespace(Namespace obj, Void param) {
    for (int i = 0; i < obj.children.size(); i++) {
      Component newComp = visit(obj.children.get(i), param);
      if (newComp != null) {
        obj.children.set(i, newComp);
      }
    }
    return null;
  }

  @Override
  protected Component visitRawElementary(RawElementary obj, Void param) {
    ElementInfo info = obj.getInfo();
    FuncProcedure entryFunc = new FuncProcedure(info, "_entry", new EvlList<FuncVariable>(), new FuncReturnNone(info), obj.getEntryFunc());
    FuncProcedure exitFunc = new FuncProcedure(info, "_exit", new EvlList<FuncVariable>(), new FuncReturnNone(info), obj.getExitFunc());
    // if this makes problems like loops, convert the body of the functions
    // after the component

    evl.data.component.elementary.ImplElementary comp = new evl.data.component.elementary.ImplElementary(obj.getInfo(), obj.name, new SimpleRef<FuncProcedure>(info, entryFunc), new SimpleRef<FuncProcedure>(info, exitFunc));
    getMap().put(obj, comp);

    comp.function.add(entryFunc);
    comp.function.add(exitFunc);
    RError.ass(obj.getDeclaration().isEmpty(), obj.getInfo());

    for (Evl itr : obj.getIface()) {
      Evl evl = itr;
      comp.iface.add((InterfaceFunction) evl);
    }

    for (Evl itr : obj.getInstantiation()) {
      Evl ni = itr;
      if (ni instanceof Constant) {
        comp.constant.add((Constant) ni);
      } else if (ni instanceof Variable) {
        comp.variable.add((Variable) ni);
      } else if (ni instanceof Function) {
        comp.function.add((Function) ni);
      } else if (ni instanceof Type) {
        comp.type.add((Type) ni);
      } else {
        throw new RuntimeException("Not yet implemented: " + ni.getClass().getCanonicalName());
      }
    }

    return comp;
  }

  @Override
  protected Component visitRawHfsm(RawHfsm obj, Void param) {
    evl.data.component.hfsm.ImplHfsm comp = new evl.data.component.hfsm.ImplHfsm(obj.getInfo(), obj.name);
    getMap().put(obj, comp);

    for (Evl itr : obj.getIface()) {
      Evl evl = itr;
      comp.iface.add((InterfaceFunction) evl);
    }

    comp.topstate = obj.getTopstate();
    comp.topstate.name = Designator.NAME_SEP + "top";
    return comp;
  }

  @Override
  protected Component visitRawComposition(RawComposition obj, Void param) {
    evl.data.component.composition.ImplComposition comp = new evl.data.component.composition.ImplComposition(obj.getInfo(), obj.name);
    getMap().put(obj, comp);

    for (Evl itr : obj.getIface()) {
      Evl evl = itr;
      comp.iface.add((InterfaceFunction) evl);
    }

    for (Evl itr : obj.getInstantiation()) {
      Evl ni = itr;
      if (ni instanceof CompUse) {
        comp.component.add((CompUse) ni);
      } else if (ni instanceof InterfaceFunction) {
        comp.iface.add((InterfaceFunction) ni);
      } else {
        throw new RuntimeException("Not yet implemented: " + ni.getClass().getCanonicalName());
      }
    }

    for (Connection con : obj.getConnection()) {
      con.endpoint.put(Direction.in, convertEp(con.endpoint.get(Direction.in)));
      con.endpoint.put(Direction.out, convertEp(con.endpoint.get(Direction.out)));
      comp.connection.add(con);
    }
    return comp;
  }

  private Endpoint convertEp(Endpoint endpoint) {
    return endpointReduction.traverse(endpoint, null);
  }

  public Map<Named, Named> getMap() {
    return map;
  }
}

class ReduceEndpoint extends NullTraverser<Endpoint, Void> {
  @Override
  protected Endpoint visitDefault(Evl obj, Void param) {
    throw new RuntimeException("not yet implemented");
  }

  @Override
  protected Endpoint visitEndpointRaw(EndpointRaw obj, Void param) {
    Reference ref = obj.ref;

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
        return new EndpointSub(ref.getInfo(), new SimpleRef<CompUse>(obj.getInfo(), (CompUse) link), name);
      }
      default: {
        RError.err(ErrorType.Fatal, ref.getInfo(), "Unknown connection endpoint");
        return null;
      }
    }
  }
}
