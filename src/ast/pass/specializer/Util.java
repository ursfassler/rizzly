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

import ast.data.AstList;
import ast.data.expression.Expression;
import ast.data.expression.ReferenceExpression;
import ast.data.expression.value.ValueExpr;
import ast.data.reference.Reference;
import ast.data.template.ActualTemplateArgument;
import ast.data.template.Template;
import ast.data.type.Type;
import ast.data.type.TypeRefFactory;
import ast.data.type.TypeReference;
import ast.data.variable.TemplateParameter;
import ast.interpreter.Memory;
import ast.knowledge.KnowledgeBase;
import ast.specification.IsTypeTemplate;
import error.ErrorType;
import error.RError;

public class Util {

  public static boolean isEvaluated(AstList<ActualTemplateArgument> genspec) {
    for (ActualTemplateArgument ta : genspec) {
      if (!(ta instanceof Type) && !(ta instanceof ValueExpr)) {
        return false;
      }
    }
    return true;
  }

  public static AstList<ActualTemplateArgument> evalArg(AstList<TemplateParameter> param, AstList<ActualTemplateArgument> arg, KnowledgeBase kb) {
    assert (param.size() == arg.size());

    AstList<ActualTemplateArgument> ret = new AstList<ActualTemplateArgument>();

    IsTypeTemplate isType = new IsTypeTemplate();

    for (int i = 0; i < arg.size(); i++) {
      ActualTemplateArgument itr = arg.get(i);
      TemplateParameter tmpl = param.get(i);
      if (isType.isSatisfiedBy(tmpl)) {
        itr = evalType(((ReferenceExpression) itr).reference, kb);
      } else {
        itr = evalExpr((Expression) itr, kb);
      }
      ret.add(itr);
    }

    return ret;
  }

  public static void checkArgCount(Template item, AstList<ActualTemplateArgument> genspec) {
    if (item.getTempl().size() != genspec.size()) {
      RError.err(ErrorType.Error, "Wrong number of parameter, expected " + item.getTempl().size() + " got " + genspec.size(), item.metadata());
    }
  }

  private static Expression evalExpr(Expression itr, KnowledgeBase kb) {
    return ExprEvaluator.evaluate(itr, new Memory(), kb);
  }

  private static Type evalType(Reference ref, KnowledgeBase kb) {
    return (Type) RefEvaluator.execute(ref, new Memory(), kb);
  }

  public static void evalArgType(AstList<TemplateParameter> templ, KnowledgeBase kb) {
    for (TemplateParameter param : templ) {
      param.type = evalTypeRef(param.type, kb);
    }
  }

  private static TypeReference evalTypeRef(TypeReference tr, KnowledgeBase kb) {
    Type type = evalType(tr.ref, kb);
    return TypeRefFactory.create(tr.metadata(), type);
  }

}
