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
import java.util.List;
import java.util.Map;

import ast.copy.Copy;
import ast.data.Ast;
import ast.data.AstList;
import ast.data.Named;
import ast.data.Namespace;
import ast.data.component.hfsm.State;
import ast.data.component.hfsm.StateContent;
import ast.data.expression.Expression;
import ast.data.expression.reference.BaseRef;
import ast.data.expression.reference.Reference;
import ast.data.function.template.FunctionTemplate;
import ast.data.raw.RawElementary;
import ast.data.template.ActualTemplateArgument;
import ast.data.template.Template;
import ast.data.type.Type;
import ast.data.type.special.TypeType;
import ast.data.type.template.TypeTemplate;
import ast.data.variable.TemplateParameter;
import ast.interpreter.Memory;
import ast.knowledge.KnowParent;
import ast.knowledge.KnowledgeBase;

//TODO rethink it; make it clean
public class Specializer {

  public static Ast process(Template item, List<ActualTemplateArgument> genspec, InstanceRepo ki, KnowledgeBase kb) {
    assert (item.getTempl().size() == genspec.size());
    for (int i = 0; i < genspec.size(); i++) {
      ActualTemplateArgument itr = genspec.get(i);
      if (isTypeTempl(item.getTempl().get(i))) {
        itr = evalType(ki, kb, (Reference) itr);
      } else {
        itr = evalExpr(ki, kb, (Expression) itr);
      }
      genspec.set(i, itr);
    }

    Ast templ = item.getObject();

    Ast inst = ki.find(templ, genspec);

    if (inst == null) {
      if (templ instanceof TypeTemplate) {
        inst = TypeTemplateSpecializer.process((TypeTemplate) templ, genspec, kb);
      } else if (templ instanceof FunctionTemplate) {
        inst = FunctionTemplateSpecializer.process((FunctionTemplate) templ, genspec, ki, kb);
      } else {
        inst = Copy.copy(templ);

        TypeSpecTrav evaluator = new TypeSpecTrav();
        Expression ri = evaluator.traverse(inst, makeMap(genspec, item.getTempl()));

        // if inst is a reference, the new one is returned
        if (ri != null) {
          inst = ri;
        }

        KnowParent kp = kb.getEntry(KnowParent.class);
        Ast parent = kp.get(item);

        // TODO create clean name

        addChild(inst, parent);
      }

      ki.add(templ, genspec, inst);

      // evaluate
      TypeEvalReplacer typeEvalReplacer = new TypeEvalReplacer(ki, kb);
      typeEvalReplacer.traverse(inst, null);

      ConstEval.process(inst, kb);

      // remove templates
      TemplDel.process(inst);
    }

    while (inst instanceof Reference) {
      ast.data.expression.reference.Reference ref = (ast.data.expression.reference.Reference) inst;
      assert (ref.offset.isEmpty());
      inst = ref.link;
    }

    assert (!(inst instanceof BaseRef));

    return inst;
  }

  private static boolean isTypeTempl(TemplateParameter templateParameter) {
    assert (templateParameter.type instanceof BaseRef);
    Type type = (Type) ((BaseRef<Named>) templateParameter.type).getTarget();
    return type instanceof TypeType;
  }

  private static Expression evalExpr(InstanceRepo ir, KnowledgeBase kb, Expression itr) {
    return ExprEvaluator.evaluate(itr, new Memory(), ir, kb);
  }

  private static Type evalType(InstanceRepo ir, KnowledgeBase kb, Reference itr) {
    return TypeEvaluator.evaluate(itr, new Memory(), ir, kb);
  }

  private static void addChild(Ast inst, Ast parent) {
    if (parent instanceof Namespace) {
      ((ast.data.Namespace) parent).children.add(inst);
    } else if (parent instanceof RawElementary) {
      ((RawElementary) parent).getInstantiation().add(inst);
    } else if (parent instanceof State) {
      ((ast.data.component.hfsm.State) parent).item.add((StateContent) inst);
    } else {
      throw new RuntimeException("not yet implemented: " + parent.getClass().getCanonicalName());
    }
  }

  private static Map<TemplateParameter, ActualTemplateArgument> makeMap(List<ActualTemplateArgument> param, AstList<TemplateParameter> param1) {
    Map<TemplateParameter, ActualTemplateArgument> map = new HashMap<TemplateParameter, ActualTemplateArgument>();
    for (int i = 0; i < param.size(); i++) {
      TemplateParameter var = param1.get(i);
      ActualTemplateArgument val = param.get(i);
      map.put(var, val);
    }
    return map;
  }

}
