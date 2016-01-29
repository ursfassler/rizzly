package ast.visitor;

import org.junit.Assert;
import org.junit.Test;

public class VisitExecutor_Test {
  final private VisitExecutorImplementation testee = new VisitExecutorImplementation();
  final private VisitorDummy visitor = new VisitorDummy();
  final private Visitee1 visitee1 = new Visitee1() {
  };
  final private Visitee2 visitee12 = new Visitee12();

  @Test
  public void does_not_visit_methods_with_wrong_signature() {
    testee.visit(visitor, visitee1);

    Assert.assertEquals("1", visitor.visited);
  }

  @Test
  public void executes_multiple_functions() {
    testee.visit(visitor, visitee12);

    Assert.assertEquals("12", visitor.visited);
  }

  @Test
  public void may_execute_no_function() {
    testee.visit(visitor, new Visitee() {
    });

    Assert.assertEquals("", visitor.visited);
  }
}

interface Visitee1 extends Visitee {
}

interface Visitee2 extends Visitee {
}

class Visitee12 implements Visitee1, Visitee2 {
}

class VisitorDummy implements Visitor {
  public String visited = "";

  public void visit(Visitee1 object) {
    visited += "1";
  }

  public void visit(Visitee2 object) {
    visited += "2";
  }
}
