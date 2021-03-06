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

package ast.data.function;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import ast.data.AstList;
import ast.data.function.ret.FuncReturn;
import ast.data.statement.Block;
import ast.data.variable.FunctionVariable;

public class FunctionFactory {
  static public <T extends Function> T create(Class<T> type, String name, AstList<FunctionVariable> param, FuncReturn retType, Block body) {
    T ret = null;
    try {
      Constructor<T> c = type.getDeclaredConstructor(String.class, AstList.class, FuncReturn.class, Block.class);
      ret = c.newInstance(name, param, retType, body);
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
