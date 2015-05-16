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

package ast.data.function.template;

import java.util.List;

import ast.ElementInfo;
import ast.data.Ast;
import ast.data.AstList;
import ast.data.expression.value.ValueExpr;
import ast.data.function.Function;
import ast.data.function.header.FuncFunction;
import ast.data.function.ret.FuncReturnType;
import ast.data.statement.Block;
import ast.data.statement.ReturnExpr;
import ast.data.template.ActualTemplateArgument;
import ast.data.type.Type;
import ast.data.type.TypeRefFactory;
import ast.data.variable.FuncVariable;
import ast.dispatcher.NullDispatcher;
import ast.knowledge.KnowEmptyValue;
import ast.knowledge.KnowledgeBase;

public class FunctionTemplateSpecializer extends NullDispatcher<Function, List<ActualTemplateArgument>> {
  private final KnowEmptyValue kev;

  public FunctionTemplateSpecializer(KnowledgeBase kb) {
    kev = kb.getEntry(KnowEmptyValue.class);
  }

  public static Function process(FunctionTemplate type, List<ActualTemplateArgument> genspec, KnowledgeBase kb) {
    FunctionTemplateSpecializer specializer = new FunctionTemplateSpecializer(kb);
    return specializer.traverse(type, genspec);
  }

  @Override
  protected Function visitDefault(Ast obj, List<ActualTemplateArgument> param) {
    throw new RuntimeException("not yet implemented: " + obj.getClass().getCanonicalName());
  }

  @Override
  protected Function visitDefaultValueTemplate(DefaultValueTemplate obj, List<ActualTemplateArgument> param) {
    assert (param.size() == 1);
    assert (param.get(0) instanceof Type);

    Type type = (Type) param.get(0);
    Function ret = makeFunc(obj.name, type);

    return ret;
  }

  private Function makeFunc(String name, Type type) {
    ElementInfo info = ElementInfo.NO;
    Block body = new Block(info);
    ValueExpr empty = kev.get(type);
    body.statements.add(new ReturnExpr(info, empty));
    return new FuncFunction(info, name, new AstList<FuncVariable>(), new FuncReturnType(info, TypeRefFactory.create(info, type)), body);
  }
}
