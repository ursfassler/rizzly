package pir.traverser;

import java.util.Map;

import pir.DefTraverser;
import pir.PirObject;
import pir.expression.reference.VarRef;
import pir.other.FuncVariable;
import pir.other.SsaVariable;
import pir.other.StateVariable;
import pir.statement.Statement;
import util.SimpleGraph;
import error.ErrorType;
import error.RError;

//TODO find better name
public class DependencyGraphMaker extends DefTraverser<Boolean, PirObject> {
  private SimpleGraph<PirObject> g = new SimpleGraph<PirObject>();
  private Map<SsaVariable, Statement> owner;

  public DependencyGraphMaker(Map<SsaVariable, Statement> owner) {
    super();
    this.owner = owner;
  }

  public static SimpleGraph<PirObject> make(PirObject obj, Map<SsaVariable, Statement> owner) {
    DependencyGraphMaker maker = new DependencyGraphMaker(owner);
    maker.traverse(obj, null);
    return maker.g;
  }

  @Override
  protected Boolean visitStatement(Statement obj, PirObject param) {
    assert (param == null);
    g.addVertex(obj);
    super.visitStatement(obj, obj);
    return null;
  }

  @Override
  protected Boolean visitVarRef(VarRef obj, PirObject param) {
    assert (param != null);
    if (obj.getRef() instanceof SsaVariable) {
      PirObject srcStmt = owner.get(obj.getRef());
      assert (srcStmt != null);
      g.addEdge(param, srcStmt);
    } else if (obj.getRef() instanceof StateVariable) {
      g.addEdge(param, obj.getRef());
    } else if (obj.getRef() instanceof FuncVariable) {
      g.addEdge(param, obj.getRef());
    } else {
      RError.err(ErrorType.Fatal, "not yet implemented: " + obj.getRef().getClass().getCanonicalName());
    }
    return super.visitVarRef(obj, param);
  }

}
