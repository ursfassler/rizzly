package pir.traverser;

import java.util.Map;

import pir.DefTraverser;
import pir.PirObject;
import pir.cfg.ReturnExpr;
import pir.cfg.ReturnVoid;
import pir.expression.reference.VarRef;
import pir.statement.Statement;
import util.SimpleGraph;

//TODO find better name
public class DependencyGraphMaker extends DefTraverser<Boolean, PirObject> {
  private SimpleGraph<PirObject> g = new SimpleGraph<PirObject>();
  private Map<PirObject, Statement> owner;

  public DependencyGraphMaker(Map<PirObject, Statement> owner) {
    super();
    this.owner = owner;
  }

  public static SimpleGraph<PirObject> make(PirObject obj, Map<PirObject, Statement> owner) {
    DependencyGraphMaker maker = new DependencyGraphMaker(owner);
    maker.traverse(obj, null);
    return maker.g;
  }

  @Override
  protected Boolean visitStatement(Statement obj, PirObject param) {
    g.addVertex(obj);
    super.visitStatement(obj, obj);
    return null;
  }

  @Override
  protected Boolean visitVarRef(VarRef obj, PirObject param) {
    assert (param != null);
    Statement srcStmt = owner.get(obj.getRef());
    assert (srcStmt != null);
    g.addEdge(param, srcStmt);
    return super.visitVarRef(obj, param);
  }

  @Override
  protected Boolean visitReturnVoid(ReturnVoid obj, PirObject param) {
    assert (param == null);
    super.visitReturnVoid(obj, obj);
    return true;
  }

  @Override
  protected Boolean visitReturnExpr(ReturnExpr obj, PirObject param) {
    assert (param == null);
    super.visitReturnExpr(obj, obj);
    return true;
  }

}
