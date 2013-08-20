package pir.traverser;

import java.util.Map;

import pir.DefTraverser;
import pir.PirObject;
import pir.expression.reference.VarRef;
import pir.expression.reference.VarRefSimple;
import pir.other.FuncVariable;
import pir.other.SsaVariable;
import pir.other.StateVariable;
import pir.other.Variable;
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
    Variable refVar = obj.getRef();
    link(param, refVar);
    return super.visitVarRef(obj, param);
  }

  private void link(PirObject param, Variable refVar) {
    if (refVar instanceof SsaVariable) {
      PirObject srcStmt = owner.get(refVar);
      if (srcStmt == null) {
        // function argument
        g.addVertex(refVar);
      } else {
        assert (srcStmt != null);
        g.addEdge(param, srcStmt);
      }
    } else if (refVar instanceof StateVariable) {
      g.addEdge(param, refVar);
    } else if (refVar instanceof FuncVariable) {
      g.addEdge(param, refVar);
    } else {
      RError.err(ErrorType.Fatal, "not yet implemented: " + refVar.getClass().getCanonicalName());
    }
  }

  @Override
  protected Boolean visitVarRefSimple(VarRefSimple obj, PirObject param) {
    assert (param != null);
    Variable refVar = obj.getRef();
    link(param, refVar);
    return super.visitVarRefSimple(obj, param);
  }

}
