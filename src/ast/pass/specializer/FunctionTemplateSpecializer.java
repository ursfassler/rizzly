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

package ast.pass.specializer;

import java.util.List;

import ast.Designator;
import ast.ElementInfo;
import ast.data.Ast;
import ast.data.AstList;
import ast.data.expression.Expression;
import ast.data.expression.reference.Reference;
import ast.data.function.Function;
import ast.data.function.header.FuncFunction;
import ast.data.function.ret.FuncReturnType;
import ast.data.function.template.DefaultValueTemplate;
import ast.data.function.template.FunctionTemplate;
import ast.data.statement.Block;
import ast.data.statement.ReturnExpr;
import ast.data.template.ActualTemplateArgument;
import ast.data.type.Type;
import ast.data.variable.FuncVariable;
import ast.knowledge.KnowEmptyValue;
import ast.knowledge.KnowledgeBase;
import ast.repository.manipulator.RepoAdder;
import ast.traverser.NullTraverser;

public class FunctionTemplateSpecializer extends NullTraverser<Function, List<ActualTemplateArgument>> {
  private final InstanceRepo ki;
  private final KnowEmptyValue kev;
  private final RepoAdder ra;

  public FunctionTemplateSpecializer(InstanceRepo ki, KnowledgeBase kb) {
    this.ki = ki;
    kev = kb.getEntry(KnowEmptyValue.class);
    ra = new RepoAdder(kb);
  }

  public static Function process(FunctionTemplate type, List<ActualTemplateArgument> genspec, InstanceRepo ki, KnowledgeBase kb) {
    FunctionTemplateSpecializer specializer = new FunctionTemplateSpecializer(ki, kb);
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

    Function ret = (Function) ki.find(obj, param);
    if (ret == null) {
      Type type = (Type) param.get(0);

      String name = obj.name + Designator.NAME_SEP + type.name;

      ret = makeFunc(name, type);
      ki.add(obj, param, ret);
      ra.add(ret);
    }
    return ret;
  }

  private Function makeFunc(String name, Type type) {
    ElementInfo info = ElementInfo.NO;
    Block body = new Block(info);
    Expression empty = kev.get(type);
    body.statements.add(new ReturnExpr(info, empty));
    return new FuncFunction(info, name, new AstList<FuncVariable>(), new FuncReturnType(info, new Reference(info, type)), body);
  }
}
