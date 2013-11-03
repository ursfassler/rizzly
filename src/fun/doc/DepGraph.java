package fun.doc;

import java.util.HashSet;
import java.util.Set;

import util.SimpleGraph;

import common.Scope;

import error.ErrorType;
import error.RError;
import fun.DefGTraverser;
import fun.Fun;
import fun.NullTraverser;
import fun.expression.reference.ReferenceLinked;
import fun.expression.reference.ReferenceUnlinked;
import fun.function.FunctionHeader;
import fun.knowledge.KnowFun;
import fun.knowledge.KnowScope;
import fun.knowledge.KnowledgeBase;
import fun.other.Component;
import fun.other.Generator;
import fun.other.Named;
import fun.other.Namespace;
import fun.type.Type;
import fun.variable.Constant;

public class DepGraph extends NullTraverser<Void, Void> {
  private SubDep dep;

  public DepGraph(KnowledgeBase kb) {
    super();
    dep = new SubDep(kb, new SimpleGraph<Named>());
  }

  static public SimpleGraph<Named> build(Namespace ns, KnowledgeBase kb) {
    DepGraph depGraph = new DepGraph(kb);
    depGraph.traverse(ns, null);
    return depGraph.dep.getGraph();
  }

  static public SimpleGraph<Named> build(Named root, KnowledgeBase kb) {
    DepGraph depGraph = new DepGraph(kb);
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
  protected Void visitGenerator(Generator obj, Void param) {
    dep.traverse(obj, obj);
    return null;
  }

  @Override
  protected Void visitComponent(Component obj, Void param) {
    dep.traverse(obj, obj);
    return null;
  }

}

class SubDep extends DefGTraverser<Void, Named> {
  private KnowledgeBase kb;
  private SimpleGraph<Named> g;
  private Set<Named> traversed = new HashSet<Named>();

  public SubDep(KnowledgeBase kb, SimpleGraph<Named> simpleGraph) {
    super();
    this.kb = kb;
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
  protected Void visitReferenceUnlinked(ReferenceUnlinked obj, Named param) {
    assert (param != null);
    super.visitReferenceUnlinked(obj, param);
    KnowFun ka = kb.getEntry(KnowFun.class);
    Fun item = ka.find(obj.getName());
    if (item != null) {
      g.addEdge(param, (Named) item);
    }
    return null;
  }

  @Override
  protected Void visitReferenceLinked(ReferenceLinked obj, Named param) {
    assert (param != null);
    super.visitReferenceLinked(obj, param);
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
