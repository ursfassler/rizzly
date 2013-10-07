package evl.solver;

import evl.expression.Expression;
import evl.expression.binop.Relation;
import evl.variable.Variable;

public class Solver {

  public static SolverResult solve(Variable var, Relation eq) {
    Expression result = RelationEvaluator.eval( eq, var );
    
    
    
/*    Expression left = tm.traverse(eq.getLeft(), var);
    Expression right = tm.traverse(eq.getRight(), var);
    
    List<Reference> lc = ClassGetter.get(Reference.class, left);
    List<Reference> rc = ClassGetter.get(Reference.class, right);
    
    if( lc.isEmpty() && rc.isEmpty() ){
    }*/
    
    throw new RuntimeException("not yet implemented");
  }

}
