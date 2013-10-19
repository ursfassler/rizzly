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
import fun.generator.ComponentGenerator;
import fun.generator.Generator;
import fun.generator.InterfaceGenerator;
import fun.generator.TypeGenerator;
import fun.knowledge.KnowFunPath;
import fun.knowledge.KnowledgeBase;
import fun.other.ActualTemplateArgument;
import fun.other.Component;
import fun.other.Interface;
import fun.other.Named;
import fun.other.Namespace;
import fun.traverser.Memory;
import fun.type.Type;
import fun.type.base.AnyType;
import fun.type.base.IntegerType;
import fun.type.template.Range;
import fun.type.template.TypeType;
import fun.type.template.UserTypeGenerator;
import fun.variable.TemplateParameter;

public class Specializer {

  public static Type processType(TypeGenerator item, List<Expression> genspec, ElementInfo info, KnowledgeBase kb) {
    List<ActualTemplateArgument> param = evalParam(item, genspec, info, kb);

    String name = makeName(item.getName(), param);
    assert (!item.getName().equals(name));

    KnowFunPath kp = kb.getEntry(KnowFunPath.class);
    Designator path = kp.get(item);
    Namespace parent = (Namespace) kb.getRoot().getChildItem(path.toList());
    Type inst = (Type) parent.find(name);
    if (inst == null) {
      if (item instanceof UserTypeGenerator) {
        inst = TypeSpecializer.evaluate(item.getParam().getList(), param, ((UserTypeGenerator) item).getTemplate(), kb);
        inst.setName(name);
      } else {
        inst = GenericSpecializer.process(item, param, kb);
      }
      assert (inst.getName().equals(name));
      parent.add(inst);
    }

    return inst;
  }

  public static Interface processIface(InterfaceGenerator item, List<Expression> genspec, ElementInfo info, KnowledgeBase kb) {
    List<ActualTemplateArgument> param = evalParam(item, genspec, info, kb);

    String name = makeName(item.getName(), param);
    assert (!item.getName().equals(name));

    KnowFunPath kp = kb.getEntry(KnowFunPath.class);
    Designator path = kp.get(item);
    Namespace parent = (Namespace) kb.getRoot().getChildItem(path.toList());
    Interface inst = (Interface) parent.find(name);
    if (inst == null) {
      inst = TypeSpecializer.evaluate(item.getParam().getList(), param, item.getTemplate(), kb);
      inst.setName(name);
      assert (inst.getName().equals(name));
      parent.add(inst);
    }

    return inst;
  }

  public static Component processComp(ComponentGenerator item, List<Expression> genspec, ElementInfo info, KnowledgeBase kb) {
    List<ActualTemplateArgument> param = evalParam(item, genspec, info, kb);

    String name = makeName(item.getName(), param);
    assert (!item.getName().equals(name));

    KnowFunPath kp = kb.getEntry(KnowFunPath.class);
    Designator path = kp.get(item);
    Namespace parent = (Namespace) kb.getRoot().getChildItem(path.toList());
    Component inst = (Component) parent.find(name);
    if (inst == null) {
      inst = TypeSpecializer.evaluate(item.getParam().getList(), param, item.getTemplate(), kb);
      inst.setName(name);
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
      Type type = EvalTo.type(tref, kb);
      ActualTemplateArgument evald;
      if (type instanceof Range) {
        Number num = (Number) ExprEvaluator.evaluate(genspec.get(i), new Memory(), kb);
        evald = num;
        // TODO check type
      } else if (type instanceof IntegerType) {
        Number num = (Number) ExprEvaluator.evaluate(genspec.get(i), new Memory(), kb);
        evald = num;
      } else if (type instanceof AnyType) {
        ReferenceLinked rl = (ReferenceLinked) genspec.get(i);
        evald = EvalTo.type(rl, kb);
        // TODO check type
      } else if (type instanceof TypeType) {
        ReferenceLinked rl = (ReferenceLinked) genspec.get(i);
        evald = EvalTo.type(rl, kb);
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
