package ast.pass.others;

import java.util.HashMap;
import java.util.Map;

import ast.Designator;
import ast.ElementInfo;
import ast.copy.Relinker;
import ast.data.Ast;
import ast.data.AstList;
import ast.data.Named;
import ast.data.Namespace;
import ast.data.component.Component;
import ast.data.component.composition.CompUse;
import ast.data.component.composition.Connection;
import ast.data.component.composition.Direction;
import ast.data.component.composition.Endpoint;
import ast.data.component.composition.EndpointRaw;
import ast.data.component.composition.EndpointSelf;
import ast.data.component.composition.EndpointSub;
import ast.data.expression.reference.RefName;
import ast.data.expression.reference.Reference;
import ast.data.expression.reference.SimpleRef;
import ast.data.function.Function;
import ast.data.function.InterfaceFunction;
import ast.data.function.header.FuncProcedure;
import ast.data.function.ret.FuncReturnNone;
import ast.data.raw.RawComposition;
import ast.data.raw.RawElementary;
import ast.data.raw.RawHfsm;
import ast.data.type.Type;
import ast.data.variable.Constant;
import ast.data.variable.FuncVariable;
import ast.data.variable.Variable;
import ast.knowledge.KnowledgeBase;
import ast.pass.AstPass;
import ast.traverser.DefTraverser;
import ast.traverser.NullTraverser;
import error.ErrorType;
import error.RError;

public class ReduceRawComp extends AstPass {
  @Override
  public void process(Namespace ast, KnowledgeBase kb) {
    ReduceRawCompWorker worker = new ReduceRawCompWorker();
    worker.traverse(ast, null);

    Relinker.relink(ast, worker.getMap());
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
    FuncProcedure entryFunc = new FuncProcedure(info, "_entry", new AstList<FuncVariable>(), new FuncReturnNone(info), obj.getEntryFunc());
    FuncProcedure exitFunc = new FuncProcedure(info, "_exit", new AstList<FuncVariable>(), new FuncReturnNone(info), obj.getExitFunc());
    // if this makes problems like loops, convert the body of the functions
    // after the component

    ast.data.component.elementary.ImplElementary comp = new ast.data.component.elementary.ImplElementary(obj.getInfo(), obj.name, new SimpleRef<FuncProcedure>(info, entryFunc), new SimpleRef<FuncProcedure>(info, exitFunc));
    getMap().put(obj, comp);

    comp.function.add(entryFunc);
    comp.function.add(exitFunc);
    RError.ass(obj.getDeclaration().isEmpty(), obj.getInfo());

    for (Ast itr : obj.getIface()) {
      Ast ast = itr;
      comp.iface.add((InterfaceFunction) ast);
    }

    for (Ast itr : obj.getInstantiation()) {
      Ast ni = itr;
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
    ast.data.component.hfsm.ImplHfsm comp = new ast.data.component.hfsm.ImplHfsm(obj.getInfo(), obj.name);
    getMap().put(obj, comp);

    for (Ast itr : obj.getIface()) {
      Ast ast = itr;
      comp.iface.add((InterfaceFunction) ast);
    }

    comp.topstate = obj.getTopstate();
    comp.topstate.name = Designator.NAME_SEP + "top";
    return comp;
  }

  @Override
  protected Component visitRawComposition(RawComposition obj, Void param) {
    ast.data.component.composition.ImplComposition comp = new ast.data.component.composition.ImplComposition(obj.getInfo(), obj.name);
    getMap().put(obj, comp);

    for (Ast itr : obj.getIface()) {
      Ast ast = itr;
      comp.iface.add((InterfaceFunction) ast);
    }

    for (Ast itr : obj.getInstantiation()) {
      Ast ni = itr;
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
  protected Endpoint visitDefault(Ast obj, Void param) {
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
