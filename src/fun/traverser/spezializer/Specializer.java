package fun.traverser.spezializer;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import common.Designator;
import common.ElementInfo;

import error.ErrorType;
import error.RError;
import fun.Copy;
import fun.Fun;
import fun.NullTraverser;
import fun.expression.Expression;
import fun.expression.Number;
import fun.expression.reference.DummyLinkTarget;
import fun.expression.reference.RefTemplCall;
import fun.expression.reference.Reference;
import fun.knowledge.KnowFunPath;
import fun.knowledge.KnowledgeBase;
import fun.other.ActualTemplateArgument;
import fun.other.Generator;
import fun.other.Named;
import fun.other.Namespace;
import fun.traverser.ConstEval;
import fun.traverser.ExprReplacer;
import fun.traverser.Memory;
import fun.traverser.TypeEvalReplacer;
import fun.type.Type;
import fun.type.base.AnyType;
import fun.type.base.IntegerType;
import fun.type.base.NaturalType;
import fun.type.base.TypeAlias;
import fun.type.template.BuiltinTemplate;
import fun.type.template.Range;
import fun.type.template.TypeType;
import fun.variable.Constant;
import fun.variable.TemplateParameter;

//TODO: cleanup
//TODO: rename to instantiator?
public class Specializer {

  public static Named process(Generator item, List<Expression> genspec, ElementInfo info, KnowledgeBase kb) {
    List<ActualTemplateArgument> param = evalParam(item, genspec, info, kb);

    String name = makeName(item.getName(), param);

    KnowFunPath kp = kb.getEntry(KnowFunPath.class);
    Designator path = kp.find(item);
    Namespace parent = (Namespace) kb.getRoot().forceChildPath(path.toList());
    Named inst = parent.find(name);
    if (inst == null) {
      if (item instanceof BuiltinTemplate) {
        inst = GenericSpecializer.process((BuiltinTemplate) item, param, kb);
      } else {
        inst = instantiate(item, param, kb);
        inst.setName(name);
      }
      assert (inst.getName().equals(name));
      parent.add(inst);
    }

    // TODO spare the ones we already checked
    TypeEvalReplacer typeEvalReplacer = new TypeEvalReplacer(kb);
    typeEvalReplacer.traverse(inst, null);

    ConstEval.process(inst, kb);

    return inst;
  }

  private static List<ActualTemplateArgument> evalParam(Generator item, List<Expression> genspec, ElementInfo info, KnowledgeBase kb) {
    if (genspec.size() != item.getTemplateParam().size()) {
      RError.err(ErrorType.Error, info, "Wrong number of parameter, expected " + item.getTemplateParam().size() + " got " + genspec.size());
    }
    assert (genspec.size() == item.getTemplateParam().size());

    List<ActualTemplateArgument> ret = new ArrayList<ActualTemplateArgument>(genspec.size());

    for (int i = 0; i < genspec.size(); i++) {
      Reference tref = item.getTemplateParam().getList().get(i).getType();
      Expression acarg = genspec.get(i);
      Type type = evalType(tref, kb);
      ActualTemplateArgument evald = ArgEvaluator.process(type, acarg, kb);
      ret.add(evald);
    }

    return ret;
  }

  private static Type evalType(Reference tref, KnowledgeBase kb) {
    Type type;
    if (tref.getLink() instanceof TemplateParameter) {
      // we use a previous defined parameter, it has to be a "Type{*}" argument
      TemplateParameter pitm;
      pitm = (TemplateParameter) tref.getLink();
      tref = pitm.getType();
      Named any = eval(tref, kb);
      assert (any instanceof TypeType);
      any = eval(((TypeType) any).getType(), kb);
      type = (Type) any;
    } else {
      Named any = eval(tref, kb);
      type = (Type) any;
    }
    return type;
  }

  static Named eval(Reference obj, KnowledgeBase kb) {
    if (obj.getLink() instanceof Constant) {
      assert (false);
    }

    if (!(obj.getLink() instanceof Generator)) {
      assert (obj.getOffset().isEmpty());
      return obj.getLink();
    }
    Generator generator = (Generator) obj.getLink();
    if (obj.getOffset().isEmpty() || !(obj.getOffset().get(0) instanceof RefTemplCall)) {
      assert (generator.getTemplateParam().isEmpty());
      return generator;
    }
    assert (obj.getOffset().size() == 1);
    assert (obj.getOffset().get(0) instanceof RefTemplCall);

    List<Expression> actparam = ((RefTemplCall) obj.getOffset().get(0)).getActualParameter();

    return Specializer.process(generator, actparam, obj.getInfo(), kb);
  }

  static private String makeName(String name, List<ActualTemplateArgument> generic) {
    if (!generic.isEmpty()) {
      name += "{";
      boolean first = true;
      for (ActualTemplateArgument expr : generic) {
        if (first) {
          first = false;
        } else {
          name += ",";
        }
        if (expr instanceof Number) {
          BigInteger ref = ((Number) expr).getValue();
          name += ref.toString();
        } else if (expr instanceof Named) {
          name += ((Named) expr).getName();
        } else {
          throw new RuntimeException("not yet implemented: " + expr.getClass().getCanonicalName());
        }
      }
      name += "}";
    }
    return name;
  }

  private static Generator instantiate(Generator template, List<ActualTemplateArgument> genspec, KnowledgeBase kb) {
    List<TemplateParameter> param = template.getTemplateParam().getList();
    template = Copy.copy(template);

    Map<TemplateParameter, ActualTemplateArgument> map = new HashMap<TemplateParameter, ActualTemplateArgument>();
    for (int i = 0; i < genspec.size(); i++) {
      TemplateParameter var = param.get(i);
      ActualTemplateArgument val = genspec.get(i);
      // TODO can we ensure that val is evaluated?
      map.put(var, val);
    }

    TypeSpecTrav evaluator = new TypeSpecTrav();
    evaluator.traverse(template, map);

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
  protected Expression visitReference(Reference obj, Map<TemplateParameter, ActualTemplateArgument> param) {
    assert (!(obj.getLink() instanceof DummyLinkTarget));
    super.visitReference(obj, param);

    if (param.containsKey(obj.getLink())) {
      ActualTemplateArgument repl = param.get(obj.getLink());
      if (repl instanceof Type) {
        return new Reference(obj.getInfo(), (Type) repl);
      } else {
        return (Expression) repl;
      }
    } else {
      assert (!(obj.getLink() instanceof TemplateParameter));
      return obj;
    }
  }

}

class ArgEvaluator extends NullTraverser<ActualTemplateArgument, Expression> {
  final private KnowledgeBase kb;

  public ArgEvaluator(KnowledgeBase kb) {
    super();
    this.kb = kb;
  }

  public static ActualTemplateArgument process(Type type, Expression acarg, KnowledgeBase kb) {
    ArgEvaluator argEvaluator = new ArgEvaluator(kb);
    return argEvaluator.traverse(type, acarg);
  }

  @Override
  protected ActualTemplateArgument visitDefault(Fun obj, Expression param) {
    throw new RuntimeException("not yet implemented: " + obj.getClass().getCanonicalName());
  }

  @Override
  protected ActualTemplateArgument visitIntegerType(IntegerType obj, Expression param) {
    Number num = (Number) ExprEvaluator.evaluate(param, new Memory(), kb);
    return num;
  }

  @Override
  protected ActualTemplateArgument visitNaturalType(NaturalType obj, Expression param) {
    Number num = (Number) ExprEvaluator.evaluate(param, new Memory(), kb);
    if (num.getValue().compareTo(BigInteger.ZERO) < 0) {
      RError.err(ErrorType.Error, param.getInfo(), "Value for Natural type has to be >= 0");
    }
    return num;
  }

  @Override
  protected ActualTemplateArgument visitRange(Range obj, Expression param) {
    Number num = (Number) ExprEvaluator.evaluate(param, new Memory(), kb);
    // TODO check type
    return num;
  }

  @Override
  protected ActualTemplateArgument visitAnyType(AnyType obj, Expression param) {
    Type evald = (Type) Specializer.eval((Reference) param, kb);
    return evald;
  }

  @Override
  protected ActualTemplateArgument visitTypeAlias(TypeAlias obj, Expression param) {
    ActualTemplateArgument evald = (ActualTemplateArgument) ExprEvaluator.evaluate(param, new Memory(), kb);
    // TODO check type
    return evald;
  }

  @Override
  protected ActualTemplateArgument visitTypeType(TypeType obj, Expression param) {
    ActualTemplateArgument evald = (ActualTemplateArgument) Specializer.eval((Reference) param, kb);
    // TODO check type
    return evald;
  }

}
