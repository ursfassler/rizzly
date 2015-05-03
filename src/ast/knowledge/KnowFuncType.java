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

package ast.knowledge;

import java.util.HashMap;
import java.util.Map;

import parser.scanner.TokenType;
import ast.data.function.Function;
import ast.data.function.header.FuncFunction;
import ast.data.function.header.FuncInterrupt;
import ast.data.function.header.FuncProcedure;
import ast.data.function.header.FuncQuery;
import ast.data.function.header.FuncResponse;
import ast.data.function.header.FuncSignal;
import ast.data.function.header.FuncSlot;

public class KnowFuncType extends KnowledgeEntry {
  static private final KnowFuncType INSTANCE = new KnowFuncType();
  private final Map<TokenType, Class<? extends Function>> classes = new HashMap<TokenType, Class<? extends Function>>();
  private final Map<Class<? extends Function>, Boolean> withRetval = new HashMap<Class<? extends Function>, Boolean>();
  private final Map<Class<? extends Function>, Boolean> withBody = new HashMap<Class<? extends Function>, Boolean>();

  {
    classes.put(TokenType.QUERY, FuncQuery.class);
    withRetval.put(FuncQuery.class, true);
    withBody.put(FuncQuery.class, false);

    classes.put(TokenType.RESPONSE, FuncResponse.class);
    withRetval.put(FuncResponse.class, true);
    withBody.put(FuncResponse.class, true);

    classes.put(TokenType.SIGNAL, FuncSignal.class);
    withRetval.put(FuncSignal.class, false);
    withBody.put(FuncSignal.class, false);

    classes.put(TokenType.SLOT, FuncSlot.class);
    withRetval.put(FuncSlot.class, false);
    withBody.put(FuncSlot.class, true);

    classes.put(TokenType.PROCEDURE, FuncProcedure.class);
    withRetval.put(FuncProcedure.class, false);
    withBody.put(FuncProcedure.class, true);

    classes.put(TokenType.FUNCTION, FuncFunction.class);
    withRetval.put(FuncFunction.class, true);
    withBody.put(FuncFunction.class, true);

    classes.put(TokenType.INTERRUPT, FuncInterrupt.class);
    withRetval.put(FuncInterrupt.class, false);
    withBody.put(FuncInterrupt.class, true);
  }

  public static Class<? extends Function> getClassOf(TokenType type) {
    if (!INSTANCE.classes.containsKey(type)) {
      throw new RuntimeException("class of type not found: " + type);
    }
    return INSTANCE.classes.get(type);
  }

  public static boolean getWithRetval(Class<? extends Function> cl) {
    assert (INSTANCE.withRetval.containsKey(cl));
    return INSTANCE.withRetval.get(cl);
  }

  public static boolean getWithBody(Class<? extends Function> cl) {
    assert (INSTANCE.withBody.containsKey(cl));
    return INSTANCE.withBody.get(cl);
  }

  @Override
  public void init(ast.knowledge.KnowledgeBase base) {
  }

}
