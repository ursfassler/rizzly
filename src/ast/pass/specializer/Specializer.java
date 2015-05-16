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

import java.util.HashMap;
import java.util.Map;

import ast.copy.Copy;
import ast.data.Ast;
import ast.data.AstList;
import ast.data.Named;
import ast.data.expression.value.ValueExpr;
import ast.data.function.template.FunctionTemplate;
import ast.data.function.template.FunctionTemplateSpecializer;
import ast.data.template.ActualTemplateArgument;
import ast.data.template.Template;
import ast.data.type.Type;
import ast.data.type.template.TypeTemplate;
import ast.data.type.template.TypeTemplateSpecializer;
import ast.data.variable.TemplateParameter;
import ast.knowledge.KnowParent;
import ast.knowledge.KnowledgeBase;
import ast.repository.manipulator.AddChild;
import ast.repository.query.ChildByName;
import ast.specification.IsClass;
import error.ErrorType;
import error.RError;

public class Specializer {
  private final Template template;
  private final AstList<ActualTemplateArgument> actualArguments;
  private final KnowledgeBase kb;

  public static Named specialize(Template template, AstList<ActualTemplateArgument> actualArguments, KnowledgeBase kb) {
    Util.checkArgCount(template, actualArguments);
    Util.evalArgType(template.getTempl(), kb);

    if (!Util.isEvaluated(actualArguments)) {
      actualArguments = Util.evalArg(template.getTempl(), actualArguments, kb);
    }

    Specializer specializer = new Specializer(template, actualArguments, kb);
    Named instance = specializer.work();

    return instance;
  }

  public Specializer(Template template, AstList<ActualTemplateArgument> actualArguments, KnowledgeBase kb) {
    super();
    Util.checkArgCount(template, actualArguments);
    this.template = template;
    this.actualArguments = actualArguments;
    this.kb = kb;
  }

  private Named work() {
    String name = NameMangler.name(template.name, actualArguments);

    KnowParent kp = kb.getEntry(KnowParent.class);
    Ast parent = kp.get(template);

    Named inst = (Named) ChildByName.find(parent, name);

    if (inst == null) {
      inst = eval(template.getObject());
      inst.name = name;

      AddChild.add(parent, inst);
      TypeEvalPass.instantiateTemplateReferences(inst, kb);
    }

    return inst;
  }

  private Named eval(Named templ) {
    if (templ instanceof TypeTemplate) {
      return TypeTemplateSpecializer.process((TypeTemplate) templ, actualArguments);
    } else if (templ instanceof FunctionTemplate) {
      return FunctionTemplateSpecializer.process((FunctionTemplate) templ, actualArguments, kb);
    } else {
      return evalUser(templ);
    }
  }

  private Named evalUser(Named templ) {
    Named inst;
    inst = Copy.copy(templ);

    Map<TemplateParameter, Type> typeMap = createArgMap(Type.class);
    Map<TemplateParameter, ValueExpr> valueMap = createArgMap(ValueExpr.class);

    if (typeMap.size() + valueMap.size() != actualArguments.size()) {
      RError.err(ErrorType.Fatal, templ.getInfo(), "Template argument missmatch");
    }

    TypeSpecTrav typeEval = new TypeSpecTrav(typeMap);
    typeEval.traverse(inst, null);

    ExprSpecTrav exprEval = new ExprSpecTrav(valueMap);
    exprEval.traverse(inst, null);

    return inst;
  }

  private <T extends Ast> Map<TemplateParameter, T> createArgMap(Class<T> kind) {
    IsClass isClass = new IsClass(kind);

    Map<TemplateParameter, T> valueMap = new HashMap<TemplateParameter, T>();
    for (int i = 0; i < actualArguments.size(); i++) {
      ActualTemplateArgument itr = actualArguments.get(i);
      TemplateParameter tmpl = template.getTempl().get(i);
      if (isClass.isSatisfiedBy(itr)) {
        valueMap.put(tmpl, (T) itr);
      }
    }
    return valueMap;
  }

}
