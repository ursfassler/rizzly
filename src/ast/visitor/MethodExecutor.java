package ast.visitor;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Iterator;

public class MethodExecutor {
  public void executeFirst(Visitor object, Collection<Method> methods, Visitee arg) {
    Iterator<Method> iterator = methods.iterator();
    if (iterator.hasNext()) {
      Method method = iterator.next();
      execute(object, method, arg);
    }
  }

  public void executeAll(Visitor object, Collection<Method> methods, Visitee arg) {
    for (Method method : methods) {
      execute(object, method, arg);
    }
  }

  private void execute(Visitor object, Method method, Visitee arg) {
    try {
      method.invoke(object, arg);
    } catch (IllegalAccessException e) {
      e.printStackTrace();
    } catch (IllegalArgumentException e) {
      e.printStackTrace();
    } catch (InvocationTargetException e) {
      if (e.getCause() instanceof RuntimeException) {
        throw (RuntimeException) e.getCause();
      } else {
        e.printStackTrace();
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}
