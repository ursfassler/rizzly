package ast.visitor;

import ast.data.Ast;
import ast.data.AstList;

public class VisitExecutorImplementation implements VisitExecutor {

  @Deprecated
  public static VisitExecutorImplementation instance() {
    return new VisitExecutorImplementation();
  }

  @Override
  public void visit(Visitor visitor, Visitee visitee) {
    VisitMethodCollector collector = new VisitMethodCollector();
    collector.collectMethods(visitor.getClass(), visitee.getClass());

    MethodExecutor executor = new MethodExecutor();
    executor.executeAll(visitor, collector.getMethods(), visitee);
  }

  @Override
  public void visit(Visitor visitor, AstList<? extends Ast> list) {
    for (Ast visitee : list) {
      visit(visitor, visitee);
    }
  }
}
