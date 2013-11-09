package evl.traverser;

import java.util.Set;

import util.SimpleGraph;

import common.Scope;

import error.ErrorType;
import error.RError;
import evl.DefTraverser;
import evl.Evl;
import evl.NullTraverser;
import evl.expression.reference.Reference;
import evl.function.FunctionBase;
import evl.knowledge.KnowScope;
import evl.other.CompUse;
import evl.other.Named;
import evl.other.Namespace;
import evl.type.TypeRef;
import evl.type.base.EnumDefRef;
import evl.type.base.EnumElement;
import evl.type.base.FunctionTypeRet;
import evl.type.base.FunctionTypeVoid;
import evl.type.composed.NamedElement;
import evl.variable.Constant;
import evl.variable.Variable;

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

  public static SimpleGraph<Named> build(Set<? extends Named> roots) {
    DepGraph depGraph = new DepGraph();
    for (Named itr : roots) {
      depGraph.traverse(itr, null);
    }
    assert (depGraph.dep.getGraph().vertexSet().containsAll(roots));
    return depGraph.dep.getGraph();
  }

  @Override
  protected Void visitDefault(Evl obj, Void param) {
    throw new RuntimeException("not yet implemented: " + obj.getClass().getCanonicalName());
  }

  @Override
  protected Void visitNamespace(Namespace obj, Void param) {
    visitItr(obj, param);
    return null;
  }

  @Override
  protected Void visitConstant(Constant obj, Void param) {
    dep.traverse(obj, obj);
    return null;
  }

  @Override
  protected Void visitFunctionBase(FunctionBase obj, Void param) {
    dep.traverse(obj, obj);
    return null;
  }

}

class SubDep extends DefTraverser<Void, Named> {
  private SimpleGraph<Named> g;

  public SubDep(SimpleGraph<Named> g) {
    super();
    this.g = g;
  }

  public SimpleGraph<Named> getGraph() {
    return g;
  }

  @Override
  public Void traverse(Evl obj, Named param) {
    g.addVertex(param);
    return super.traverse(obj, param);
  }

  @Override
  protected Void visitTypeRef(TypeRef obj, Named param) {
    if (!g.containsVertex(obj.getRef())) {
      g.addVertex(obj.getRef());
      g.addEdge(param, obj.getRef());
      visit(obj.getRef(), obj.getRef());
    }
    return super.visitTypeRef(obj, param);
  }

  @Override
  protected Void visitEnumDefRef(EnumDefRef obj, Named param) {
    if (!g.containsVertex(obj.getElem())) {
      g.addVertex(obj.getElem());
      g.addEdge(param, obj.getElem());
      visit(obj.getElem(), obj.getElem());
    }
    return super.visitEnumDefRef(obj, param);
  }

  @Override
  protected Void visitReference(Reference obj, Named param) {
    assert (param != null);
    super.visitReference(obj, param);

    Named dst = obj.getLink();
    if (g.containsVertex(dst)) {
      return null;
    }
    g.addVertex(dst);
    Scope scope = KnowScope.get(dst);
    switch (scope) { // TODO do we need that?
    case global:
    case privat:
      g.addEdge(param, dst);
      break;
    case local:
      break;
    default:
      RError.err(ErrorType.Fatal, obj.getInfo(), "Unhandled scope: " + scope);
      break;
    }

    visit(dst, dst);

    return null;
  }

  @Override
  protected Void visitCompUse(CompUse obj, Named param) {
    g.addEdge(param, obj.getLink());
    visit(obj.getLink(), obj.getLink());
    return super.visitCompUse(obj, param);
  }

  @Override
  protected Void visitNamedElement(NamedElement obj, Named param) {
    g.addVertex(obj.getType().getRef());
    g.addEdge(param, obj.getType().getRef());
    visit(obj.getType().getRef(), obj.getType().getRef());
    return super.visitNamedElement(obj, param);
  }

  @Override
  protected Void visitFunctionTypeVoid(FunctionTypeVoid obj, Named param) {
    throw new RuntimeException("not yet implemented");
  }

  @Override
  protected Void visitFunctionTypeRet(FunctionTypeRet obj, Named param) {
    throw new RuntimeException("not yet implemented");
  }

  @Override
  protected Void visitVariable(Variable obj, Named param) {
    visit(obj.getType(), param);
    // g.addEdge(param, obj.getType());
    // visit( obj.getType(), obj.getType() );
    return super.visitVariable(obj, param);
  }

  @Override
  protected Void visitEnumElement(EnumElement obj, Named param) {
    // break cycle
    // problem since enumElement is of its defining type
    return null;
  }

}