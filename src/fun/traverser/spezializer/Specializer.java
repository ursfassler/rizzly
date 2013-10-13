package fun.traverser.spezializer;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import common.Designator;
import common.ElementInfo;

import error.ErrorType;
import error.RError;
import fun.Fun;
import fun.FunBase;
import fun.NullTraverser;
import fun.expression.Expression;
import fun.expression.Number;
import fun.expression.reference.Reference;
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
import fun.other.NamedComponent;
import fun.other.NamedInterface;
import fun.other.Namespace;
import fun.traverser.Memory;
import fun.type.NamedType;
import fun.type.Type;
import fun.type.base.AnyType;
import fun.type.base.BaseType;
import fun.type.base.IntegerType;
import fun.type.template.Range;
import fun.type.template.TypeType;
import fun.variable.TemplateParameter;

public class Specializer {

  public static NamedType processType(TypeGenerator item, List<Expression> genspec, ElementInfo info, KnowledgeBase kb) {
    List<ActualTemplateArgument> param = evalParam(item, genspec, info, kb);

    String name = makeName(item.getName(), param);
    assert (!item.getName().equals(name));

    KnowFunPath kp = kb.getEntry(KnowFunPath.class);
    Designator path = kp.get(item);
    Namespace parent = (Namespace) kb.getRoot().getChildItem(path.toList());
    NamedType inst = (NamedType) parent.find(name);
    if (inst == null) {
      Type rawret;
      if (item.getItem() instanceof BaseType) {
        rawret = GenericSpecializer.process((TypeGenerator) item, param, kb);
      } else {
        rawret = TypeSpecializer.evaluate(item, param, kb);
      }
      inst = new NamedType(new ElementInfo(), name, (Type) rawret);
      parent.add(inst);
    }

    assert (inst.getName().equals(name));

    return inst;
  }

  public static NamedInterface processIface(InterfaceGenerator item, List<Expression> genspec, ElementInfo info, KnowledgeBase kb) {
    List<ActualTemplateArgument> param = evalParam(item, genspec, info, kb);

    String name = makeName(item.getName(), param);
    assert (!item.getName().equals(name));

    KnowFunPath kp = kb.getEntry(KnowFunPath.class);
    Designator path = kp.get(item);
    Namespace parent = (Namespace) kb.getRoot().getChildItem(path.toList());
    NamedInterface inst = (NamedInterface) parent.find(name);
    if (inst == null) {
      Interface rawret;
      rawret = TypeSpecializer.evaluate(item, param, kb);
      inst = new NamedInterface(new ElementInfo(), name, rawret);
      parent.add(inst);
    }

    assert (inst.getName().equals(name));

    return inst;
  }

  public static NamedComponent processComp(ComponentGenerator item, List<Expression> genspec, ElementInfo info, KnowledgeBase kb) {
    List<ActualTemplateArgument> param = evalParam(item, genspec, info, kb);

    String name = makeName(item.getName(), param);
    assert (!item.getName().equals(name));

    KnowFunPath kp = kb.getEntry(KnowFunPath.class);
    Designator path = kp.get(item);
    Namespace parent = (Namespace) kb.getRoot().getChildItem(path.toList());
    NamedComponent inst = (NamedComponent) parent.find(name);
    if (inst == null) {
      Component rawret;
      rawret = TypeSpecializer.evaluate(item, param, kb);
      inst = new NamedComponent(new ElementInfo(), name, rawret);
      parent.add(inst);
    }

    assert (inst.getName().equals(name));

    return inst;
  }

  private static List<ActualTemplateArgument> evalParam(Generator<? extends FunBase> item, List<Expression> genspec, ElementInfo info, KnowledgeBase kb) {
    if (genspec.size() != item.getParam().size()) {
      RError.err(ErrorType.Error, info, "Wrong number of parameter, expected " + genspec.size() + " got " + item.getParam().size());
    }
    assert (genspec.size() == item.getParam().size());

    List<ActualTemplateArgument> ret = new ArrayList<ActualTemplateArgument>(genspec.size());

    for (int i = 0; i < genspec.size(); i++) {
      TemplateParameter pitm = item.getParam().getList().get(i);
      ReferenceLinked tref = (ReferenceLinked) pitm.getType();
      NamedType ntype = EvalTo.type(tref, kb);
      ActualTemplateArgument evald;
      Type type = ntype.getType();
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
        throw new RuntimeException("not yet implemented: " + ntype.getName());
      }
      // TODO check type
      ret.add(evald);
    }

    return ret;
  }

  static private String makeName(String name, List<ActualTemplateArgument> generic) {
    name += "{";
    for (ActualTemplateArgument expr : generic) {
      name += Designator.NAME_SEP;
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

class SpecDispatcher extends NullTraverser<Fun, List<Expression>> {

  public static Fun execute(Reference type, KnowledgeBase kb) {
    SpecDispatcher dispatcher = new SpecDispatcher();
    return dispatcher.traverse(type, null);
  }

  @Override
  protected Fun visitDefault(Fun obj, List<Expression> param) {
    throw new RuntimeException("not yet implemented: " + obj.getClass().getCanonicalName());
  }

}
