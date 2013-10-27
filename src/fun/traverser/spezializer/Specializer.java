package fun.traverser.spezializer;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import common.Designator;
import common.ElementInfo;

import error.ErrorType;
import error.RError;
import fun.expression.Expression;
import fun.expression.Number;
import fun.expression.reference.ReferenceLinked;
import fun.knowledge.KnowFunPath;
import fun.knowledge.KnowledgeBase;
import fun.other.ActualTemplateArgument;
import fun.other.Generator;
import fun.other.Named;
import fun.other.Namespace;
import fun.traverser.Memory;
import fun.type.Type;
import fun.type.base.AnyType;
import fun.type.base.IntegerType;
import fun.type.template.BuiltinTemplate;
import fun.type.template.Range;
import fun.type.template.TypeType;
import fun.variable.TemplateParameter;

public class Specializer {

  public static Named process(Generator item, List<Expression> genspec, ElementInfo info, KnowledgeBase kb) {
    List<ActualTemplateArgument> param = evalParam(item, genspec, info, kb);

    String name = makeName(item.getName(), param);
    assert (!item.getName().equals(name));

    KnowFunPath kp = kb.getEntry(KnowFunPath.class);
    Designator path = kp.get(item);
    Namespace parent = (Namespace) kb.getRoot().getChildItem(path.toList());
    Named inst = parent.find(name);
    if (inst == null) {
      if (item.getTemplate() instanceof BuiltinTemplate) {
        inst = GenericSpecializer.process((BuiltinTemplate) item.getTemplate(), param, kb);
      } else {
        inst = TypeSpecializer.evaluate(item.getParam().getList(), param, item.getTemplate(), kb);
        inst.setName(name);
      }
      assert (inst.getName().equals(name));
      parent.add(inst);
    }

    return inst;
  }

  private static List<ActualTemplateArgument> evalParam(Generator item, List<Expression> genspec, ElementInfo info, KnowledgeBase kb) {
    if (genspec.size() != item.getParam().size()) {
      RError.err(ErrorType.Error, info, "Wrong number of parameter, expected " + genspec.size() + " got " + item.getParam().size());
    }
    assert (genspec.size() == item.getParam().size());

    List<ActualTemplateArgument> ret = new ArrayList<ActualTemplateArgument>(genspec.size());

    for (int i = 0; i < genspec.size(); i++) {
      TemplateParameter pitm = item.getParam().getList().get(i);
      ReferenceLinked tref = (ReferenceLinked) pitm.getType();
      Type type = (Type) EvalTo.any(tref, kb);
      ActualTemplateArgument evald;
      Expression acarg = genspec.get(i);
      if (type instanceof Range) {
        Number num = (Number) ExprEvaluator.evaluate(acarg, new Memory(), kb);
        evald = num;
        // TODO check type
      } else if (type instanceof IntegerType) {
        Number num = (Number) ExprEvaluator.evaluate(acarg, new Memory(), kb);
        evald = num;
      } else if (type instanceof AnyType) {
        ReferenceLinked rl = (ReferenceLinked) acarg;
        evald = (Type) EvalTo.any(rl, kb);
        // TODO check type
      } else if (type instanceof TypeType) {
        ReferenceLinked rl = (ReferenceLinked) acarg;
        evald = (Type) EvalTo.any(rl, kb);
        // TODO check type
      } else {
        throw new RuntimeException("not yet implemented: " + type.getName());
      }
      // TODO check type
      ret.add(evald);
    }

    return ret;
  }

  static private String makeName(String name, List<ActualTemplateArgument> generic) {
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
    return name;
  }

}
