package evl.solver;

import error.ErrorType;
import error.RError;
import evl.Evl;
import evl.NullTraverser;
import evl.expression.Expression;
import evl.expression.binop.Equal;
import evl.expression.binop.Greater;
import evl.expression.binop.Greaterequal;
import evl.expression.binop.Less;
import evl.expression.binop.Lessequal;
import evl.expression.binop.Notequal;
import evl.expression.binop.Relation;
import evl.expression.reference.Reference;
import evl.variable.Variable;

public class Solver {

  public static Expression solve(Variable var, Relation eq) {
    StupidSolver solver = new StupidSolver();
    Expression expr = solver.traverse(eq, null);
    return expr;
  }

}

class StupidSolver extends NullTraverser<Expression, Void> {

  @Override
  protected Expression visitDefault(Evl obj, Void param) {
    throw new RuntimeException("not yet implemented: " + obj.getClass().getCanonicalName());
  }

  private void checkForSimpleProblem(Relation obj) {
    boolean varInLeft = obj.getLeft() instanceof Reference;
    boolean varInRight = obj.getRight() instanceof Reference;
    if( varInLeft && varInRight ){
      RError.err(ErrorType.Fatal, obj.getInfo(), "Can not yet solve equations, please simplify" );
    }
    if( !varInLeft && !varInRight ){
      RError.err(ErrorType.Fatal, obj.getInfo(), "No variable found" );
    }
  }

  @Override
  protected Expression visitGreater(Greater obj, Void param) {
    checkForSimpleProblem(obj);
    if( obj.getLeft() instanceof Reference ){
      return obj;
    } else {
      return new Lessequal( obj.getInfo(), obj.getRight(), obj.getLeft() );
    }
  }

  @Override
  protected Expression visitGreaterequal(Greaterequal obj, Void param) {
    checkForSimpleProblem(obj);
    if( obj.getLeft() instanceof Reference ){
      return obj;
    } else {
      return new Less( obj.getInfo(), obj.getRight(), obj.getLeft() );
    }
  }

  @Override
  protected Expression visitLess(Less obj, Void param) {
    checkForSimpleProblem(obj);
    if( obj.getLeft() instanceof Reference ){
      return obj;
    } else {
      return new Greaterequal( obj.getInfo(), obj.getRight(), obj.getLeft() );
    }
  }

  @Override
  protected Expression visitLessequal(Lessequal obj, Void param) {
    checkForSimpleProblem(obj);
    if( obj.getLeft() instanceof Reference ){
      return obj;
    } else {
      return new Greater( obj.getInfo(), obj.getRight(), obj.getLeft() );
    }
  }

  @Override
  protected Expression visitEqual(Equal obj, Void param) {
    checkForSimpleProblem(obj);
    if( obj.getLeft() instanceof Reference ){
      return obj;
    } else {
      return new Notequal( obj.getInfo(), obj.getRight(), obj.getLeft() );
    }
  }

  @Override
  protected Expression visitNotequal(Notequal obj, Void param) {
    checkForSimpleProblem(obj);
    if( obj.getLeft() instanceof Reference ){
      return obj;
    } else {
      return new Equal( obj.getInfo(), obj.getRight(), obj.getLeft() );
    }
  }

}
