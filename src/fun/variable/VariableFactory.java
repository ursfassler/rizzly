package fun.variable;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import common.ElementInfo;

import fun.expression.reference.Reference;

public class VariableFactory {
  static public <T extends Variable> T create(Class<T> kind, ElementInfo info, String name, Reference type) {
    T ret = null;
    try {
      Constructor<T> c = kind.getDeclaredConstructor(ElementInfo.class, String.class, Reference.class);
      ret = c.newInstance(info, name, type);
    } catch (InstantiationException e) {
      e.printStackTrace();
    } catch (IllegalAccessException e) {
      e.printStackTrace();
    } catch (IllegalArgumentException e) {
      e.printStackTrace();
    } catch (SecurityException e) {
      e.printStackTrace();
    } catch (InvocationTargetException e) {
      e.printStackTrace();
    } catch (NoSuchMethodException e) {
      e.printStackTrace();
    }
    return ret;
  }

}
