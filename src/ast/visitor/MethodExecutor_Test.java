package ast.visitor;

import java.lang.reflect.Method;
import java.util.ArrayList;

import org.junit.Assert;
import org.junit.Test;

public class MethodExecutor_Test {
  private final MethodExecutor testee = new MethodExecutor();
  private final ArrayList<Method> methods = new ArrayList<Method>();

  {
    try {
      methods.add(Dummy.class.getMethod("method1", Object.class));
      methods.add(Dummy.class.getMethod("method2", Object.class));
      methods.add(Dummy.class.getMethod("method3", Object.class));
    } catch (NoSuchMethodException e) {
      e.printStackTrace();
    } catch (SecurityException e) {
      e.printStackTrace();
    }
  }

  @Test
  public void no_execution_when_there_are_no_functions() {
    Dummy dummy = new Dummy();

    testee.executeFirst(dummy, new ArrayList<Method>(), null);

    Assert.assertEquals("", dummy.calls);
  }

  @Test
  public void can_execute_first_function() {
    Dummy dummy = new Dummy();

    testee.executeFirst(dummy, methods, null);

    Assert.assertEquals(3, methods.size());
    Assert.assertEquals("1", dummy.calls);
  }

  @Test
  public void can_execute_all_functions() {
    Dummy dummy = new Dummy();

    testee.executeAll(dummy, methods, null);

    Assert.assertEquals(3, methods.size());
    Assert.assertEquals("123", dummy.calls);
  }

  @Test(expected = RuntimeException.class)
  public void an_exception_from_the_method_is_thrown_to_the_execute_method() {
    ArrayList<Method> methods = new ArrayList<Method>();
    try {
      methods.add(Problems.class.getMethod("methodThrowsException", Object.class));
    } catch (NoSuchMethodException e) {
    } catch (SecurityException e) {
    }
    Problems dummy = new Problems();

    testee.executeAll(dummy, methods, null);
  }
}

class Dummy implements Visitor {
  public String calls = "";

  public void method1(Object object) {
    calls += "1";
  }

  public void method2(Object object) {
    calls += "2";
  }

  public void method3(Object object) {
    calls += "3";
  }

}

class Problems implements Visitor {
  public void methodThrowsException(Object object) {
    throw new RuntimeException("the exception message");
  }

}
