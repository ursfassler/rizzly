package fun.traverser.spezializer;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import fun.Copy;
import fun.Fun;
import fun.expression.Expression;
import fun.expression.reference.BaseRef;
import fun.expression.reference.RefTemplCall;
import fun.expression.reference.Reference;
import fun.hfsm.State;
import fun.hfsm.StateContent;
import fun.knowledge.KnowInstance;
import fun.knowledge.KnowParent;
import fun.knowledge.KnowledgeBase;
import fun.other.ActualTemplateArgument;
import fun.other.CompImpl;
import fun.other.FunList;
import fun.other.Namespace;
import fun.other.Template;
import fun.traverser.ConstEval;
import fun.traverser.Memory;
import fun.type.Type;
import fun.type.template.TypeTemplate;
import fun.type.template.TypeType;
import fun.variable.Constant;
import fun.variable.TemplateParameter;

//TODO rethink it; make it clean
public class Specializer {

  public static Fun process(Template item, List<ActualTemplateArgument> genspec, KnowledgeBase kb) {
    for (int i = 0; i < genspec.size(); i++) {
      ActualTemplateArgument itr = genspec.get(i);
      itr = eval(kb, itr);
      genspec.set(i, itr);
    }

    Fun templ = item.getObject();

    KnowInstance ki = kb.getEntry(KnowInstance.class);
    Fun inst = ki.find(templ, genspec);

    if (inst == null) {
      if (templ instanceof TypeTemplate) {
        inst = GenericSpecializer.process((TypeTemplate) templ, genspec, kb);
        // TODO create clean name
      } else {
        inst = Copy.copy(templ);

        TypeSpecTrav evaluator = new TypeSpecTrav();
        Expression ri = evaluator.traverse(inst, makeMap(genspec, item.getTempl()));

        // if inst is a reference, the new one is returned
        if (ri != null) {
          inst = ri;
        }

        KnowParent kp = kb.getEntry(KnowParent.class);
        Fun parent = kp.get(item);

        // TODO create clean name

        addChild(inst, parent);
      }

      ki.add(templ, genspec, inst);

      // evaluate
      TypeEvalReplacer typeEvalReplacer = new TypeEvalReplacer(kb);
      typeEvalReplacer.traverse(inst, null);

      ConstEval.process(inst, kb);

      // remove templates
      TemplDel.process(inst);
    }

    while (inst instanceof Reference) {
      Reference ref = (Reference) inst;
      assert (ref.getOffset().isEmpty());
      inst = ref.getLink();
    }

    assert (!(inst instanceof BaseRef));

    return inst;
  }

  private static String makeName(Template item) {
    return item.getName();
  }

  private static ActualTemplateArgument eval(KnowledgeBase kb, ActualTemplateArgument itr) {
    if (itr instanceof Expression) {
      itr = ExprEvaluator.evaluate((Expression) itr, new Memory(), kb);
    }
    while (itr instanceof Reference) {
      Reference ref = (Reference) itr;
      assert (ref.getOffset().isEmpty());
      itr = (ActualTemplateArgument) ref.getLink();
    }
    return itr;
  }

  static ActualTemplateArgument eval(ActualTemplateArgument actualTemplateArgument, KnowledgeBase kb) {
    throw new RuntimeException("not yet implemented");
  }

  private static void addChild(Fun inst, Fun parent) {
    if (parent instanceof Namespace) {
      ((Namespace) parent).getChildren().add(inst);
    } else if (parent instanceof CompImpl) {
      ((CompImpl) parent).getObjects().add(inst);
    } else if (parent instanceof State) {
      ((State) parent).getItemList().add((StateContent) inst);
    } else {
      throw new RuntimeException("not yet implemented: " + parent.getClass().getCanonicalName());
    }
  }

  private static Map<TemplateParameter, ActualTemplateArgument> makeMap(List<ActualTemplateArgument> param, FunList<TemplateParameter> param1) {
    Map<TemplateParameter, ActualTemplateArgument> map = new HashMap<TemplateParameter, ActualTemplateArgument>();
    for (int i = 0; i < param.size(); i++) {
      TemplateParameter var = param1.get(i);
      ActualTemplateArgument val = param.get(i);
      map.put(var, val);
    }
    return map;
  }

  static Type evalType(Reference tref, KnowledgeBase kb) {
    Type type;
    if (tref.getLink() instanceof TemplateParameter) {
      // we use a previous defined parameter, it has to be a "Type{*}" argument
      TemplateParameter pitm;
      pitm = (TemplateParameter) tref.getLink();
      tref = pitm.getType();
      Fun any = eval(tref, kb);
      assert (any instanceof TypeType);
      any = eval(((TypeType) any).getType(), kb);
      type = (Type) any;
    } else if (tref.getLink() instanceof Type) {
      assert (tref.getOffset().isEmpty());
      return (Type) tref.getLink();
    } else {
      Fun any = eval(tref, kb);
      type = (Type) any;
    }
    return type;
  }

  private static Fun eval(Reference obj, KnowledgeBase kb) {
    if (obj.getLink() instanceof Constant) {
      assert (false);
    }

    if (obj.getLink() instanceof Template) {
      Template generator = (Template) obj.getLink();

      FunList<ActualTemplateArgument> actparam;
      if (obj.getOffset().isEmpty() || !(obj.getOffset().get(0) instanceof RefTemplCall)) {
        assert (generator.getTempl().isEmpty());
        actparam = new FunList<ActualTemplateArgument>();
      } else {
        assert (obj.getOffset().size() == 1);
        assert (obj.getOffset().get(0) instanceof RefTemplCall);
        actparam = ((RefTemplCall) obj.getOffset().get(0)).getActualParameter();
      }

      return Specializer.process(generator, actparam, kb);
    } else if (obj.getLink() instanceof TypeTemplate) {
      TypeTemplate generator = (TypeTemplate) obj.getLink();
      assert (obj.getOffset().size() == 1);
      assert (obj.getOffset().get(0) instanceof RefTemplCall);

      List<ActualTemplateArgument> actparam = ((RefTemplCall) obj.getOffset().get(0)).getActualParameter();
      return GenericSpecializer.process(generator, actparam, kb);
    } else if (obj.getLink() instanceof Type) {
      assert (obj.getOffset().isEmpty());
      return obj.getLink();
    } else {
      throw new RuntimeException("not yet implemented: " + obj.getLink().getClass().getCanonicalName());
      // if (!(obj.getLink() instanceof Declaration)) {
      // assert (obj.getOffset().isEmpty());
      // return obj.getLink();
      // }
    }
  }

}
