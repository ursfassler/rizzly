package ast.visitor;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;

public class VisitMethodCollector {
  private ArrayList<Method> methods = new ArrayList<Method>();

  public Collection<Method> getMethods() {
    return methods;
  }

  public void collectMethods(Class visitor, Class visitee) {
    collectMethodsOfAllSuperclasses(visitor, visitee);
    collectMethodsOfAllInterfaces(visitor, visitee);
  }

  private void collectMethodsOfAllInterfaces(Class visitor, Class visitee) {
    for (Class iface : visitee.getInterfaces()) {
      collectMethodsOfAllSuperclasses(visitor, iface);
    }
  }

  private void collectMethodsOfAllSuperclasses(Class visitor, Class visitee) {
    for (; visitee != null; visitee = visitee.getSuperclass()) {
      collectMethodIfAvailable(visitor, visitee);
      collectMethodsOfAllInterfaces(visitor, visitee);
    }
  }

  private void collectMethodIfAvailable(Class visitor, Class visitee) {
    try {
      Method method = visitor.getMethod("visit", visitee);
      if (!methods.contains(method)) {
        methods.add(method);
      }
    } catch (NoSuchMethodException e) {
    } catch (SecurityException e) {
    }
  }

}
