package evl.function;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import common.ElementInfo;

import evl.expression.reference.SimpleRef;
import evl.other.EvlList;
import evl.statement.Block;
import evl.variable.FuncVariable;

public class FunctionFactory {
  static public <T extends Function> T create(Class<T> type, ElementInfo info, String name, EvlList<FuncVariable> param, SimpleRef retType, Block body) {
    T ret = null;
    try {
      Constructor<T> c = type.getDeclaredConstructor(ElementInfo.class, String.class, EvlList.class, SimpleRef.class, Block.class);
      ret = c.newInstance(info, name, param, retType, body);
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
