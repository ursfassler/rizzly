package fun.function;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import common.ElementInfo;

import fun.expression.reference.Reference;
import fun.other.FunList;
import fun.statement.Block;
import fun.variable.FuncVariable;

public class FunctionFactory {
  static public <T extends FuncHeader> T create(Class<T> type, ElementInfo info, String name, FunList<FuncVariable> param, Reference retType) {
    T ret = null;
    try {
      Constructor<T> c = type.getDeclaredConstructor(ElementInfo.class, String.class, FunList.class, Reference.class);
      ret = c.newInstance(info, name, param, retType);
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

  static public <T extends FuncHeader> T create(Class<T> type, ElementInfo info, String name, FunList<FuncVariable> param, Reference retType, Block body) {
    T ret = null;
    try {
      Constructor<T> c = type.getDeclaredConstructor(ElementInfo.class, String.class, FunList.class, Reference.class, Block.class);
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
