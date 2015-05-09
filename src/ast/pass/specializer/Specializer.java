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
import ast.data.Namespace;
import ast.data.component.hfsm.State;
import ast.data.component.hfsm.StateContent;
import ast.data.expression.Expression;
import ast.data.expression.RefExp;
import ast.data.function.template.FunctionTemplate;
import ast.data.raw.RawElementary;
import ast.data.reference.Reference;
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

    Map<TemplateParameter, Type> typeMap = new HashMap<TemplateParameter, Type>();
    Map<TemplateParameter, Expression> valueMap = new HashMap<TemplateParameter, Expression>();

    for (int i = 0; i < genspec.size(); i++) {
      ActualTemplateArgument itr = genspec.get(i);
      TemplateParameter tmpl = item.getTempl().get(i);
      if (isTypeTempl(tmpl)) {
        Type type = evalType(ki, kb, ((RefExp) itr).ref);
        typeMap.put(tmpl, type);
        itr = type;
      } else {
        Expression expr = evalExpr(ki, kb, (Expression) itr);
        valueMap.put(tmpl, expr);
        itr = expr;
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

        TypeSpecTrav typeEval = new TypeSpecTrav(typeMap);
        Reference tr = typeEval.traverse(inst, null);

        ExprSpecTrav exprEval = new ExprSpecTrav(valueMap);
        Expression ri = exprEval.traverse(inst, null);

        if (tr != null) {
          inst = tr;
        }
        if (ri != null) {
          assert (false);
        }

        KnowParent kp = kb.getEntry(KnowParent.class);
        Ast parent = kp.get(item);

        // TODO create clean name

        addChild(inst, parent);
      }

      ki.add(templ, genspec, inst);

      TypeEvalExecutor.eval(inst, ki, kb);

      ConstEval.process(inst, kb);

      // remove templates
      TemplDel.process(inst);
    }

    while (inst instanceof Reference) {
      Reference ref = (Reference) inst;
      assert (ref.offset.isEmpty());
      inst = ref.link;
    }

    assert (!(inst instanceof Reference));

    return inst;
  }

  private static boolean isTypeTempl(TemplateParameter templateParameter) {
    Type type = (Type) templateParameter.type.ref.getTarget();
    return type instanceof TypeType;
  }

  private static Expression evalExpr(InstanceRepo ir, KnowledgeBase kb, Expression itr) {
    return ExprEvaluator.evaluate(itr, new Memory(), ir, kb);
  }

  private static Type evalType(InstanceRepo ir, KnowledgeBase kb, Reference ref) {
    return TypeEvaluator.evaluate(ref, new Memory(), ir, kb);
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

}
