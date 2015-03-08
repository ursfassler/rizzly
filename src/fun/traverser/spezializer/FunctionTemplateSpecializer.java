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

import fun.Fun;
import fun.NullTraverser;
import fun.expression.Expression;
import fun.expression.reference.Reference;
import fun.function.FuncFunction;
import fun.function.FuncImpl;
import fun.function.FuncReturnType;
import fun.function.template.DefaultValueTemplate;
import fun.function.template.FunctionTemplate;
import fun.knowledge.KnowBaseItem;
import fun.knowledge.KnowEmptyValue;
import fun.knowledge.KnowInstance;
import fun.knowledge.KnowledgeBase;
import fun.other.ActualTemplateArgument;
import fun.other.FunList;
import fun.statement.Block;
import fun.statement.ReturnExpr;
import fun.type.Type;
import fun.type.base.AnyType;
import fun.variable.FuncVariable;

public class FunctionTemplateSpecializer extends NullTraverser<FuncImpl, List<ActualTemplateArgument>> {
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

  public static FuncImpl process(FunctionTemplate type, List<ActualTemplateArgument> genspec, KnowledgeBase kb) {
    FunctionTemplateSpecializer specializer = new FunctionTemplateSpecializer(kb);
    return specializer.traverse(type, genspec);
  }

  @Override
  protected FuncImpl visitDefault(Fun obj, List<ActualTemplateArgument> param) {
    throw new RuntimeException("not yet implemented: " + obj.getClass().getCanonicalName());
  }

  @Override
  protected FuncImpl visitDefaultValueTemplate(DefaultValueTemplate obj, List<ActualTemplateArgument> param) {
    assert (param.size() == 1);
    FuncImpl ret = (FuncImpl) ki.find(obj, param);
    if (ret == null) {
      Type type = (Type) ArgEvaluator.process(new AnyType(), param.get(0), kb);

      String name = obj.getName() + Designator.NAME_SEP + type.getName();

      ret = makeFunc(name, type);
      ki.add(obj, param, ret);
      kbi.addItem(ret);
    }
    return ret;
  }

  private FuncImpl makeFunc(String name, Type type) {
    ElementInfo info = ElementInfo.NO;
    Block body = new Block(info);
    Expression empty = kev.get(type);
    body.getStatements().add(new ReturnExpr(info, empty));
    return new FuncFunction(info, name, new FunList<FuncVariable>(), new FuncReturnType(info, new Reference(info, type)), body);
  }
}
