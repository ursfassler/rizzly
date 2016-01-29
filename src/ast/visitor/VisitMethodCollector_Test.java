package ast.visitor;

import java.lang.reflect.Method;
import java.util.Collection;

import org.junit.Assert;
import org.junit.Test;

public class VisitMethodCollector_Test {
  final private VisitMethodCollector testee = new VisitMethodCollector();

  @Test
  public void returns_nothing_when_no_method_is_found() {
    testee.collectMethods(Visitor.class, Object.class);
    Collection<Method> methods = testee.getMethods();

    Assert.assertNotNull(methods);
    Assert.assertTrue(methods.isEmpty());
  }

  class ObjectVisitor implements Visitor {
    public void visit(Object object) {
    }
  }

  @Test
  public void returns_visit_method_of_class() {
    ObjectVisitor visitor = new ObjectVisitor();
    testee.collectMethods(visitor.getClass(), Visitee.class);

    Collection<Method> methods = testee.getMethods();

    Assert.assertNotNull(methods);
    Assert.assertEquals(1, methods.size());
    Assert.assertEquals(expected(ObjectVisitor.class, Object.class), methods.iterator().next());

  }

  class ObjectVisitorSubclass extends ObjectVisitor {
  }

  @Test
  public void returns_visit_method_of_superclass() {
    ObjectVisitorSubclass visitor = new ObjectVisitorSubclass();
    testee.collectMethods(visitor.getClass(), Object.class);

    Collection<Method> methods = testee.getMethods();

    Assert.assertNotNull(methods);
    Assert.assertEquals(1, methods.size());
    Assert.assertEquals(expected(ObjectVisitor.class, Object.class), methods.toArray()[0]);
  }

  class ObjectVisitorSubclassWithVisit extends ObjectVisitor {
    @Override
    public void visit(Object object) {
    }
  }

  @Test
  public void returns_overriden_visit_method_of_subclass() {
    ObjectVisitor visitor = new ObjectVisitorSubclassWithVisit();
    testee.collectMethods(visitor.getClass(), Object.class);

    Collection<Method> methods = testee.getMethods();

    Assert.assertNotNull(methods);
    Assert.assertEquals(1, methods.size());
    Assert.assertEquals(expected(ObjectVisitorSubclassWithVisit.class, Object.class), methods.toArray()[0]);
  }

  class Visitee {
  }

  @Test
  public void returns_method_with_best_match_for_visitee() {
    testee.collectMethods(ObjectVisitor.class, Visitee.class);
    Collection<Method> methods = testee.getMethods();

    Assert.assertNotNull(methods);
    Assert.assertEquals(1, methods.size());
    Assert.assertEquals(expected(ObjectVisitor.class, Object.class), methods.toArray()[0]);
  }

  class VisitorWithMultipleVisitMethods {
    public void visit(Object object) {
    }

    public void visit(Visitee object) {
    }
  }

  @Test
  public void return_method_with_mode_narrow_type_first() {
    testee.collectMethods(VisitorWithMultipleVisitMethods.class, Visitee.class);
    Collection<Method> methods = testee.getMethods();

    Assert.assertNotNull(methods);
    Assert.assertEquals(2, methods.size());
    Assert.assertEquals(expected(VisitorWithMultipleVisitMethods.class, Visitee.class), methods.toArray()[0]);
    Assert.assertEquals(expected(VisitorWithMultipleVisitMethods.class, Object.class), methods.toArray()[1]);
  }

  interface VisiteeInterface {
  }

  class VisitorVisitsInterface {
    public void visit(VisiteeInterface object) {
    }
  }

  @Test
  public void return_method_when_visitee_is_an_interface() {
    testee.collectMethods(VisitorVisitsInterface.class, VisiteeInterface.class);
    Collection<Method> methods = testee.getMethods();

    Assert.assertNotNull(methods);
    Assert.assertEquals(1, methods.size());
    Assert.assertEquals(expected(VisitorVisitsInterface.class, VisiteeInterface.class), methods.toArray()[0]);
  }

  class VisiteeWithInterface implements VisiteeInterface {
  }

  class VisitorWithInterfaceVisits {
    public void visit(VisiteeInterface object) {
    }
  }

  @Test
  public void return_method_with_matching_interface() {
    testee.collectMethods(VisitorWithInterfaceVisits.class, VisiteeWithInterface.class);
    Collection<Method> methods = testee.getMethods();

    Assert.assertNotNull(methods);
    Assert.assertEquals(1, methods.size());
    Assert.assertEquals(expected(VisitorWithInterfaceVisits.class, VisiteeInterface.class), methods.toArray()[0]);
  }

  interface SubInterface extends VisiteeInterface {
  }

  @Test
  public void return_method_with_matching_superinterface() {
    testee.collectMethods(VisitorWithInterfaceVisits.class, SubInterface.class);
    Collection<Method> methods = testee.getMethods();

    Assert.assertNotNull(methods);
    Assert.assertEquals(1, methods.size());
    Assert.assertEquals(expected(VisitorWithInterfaceVisits.class, VisiteeInterface.class), methods.toArray()[0]);
  }

  class VisitorWithObjectAndInterfaceVisits {
    public void visit(VisiteeWithInterface object) {
    }

    public void visit(VisiteeInterface object) {
    }
  }

  @Test
  public void return_object_bevore_interface() {
    testee.collectMethods(VisitorWithObjectAndInterfaceVisits.class, VisiteeWithInterface.class);
    Collection<Method> methods = testee.getMethods();

    Assert.assertNotNull(methods);
    Assert.assertEquals(2, methods.size());
    Assert.assertEquals(expected(VisitorWithObjectAndInterfaceVisits.class, VisiteeWithInterface.class), methods.toArray()[0]);
    Assert.assertEquals(expected(VisitorWithObjectAndInterfaceVisits.class, VisiteeInterface.class), methods.toArray()[1]);
  }

  private Method expected(Class visitor, Class visitee) {
    try {
      return visitor.getMethod("visit", visitee);
    } catch (NoSuchMethodException e) {
    } catch (SecurityException e) {
    }
    throw new RuntimeException("visit method not found");
  }

  class Base implements VisiteeInterface {
  }

  class Derived extends Base {
  }

  @Test
  public void visit_interface_of_base_object() {
    Object visitor = new VisitorVisitsInterface();
    Object visitee = new Derived();
    testee.collectMethods(visitor.getClass(), visitee.getClass());
    Collection<Method> methods = testee.getMethods();

    Assert.assertNotNull(methods);
    Assert.assertEquals(1, methods.size());
    Assert.assertEquals(expected(VisitorVisitsInterface.class, VisiteeInterface.class), methods.toArray()[0]);
  }
}
