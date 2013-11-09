package fun.doc;

import java.util.HashSet;
import java.util.Set;

import util.SimpleGraph;

import common.Scope;

import error.ErrorType;
import error.RError;
import fun.DefTraverser;
import fun.Fun;
import fun.NullTraverser;
import fun.expression.reference.Reference;
import fun.function.FunctionHeader;
import fun.knowledge.KnowScope;
import fun.other.Component;
import fun.other.Named;
import fun.other.Namespace;
import fun.type.Type;
import fun.variable.Constant;

public class DepGraph extends NullTraverser<Void, Void> {
  private SubDep dep;

  public DepGraph() {
    super();
    dep = new SubDep(new SimpleGraph<Named>());
  }

  static public SimpleGraph<Named> build(Namespace ns) {
    DepGraph depGraph = new DepGraph();
    depGraph.traverse(ns, null);
    return depGraph.dep.getGraph();
  }

  static public SimpleGraph<Named> build(Named root) {
    DepGraph depGraph = new DepGraph();
    depGraph.traverse(root, null);
    return depGraph.dep.getGraph();
  }

  @Override
  protected Void visitDefault(Fun obj, Void param) {
    throw new RuntimeException("not yet implemented: " + obj.getClass().getCanonicalName());
  }

  @Override
  protected Void visitNamespace(Namespace obj, Void param) {
    visitItr(obj, param);
    return null;
  }

  @Override
  protected Void visitType(Type obj, Void param) {
    dep.traverse(obj, obj);
    return null;
  }

  @Override
  protected Void visitConstant(Constant obj, Void param) {
    dep.traverse(obj, obj);
    return null;
  }

  @Override
  protected Void visitFunctionHeader(FunctionHeader obj, Void param) {
    dep.traverse(obj, obj);
    return null;
  }

  @Override
  protected Void visitComponent(Component obj, Void param) {
    dep.traverse(obj, obj);
    return null;
  }

}

class SubDep extends DefTraverser<Void, Named> {
  private SimpleGraph<Named> g;
  private Set<Named> traversed = new HashSet<Named>();

  public SubDep(SimpleGraph<Named> simpleGraph) {
    super();
    this.g = simpleGraph;
  }

  public SimpleGraph<Named> getGraph() {
    return g;
  }

  @Override
  protected Void visit(Fun obj, Named param) {
    if (obj instanceof Named) {
      if (traversed.contains(obj)) {
        return null;
      }
      traversed.add((Named) obj);
      g.addVertex((Named) obj); // otherwise an element with no dependency is not added
    }
    return super.visit(obj, param);
  }

  @Override
  protected Void visitReference(Reference obj, Named param) {
    assert (param != null);
    super.visitReference(obj, param);
    Fun dst = obj.getLink();
    Scope scope = KnowScope.get(dst);
    switch (scope) {
    case global:
      g.addVertex(obj.getLink());
      g.addEdge(param, obj.getLink());
      break;
    case local:
    case privat:
      break;
    default:
      RError.err(ErrorType.Fatal, obj.getInfo(), "Unhandled scope: " + scope);
      break;
    }

    visit(obj.getLink(), obj.getLink());

    return null;
  }

}
