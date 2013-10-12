package fun.traverser.spezializer;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import fun.Copy;
import fun.FunBase;
import fun.expression.Expression;
import fun.expression.reference.ReferenceLinked;
import fun.expression.reference.ReferenceUnlinked;
import fun.generator.Generator;
import fun.knowledge.KnowledgeBase;
import fun.traverser.ExprReplacer;
import fun.traverser.Memory;
import fun.traverser.ReLinker;
import fun.traverser.TypeEvalReplacer;
import fun.variable.TemplateParameter;

/**
 * Generates a type from a unspecified TypeGenerator and its arguments
 *
 * @author urs
 *
 */
public class TypeSpecializer {

  public static <T extends FunBase> T evaluate(Generator<T> funObj, List<Expression> genspec, KnowledgeBase kb) {
    Generator<T> cobj = Copy.copy(funObj);
    /*
     * if this function has a reference to itself, the reference points to the new copied object. This is not what we
     * want since it is unspecialized
     *
     * As a workaround, we relink links to this function back to the original object
     */
    {
      Map<Generator<T>, Generator<T>> map = new HashMap<Generator<T>, Generator<T>>();
      map.put(cobj, funObj);
      ReLinker.process(cobj, map);
    }

    Memory mem = new Memory();
    Map<TemplateParameter, Expression> map = new HashMap<TemplateParameter, Expression>();
    for (int i = 0; i < genspec.size(); i++) {
      TemplateParameter var = cobj.getParam().getList().get(i);
      Expression val = genspec.get(i);
      // TODO can we ensure that val is evaluated?
      map.put(var, val);

      mem.createVar(var);
      mem.setInt(var, val);
    }

    T spec = cobj.getItem();

    TypeSpecTrav evaluator = new TypeSpecTrav();
    evaluator.traverse(spec, map);

    TypeEvalReplacer typeEvalReplacer = new TypeEvalReplacer(kb);
    typeEvalReplacer.traverse(spec, mem);

    return spec;
  }

}

/**
 * Replaces a reference to a CompfuncParameter with the value of it
 *
 * @author urs
 *
 */
class TypeSpecTrav extends ExprReplacer<Map<TemplateParameter, Expression>> {
  @Override
  protected Expression visitReferenceUnlinked(ReferenceUnlinked obj, Map<TemplateParameter, Expression> param) {
    throw new RuntimeException("not yet implemented");
  }

  @Override
  protected Expression visitReferenceLinked(ReferenceLinked obj, Map<TemplateParameter, Expression> param) {
    super.visitReferenceLinked(obj, param);

    if (param.containsKey(obj.getLink())) {
      throw new RuntimeException("not yet implemented");
//      Expression repl = param.get(obj.getLink());
//      ReferenceLinked ret = new ReferenceLinked(obj.getInfo(), );
//      return ;
    } else {
      assert (!(obj.getLink() instanceof TemplateParameter));
      return obj;
    }
  }

}
