/**
 *  This file is part of Rizzly.
 *
 *  Rizzly is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Rizzly is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with Rizzly.  If not, see <http://www.gnu.org/licenses/>.
 */

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
