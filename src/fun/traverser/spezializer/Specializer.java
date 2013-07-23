package fun.traverser.spezializer;

import java.util.List;

import common.Designator;
import common.ElementInfo;

import fun.expression.Expression;
import fun.expression.Number;
import fun.generator.ComponentGenerator;
import fun.generator.InterfaceGenerator;
import fun.generator.TypeGenerator;
import fun.knowledge.KnowFunPath;
import fun.knowledge.KnowledgeBase;
import fun.other.Component;
import fun.other.Interface;
import fun.other.Named;
import fun.other.NamedComponent;
import fun.other.NamedInterface;
import fun.other.Namespace;
import fun.traverser.Memory;
import fun.type.NamedType;
import fun.type.Type;
import fun.type.base.BaseType;

public class Specializer {

  public static NamedType processType(TypeGenerator item, List<Expression> genspec, KnowledgeBase kb) {
    assert (genspec.size() == item.getParam().size());

    for (int i = 0; i < genspec.size(); i++) {
      Expression nexpr = ExprEvaluator.evaluate(genspec.get(i), new Memory(), kb);
      genspec.set(i, nexpr);
    }

    String name = makeName(item.getName(), genspec);
    assert (!item.getName().equals(name));

    KnowFunPath kp = kb.getEntry(KnowFunPath.class);
    Designator path = kp.get(item);
    Namespace parent = (Namespace) kb.getRoot().getChildItem(path.toList());
    NamedType inst = (NamedType) parent.find(name);
    if (inst == null) {
      Type rawret;
      if (item.getItem() instanceof BaseType) {
        rawret = GenericSpecializer.process((TypeGenerator) item, genspec, kb);
      } else {
        rawret = TypeSpecializer.evaluate(item, genspec, kb);
      }
      inst = new NamedType(new ElementInfo(), name, (Type) rawret);
      parent.add(inst);
    }

    assert (inst.getName().equals(name));

    return inst;
  }

  public static NamedInterface processIface(InterfaceGenerator item, List<Expression> genspec, KnowledgeBase kb) {
    assert (genspec.size() == item.getParam().size());

    for (int i = 0; i < genspec.size(); i++) {
      Expression nexpr = ExprEvaluator.evaluate(genspec.get(i), new Memory(), kb);
      genspec.set(i, nexpr);
    }

    String name = makeName(item.getName(), genspec);
    assert (!item.getName().equals(name));

    KnowFunPath kp = kb.getEntry(KnowFunPath.class);
    Designator path = kp.get(item);
    Namespace parent = (Namespace) kb.getRoot().getChildItem(path.toList());
    NamedInterface inst = (NamedInterface) parent.find(name);
    if (inst == null) {
      Interface rawret;
      rawret = TypeSpecializer.evaluate(item, genspec, kb);
      inst = new NamedInterface(new ElementInfo(), name, rawret);
      parent.add(inst);
    }

    assert (inst.getName().equals(name));

    return inst;
  }

  public static NamedComponent processComp(ComponentGenerator item, List<Expression> genspec, KnowledgeBase kb) {
    assert (genspec.size() == item.getParam().size());

    for (int i = 0; i < genspec.size(); i++) {
      Expression nexpr = ExprEvaluator.evaluate(genspec.get(i), new Memory(), kb);
      genspec.set(i, nexpr);
    }

    String name = makeName(item.getName(), genspec);
    assert (!item.getName().equals(name));

    KnowFunPath kp = kb.getEntry(KnowFunPath.class);
    Designator path = kp.get(item);
    Namespace parent = (Namespace) kb.getRoot().getChildItem(path.toList());
    NamedComponent inst = (NamedComponent) parent.find(name);
    if (inst == null) {
      Component rawret;
      rawret = TypeSpecializer.evaluate(item, genspec, kb);
      inst = new NamedComponent(new ElementInfo(), name, rawret);
      parent.add(inst);
    }

    assert (inst.getName().equals(name));

    return inst;
  }

  static private String makeName(String name, List<Expression> generic) {
    name += "{";
    for (Expression expr : generic) {
      name += Designator.NAME_SEP;
      if (expr instanceof Number) {
        int ref = ((Number) expr).getValue();
        name += Integer.toString(ref);
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
