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

package fun.knowledge;

import java.util.HashMap;
import java.util.Map;

import parser.TokenType;
import fun.function.FuncFunction;
import fun.function.FuncHeader;
import fun.function.FuncInterrupt;
import fun.function.FuncProcedure;
import fun.function.FuncQuery;
import fun.function.FuncResponse;
import fun.function.FuncSignal;
import fun.function.FuncSlot;

public class KnowFuncType extends KnowledgeEntry {
  static private final KnowFuncType INSTANCE = new KnowFuncType();
  private final Map<TokenType, Class<? extends FuncHeader>> classes = new HashMap<TokenType, Class<? extends FuncHeader>>();
  private final Map<Class<? extends FuncHeader>, Boolean> withRetval = new HashMap<Class<? extends FuncHeader>, Boolean>();
  private final Map<Class<? extends FuncHeader>, Boolean> withBody = new HashMap<Class<? extends FuncHeader>, Boolean>();
  private final Map<Class<? extends FuncHeader>, Boolean> templateArguments = new HashMap<Class<? extends FuncHeader>, Boolean>();
  private final Map<Class<? extends FuncHeader>, Boolean> runtimeArguments = new HashMap<Class<? extends FuncHeader>, Boolean>();

  {
    classes.put(TokenType.QUERY, FuncQuery.class);
    withRetval.put(FuncQuery.class, true);
    withBody.put(FuncQuery.class, false);
    templateArguments.put(FuncQuery.class, false);
    runtimeArguments.put(FuncQuery.class, true);

    classes.put(TokenType.RESPONSE, FuncResponse.class);
    withRetval.put(FuncResponse.class, true);
    withBody.put(FuncResponse.class, true);
    templateArguments.put(FuncResponse.class, false);
    runtimeArguments.put(FuncResponse.class, true);

    classes.put(TokenType.SIGNAL, FuncSignal.class);
    withRetval.put(FuncSignal.class, false);
    withBody.put(FuncSignal.class, false);
    templateArguments.put(FuncSignal.class, false);
    runtimeArguments.put(FuncSignal.class, true);

    classes.put(TokenType.SLOT, FuncSlot.class);
    withRetval.put(FuncSlot.class, false);
    withBody.put(FuncSlot.class, true);
    templateArguments.put(FuncSlot.class, false);
    runtimeArguments.put(FuncSlot.class, true);

    classes.put(TokenType.PROCEDURE, FuncProcedure.class);
    withRetval.put(FuncProcedure.class, false);
    withBody.put(FuncProcedure.class, true);
    templateArguments.put(FuncProcedure.class, false);
    runtimeArguments.put(FuncProcedure.class, true);

    classes.put(TokenType.FUNCTION, FuncFunction.class);
    withRetval.put(FuncFunction.class, true);
    withBody.put(FuncFunction.class, true);
    templateArguments.put(FuncFunction.class, false);
    runtimeArguments.put(FuncFunction.class, true);

    classes.put(TokenType.INTERRUPT, FuncInterrupt.class);
    withRetval.put(FuncInterrupt.class, false);
    withBody.put(FuncInterrupt.class, true);
    templateArguments.put(FuncInterrupt.class, false);
    runtimeArguments.put(FuncInterrupt.class, false);
  }

  public static Class<? extends FuncHeader> getClassOf(TokenType type) {
    if (!INSTANCE.classes.containsKey(type)) {
      throw new RuntimeException("class of type not found: " + type);
    }
    return INSTANCE.classes.get(type);
  }

  public static boolean getWithRetval(Class<? extends FuncHeader> cl) {
    assert (INSTANCE.withRetval.containsKey(cl));
    return INSTANCE.withRetval.get(cl);
  }

  public static boolean getWithBody(Class<? extends FuncHeader> cl) {
    assert (INSTANCE.withBody.containsKey(cl));
    return INSTANCE.withBody.get(cl);
  }

  public static boolean getTemplatearguments(Class<? extends FuncHeader> cl) {
    assert (INSTANCE.templateArguments.containsKey(cl));
    return INSTANCE.templateArguments.get(cl);
  }

  public static boolean getRuntimearguments(Class<? extends FuncHeader> cl) {
    assert (INSTANCE.runtimeArguments.containsKey(cl));
    return INSTANCE.runtimeArguments.get(cl);
  }

  @Override
  public void init(KnowledgeBase base) {
  }

}
