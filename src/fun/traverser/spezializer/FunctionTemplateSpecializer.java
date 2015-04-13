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

package fun.traverser.spezializer;

import java.util.List;

import common.Designator;
import common.ElementInfo;

import evl.data.Evl;
import evl.data.EvlList;
import evl.data.expression.Expression;
import evl.data.expression.reference.Reference;
import evl.data.function.Function;
import evl.data.function.header.FuncFunction;
import evl.data.function.ret.FuncReturnType;
import evl.data.function.template.DefaultValueTemplate;
import evl.data.function.template.FunctionTemplate;
import evl.data.statement.Block;
import evl.data.statement.ReturnExpr;
import evl.data.type.Type;
import evl.data.type.special.AnyType;
import evl.data.variable.FuncVariable;
import evl.knowledge.KnowBaseItem;
import evl.knowledge.KnowEmptyValue;
import evl.knowledge.KnowInstance;
import evl.knowledge.KnowledgeBase;
import evl.traverser.NullTraverser;
import fun.other.ActualTemplateArgument;

public class FunctionTemplateSpecializer extends NullTraverser<Function, List<ActualTemplateArgument>> {
  private final KnowledgeBase kb;
  private final KnowInstance ki;
  private final KnowBaseItem kbi;
  private final KnowEmptyValue kev;

  public FunctionTemplateSpecializer(KnowledgeBase kb) {
    this.kb = kb;
    ki = kb.getEntry(KnowInstance.class);
    kbi = kb.getEntry(KnowBaseItem.class);
    kev = kb.getEntry(KnowEmptyValue.class);
  }

  public static Function process(FunctionTemplate type, List<ActualTemplateArgument> genspec, KnowledgeBase kb) {
    FunctionTemplateSpecializer specializer = new FunctionTemplateSpecializer(kb);
    return specializer.traverse(type, genspec);
  }

  @Override
  protected Function visitDefault(Evl obj, List<ActualTemplateArgument> param) {
    throw new RuntimeException("not yet implemented: " + obj.getClass().getCanonicalName());
  }

  @Override
  protected Function visitDefaultValueTemplate(DefaultValueTemplate obj, List<ActualTemplateArgument> param) {
    assert (param.size() == 1);
    Function ret = (Function) ki.find(obj, param);
    if (ret == null) {
      Type type = (Type) ArgEvaluator.process(new AnyType(), param.get(0), kb);

      String name = obj.getName() + Designator.NAME_SEP + type.name;

      ret = makeFunc(name, type);
      ki.add(obj, param, ret);
      kbi.addItem(ret);
    }
    return ret;
  }

  private Function makeFunc(String name, Type type) {
    ElementInfo info = ElementInfo.NO;
    Block body = new Block(info);
    Expression empty = kev.get(type);
    body.statements.add(new ReturnExpr(info, empty));
    return new FuncFunction(info, name, new EvlList<FuncVariable>(), new FuncReturnType(info, new Reference(info, type)), body);
  }
}
