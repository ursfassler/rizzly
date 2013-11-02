package evl.function;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import common.ElementInfo;

import evl.other.ListOfNamed;
import evl.variable.FuncVariable;


public class FunctionFactory {
  static public <T extends FunctionBase> T create(Class<T> type, ElementInfo info, String name, ListOfNamed<FuncVariable> param) {
    T ret = null;
    try {
      Constructor<T> c = type.getDeclaredConstructor(ElementInfo.class, String.class, ListOfNamed.class);
      ret = c.newInstance(info, name, param);
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
