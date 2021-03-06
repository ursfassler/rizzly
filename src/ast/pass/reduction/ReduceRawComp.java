package ast.pass.reduction;

import java.util.HashMap;
import java.util.Map;

import ast.Designator;
import ast.copy.Relinker;
import ast.data.Ast;
import ast.data.AstList;
import ast.data.Named;
import ast.data.Namespace;
import ast.data.component.Component;
import ast.data.component.composition.ComponentUse;
import ast.data.component.composition.Connection;
import ast.data.component.composition.Endpoint;
import ast.data.component.composition.EndpointRaw;
import ast.data.component.composition.EndpointSelf;
import ast.data.component.composition.EndpointSub;
import ast.data.component.composition.ImplComposition;
import ast.data.component.elementary.ImplElementary;
import ast.data.component.hfsm.ImplHfsm;
import ast.data.function.FuncRefFactory;
import ast.data.function.Function;
import ast.data.function.InterfaceFunction;
import ast.data.function.header.Procedure;
import ast.data.function.ret.FuncReturnNone;
import ast.data.raw.RawComposition;
import ast.data.raw.RawElementary;
import ast.data.raw.RawHfsm;
import ast.data.reference.LinkedAnchor;
import ast.data.reference.OffsetReference;
import ast.data.reference.RefFactory;
import ast.data.reference.RefName;
import ast.data.type.Type;
import ast.data.variable.Constant;
import ast.data.variable.FunctionVariable;
import ast.data.variable.Variable;
import ast.dispatcher.DfsTraverser;
import ast.dispatcher.NullDispatcher;
import ast.knowledge.KnowledgeBase;
import ast.meta.MetaList;
import ast.pass.AstPass;
import error.ErrorType;
import error.RError;

public class ReduceRawComp implements AstPass {

  @Override
  public void process(Namespace ast, KnowledgeBase kb) {
    ReduceRawCompWorker worker = new ReduceRawCompWorker();
    worker.traverse(ast, null);

    Relinker.relink(ast, worker.getMap());
  }
}

class ReduceRawCompWorker extends DfsTraverser<Component, Void> {
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
    MetaList info = obj.metadata();// TODO use info for everything
    Procedure entryFunc = new Procedure("_entry", new AstList<FunctionVariable>(), new FuncReturnNone(), obj.getEntryFunc());
    Procedure exitFunc = new Procedure("_exit", new AstList<FunctionVariable>(), new FuncReturnNone(), obj.getExitFunc());
    // if this makes problems like loops, convert the body of the functions
    // after the component

    ImplElementary comp = new ImplElementary(obj.getName(), FuncRefFactory.create(entryFunc), FuncRefFactory.create(exitFunc));
    comp.metadata().add(obj.metadata());
    getMap().put(obj, comp);

    comp.function.add(entryFunc);
    comp.function.add(exitFunc);
    RError.ass(obj.getDeclaration().isEmpty(), obj.metadata(), "declaration should be empty");

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
    ImplHfsm comp = new ImplHfsm(obj.getName());
    comp.metadata().add(obj.metadata());
    getMap().put(obj, comp);

    for (Ast itr : obj.getIface()) {
      Ast ast = itr;
      comp.iface.add((InterfaceFunction) ast);
    }

    comp.topstate = obj.getTopstate();
    comp.topstate.setName(Designator.NAME_SEP + "top");
    return comp;
  }

  @Override
  protected Component visitRawComposition(RawComposition obj, Void param) {
    ImplComposition comp = new ImplComposition(obj.getName());
    comp.metadata().add(obj.metadata());
    getMap().put(obj, comp);

    for (Ast itr : obj.getIface()) {
      Ast ast = itr;
      comp.iface.add((InterfaceFunction) ast);
    }

    for (Ast itr : obj.getInstantiation()) {
      Ast ni = itr;
      if (ni instanceof ComponentUse) {
        comp.component.add((ComponentUse) ni);
      } else if (ni instanceof InterfaceFunction) {
        comp.iface.add((InterfaceFunction) ni);
      } else {
        throw new RuntimeException("Not yet implemented: " + ni.getClass().getCanonicalName());
      }
    }

    for (Connection con : obj.getConnection()) {
      con.setSrc(convertEp(con.getSrc()));
      con.setDst(convertEp(con.getDst()));
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

class ReduceEndpoint extends NullDispatcher<Endpoint, Void> {
  @Override
  protected Endpoint visitDefault(Ast obj, Void param) {
    throw new RuntimeException("not yet implemented");
  }

  @Override
  protected Endpoint visitEndpointRaw(EndpointRaw obj, Void param) {
    OffsetReference ref = (OffsetReference) obj.getRef();

    switch (ref.getOffset().size()) {
      case 0: {
        Named link = ((LinkedAnchor) ref.getAnchor()).getLink();
        RError.ass(link instanceof Function, ref.metadata(), "expected function for: " + link.getName());
        return new EndpointSelf(ref.metadata(), FuncRefFactory.create(ref.metadata(), (Function) link));
      }
      case 1: {
        Named link = ((LinkedAnchor) ref.getAnchor()).getLink();
        RError.ass(link instanceof ComponentUse, ref.metadata(), "expected compuse for: " + link.getName());
        String name = ((RefName) ref.getOffset().get(0)).name;
        return new EndpointSub(ref.metadata(), RefFactory.create(obj.metadata(), link), name);
      }
      default: {
        RError.err(ErrorType.Fatal, "Unknown connection endpoint", ref.metadata());
        return null;
      }
    }
  }
}
