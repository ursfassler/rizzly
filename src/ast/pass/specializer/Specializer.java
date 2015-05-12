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
import ast.data.Namespace;
import ast.data.component.hfsm.State;
import ast.data.component.hfsm.StateContent;
import ast.data.expression.Expression;
import ast.data.expression.RefExp;
import ast.data.expression.value.ValueExpr;
import ast.data.function.template.FunctionTemplate;
import ast.data.raw.RawElementary;
import ast.data.reference.Reference;
import ast.data.template.ActualTemplateArgument;
import ast.data.template.Template;
import ast.data.type.Type;
import ast.data.type.TypeRef;
import ast.data.type.TypeRefFactory;
import ast.data.type.template.TypeTemplate;
import ast.data.variable.TemplateParameter;
import ast.interpreter.Memory;
import ast.knowledge.KnowParent;
import ast.knowledge.KnowledgeBase;
import ast.specification.IsClass;
import ast.specification.IsTypeTemplate;
import error.ErrorType;
import error.RError;

public class Specializer {
  private final Template item;
  private final AstList<ActualTemplateArgument> genspec;
  private final InstanceRepo ir;
  private final KnowledgeBase kb;

  @Deprecated
  // FIXME use process, provide evaluated
  public static Ast evalArgAndProcess(Template item, AstList<ActualTemplateArgument> genspec, InstanceRepo ir, KnowledgeBase kb) {
    evalArgType(item.getTempl(), ir, kb);

    if (!isEvaluated(genspec)) {
      genspec = evalArg(item.getTempl(), genspec, ir, kb);
    }
    return process(item, genspec, ir, kb);
  }

  public static Ast process(Template item, AstList<ActualTemplateArgument> genspec, InstanceRepo ir, KnowledgeBase kb) {
    assert (isEvaluated(genspec));
    Specializer specializer = new Specializer(item, genspec, ir, kb);
    return specializer.work();
  }

  private static boolean isEvaluated(AstList<ActualTemplateArgument> genspec) {
    for (ActualTemplateArgument ta : genspec) {
      if (!(ta instanceof Type) && !(ta instanceof ValueExpr)) {
        return false;
      }
    }
    return true;
  }

  public Specializer(Template item, AstList<ActualTemplateArgument> genspec, InstanceRepo ir, KnowledgeBase kb) {
    super();
    this.item = item;
    this.genspec = genspec;
    this.ir = ir;
    this.kb = kb;
  }

  public Ast work() {
    Ast inst = ir.find(item.getObject(), genspec);

    if (inst == null) {
      inst = createInstance();
    }

    // XXX WTF?
    while (inst instanceof Reference) {
      Reference ref = (Reference) inst;
      assert (ref.offset.isEmpty());
      inst = ref.link;
    }

    assert (!(inst instanceof Reference));

    return inst;
  }

  private Ast createInstance() {
    Ast inst;
    Ast templ = item.getObject();

    inst = eval(templ);
    ir.add(templ, genspec, inst);

    TypeEvalExecutor.eval(inst, ir, kb);

    ConstEval.process(inst, kb);

    // remove templates
    TemplDel.process(inst);
    return inst;
  }

  private Ast eval(Ast templ) {
    Ast inst;
    if (templ instanceof TypeTemplate) {
      inst = TypeTemplateSpecializer.process((TypeTemplate) templ, genspec, kb);
    } else if (templ instanceof FunctionTemplate) {
      inst = FunctionTemplateSpecializer.process((FunctionTemplate) templ, genspec, ir, kb);
    } else {
      inst = evalUser(templ);
      append(inst);
    }
    return inst;
  }

  private void append(Ast inst) {
    KnowParent kp = kb.getEntry(KnowParent.class);
    Ast parent = kp.get(item);

    // TODO create clean name

    addChild(inst, parent);
  }

  private Ast evalUser(Ast templ) {
    Ast inst;
    inst = Copy.copy(templ);

    Map<TemplateParameter, Type> typeMap = createArgMap(Type.class);
    Map<TemplateParameter, ValueExpr> valueMap = createArgMap(ValueExpr.class);

    if (typeMap.size() + valueMap.size() != genspec.size()) {
      RError.err(ErrorType.Fatal, templ.getInfo(), "Template argument missmatch");
    }

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
    return inst;
  }

  private <T extends Ast> Map<TemplateParameter, T> createArgMap(Class<T> kind) {
    IsClass isClass = new IsClass(kind);

    Map<TemplateParameter, T> valueMap = new HashMap<TemplateParameter, T>();
    for (int i = 0; i < genspec.size(); i++) {
      ActualTemplateArgument itr = genspec.get(i);
      TemplateParameter tmpl = item.getTempl().get(i);
      if (isClass.isSatisfiedBy(itr)) {
        valueMap.put(tmpl, (T) itr);
      }
    }
    return valueMap;
  }

  private void addChild(Ast inst, Ast parent) {
    if (parent instanceof Namespace) {
      ((Namespace) parent).children.add(inst);
    } else if (parent instanceof RawElementary) {
      ((RawElementary) parent).getInstantiation().add(inst);
    } else if (parent instanceof State) {
      ((State) parent).item.add((StateContent) inst);
    } else {
      throw new RuntimeException("not yet implemented: " + parent.getClass().getCanonicalName());
    }
  }

  @Deprecated
  // FIXME the provided arguments should be evaluated before calling
  static private AstList<ActualTemplateArgument> evalArg(AstList<TemplateParameter> param, AstList<ActualTemplateArgument> arg, InstanceRepo ir, KnowledgeBase kb) {
    assert (param.size() == arg.size());

    AstList<ActualTemplateArgument> ret = new AstList<ActualTemplateArgument>();

    IsTypeTemplate isType = new IsTypeTemplate();

    for (int i = 0; i < arg.size(); i++) {
      ActualTemplateArgument itr = arg.get(i);
      TemplateParameter tmpl = param.get(i);
      if (isType.isSatisfiedBy(tmpl)) {
        itr = evalType(((RefExp) itr).ref, ir, kb);
      } else {
        itr = evalExpr((Expression) itr, ir, kb);
      }
      ret.add(itr);
    }

    return ret;
  }

  private static Expression evalExpr(Expression itr, InstanceRepo ir, KnowledgeBase kb) {
    return ExprEvaluator.evaluate(itr, new Memory(), ir, kb);
  }

  private static Type evalType(Reference ref, InstanceRepo ir, KnowledgeBase kb) {
    return TypeEvaluator.evaluate(ref, new Memory(), ir, kb);
  }

  @Deprecated
  // FIXME do this before
  private static void evalArgType(AstList<TemplateParameter> templ, InstanceRepo ir, KnowledgeBase kb) {
    for (TemplateParameter param : templ) {
      param.type = evalTypeRef(param.type, ir, kb);
    }
  }

  private static TypeRef evalTypeRef(TypeRef tr, InstanceRepo ir, KnowledgeBase kb) {
    Type type = TypeEvaluator.evaluate(tr.ref, new Memory(), ir, kb);
    return TypeRefFactory.create(tr.getInfo(), type);
  }

}
