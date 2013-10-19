package fun.traverser.spezializer;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import fun.Copy;
import fun.Fun;
import fun.expression.Expression;
import fun.expression.reference.ReferenceLinked;
import fun.expression.reference.ReferenceUnlinked;
import fun.knowledge.KnowledgeBase;
import fun.other.ActualTemplateArgument;
import fun.traverser.ExprReplacer;
import fun.traverser.Memory;
import fun.traverser.TypeEvalReplacer;
import fun.type.Type;
import fun.variable.TemplateParameter;

/**
 * Generates a type from a unspecified TypeGenerator and its arguments
 * 
 * @author urs
 * 
 */
public class TypeSpecializer {

  public static <T extends Fun> T evaluate(List<TemplateParameter> param, List<ActualTemplateArgument> genspec, T template, KnowledgeBase kb) {
    template = Copy.copy(template);

    Memory mem = new Memory();
    Map<TemplateParameter, ActualTemplateArgument> map = new HashMap<TemplateParameter, ActualTemplateArgument>();
    for (int i = 0; i < genspec.size(); i++) {
      TemplateParameter var = param.get(i);
      ActualTemplateArgument val = genspec.get(i);
      // TODO can we ensure that val is evaluated?
      map.put(var, val);

      if (val instanceof Expression) { // TODO ok?
        mem.createVar(var);
        mem.setInt(var, (Expression) val);
      }
    }

    TypeSpecTrav evaluator = new TypeSpecTrav();
    evaluator.traverse(template, map);

    TypeEvalReplacer typeEvalReplacer = new TypeEvalReplacer(kb);
    typeEvalReplacer.traverse(template, mem);

    return template;
  }

}

/**
 * Replaces a reference to a CompfuncParameter with the value of it
 * 
 * @author urs
 * 
 */
class TypeSpecTrav extends ExprReplacer<Map<TemplateParameter, ActualTemplateArgument>> {
  @Override
  protected Expression visitReferenceUnlinked(ReferenceUnlinked obj, Map<TemplateParameter, ActualTemplateArgument> param) {
    throw new RuntimeException("not yet implemented");
  }

  @Override
  protected Expression visitReferenceLinked(ReferenceLinked obj, Map<TemplateParameter, ActualTemplateArgument> param) {
    super.visitReferenceLinked(obj, param);

    if (param.containsKey(obj.getLink())) {
      ActualTemplateArgument repl = param.get(obj.getLink());
      if (repl instanceof Type) {
        return new ReferenceLinked(obj.getInfo(), (Type) repl);
      } else {
        return (Expression) repl;
      }
    } else {
      assert (!(obj.getLink() instanceof TemplateParameter));
      return obj;
    }
  }

}
