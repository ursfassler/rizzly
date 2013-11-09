package fun.knowledge;

import java.util.HashSet;
import java.util.Set;

import common.Scope;

import error.ErrorType;
import error.RError;
import fun.Fun;
import fun.composition.ImplComposition;
import fun.expression.reference.DummyLinkTarget;
import fun.function.impl.FuncEntryExit;
import fun.function.impl.FuncGlobal;
import fun.function.impl.FuncPrivateRet;
import fun.function.impl.FuncPrivateVoid;
import fun.function.impl.FuncProtRet;
import fun.function.impl.FuncProtVoid;
import fun.hfsm.ImplHfsm;
import fun.hfsm.StateComposite;
import fun.hfsm.StateSimple;
import fun.hfsm.Transition;
import fun.other.ImplElementary;
import fun.type.base.AnyType;
import fun.type.base.BooleanType;
import fun.type.base.EnumElement;
import fun.type.base.EnumType;
import fun.type.base.IntegerType;
import fun.type.base.NaturalType;
import fun.type.base.StringType;
import fun.type.base.TypeAlias;
import fun.type.base.VoidType;
import fun.type.composed.RecordType;
import fun.type.composed.UnionType;
import fun.type.template.Array;
import fun.type.template.ArrayTemplate;
import fun.type.template.Range;
import fun.type.template.RangeTemplate;
import fun.type.template.TypeType;
import fun.type.template.TypeTypeTemplate;
import fun.variable.CompUse;
import fun.variable.ConstGlobal;
import fun.variable.ConstPrivate;
import fun.variable.FuncVariable;
import fun.variable.StateVariable;
import fun.variable.TemplateParameter;

public class KnowScope extends KnowledgeEntry {
  @SuppressWarnings("rawtypes")
  static final private Set<Class> global = new HashSet<Class>();
  @SuppressWarnings("rawtypes")
  static final private Set<Class> local = new HashSet<Class>();
  @SuppressWarnings("rawtypes")
  static final private Set<Class> priv = new HashSet<Class>();

  static {
    global.add(ConstGlobal.class);
    global.add(FuncGlobal.class);

    global.add(IntegerType.class);
    global.add(ArrayTemplate.class);
    global.add(Range.class);
    global.add(RangeTemplate.class);
    global.add(NaturalType.class);
    global.add(TypeTypeTemplate.class);
    global.add(AnyType.class);
    global.add(VoidType.class);
    global.add(RecordType.class);
    global.add(UnionType.class);
    global.add(Array.class);
    global.add(StringType.class);
    global.add(TypeAlias.class);
    global.add(TypeType.class);
    global.add(EnumType.class);
    global.add(BooleanType.class);
    global.add(EnumElement.class);
    global.add(ImplElementary.class);
    global.add(ImplComposition.class);
    global.add(ImplHfsm.class);
    global.add(DummyLinkTarget.class);

    local.add(FuncVariable.class);
    local.add(TemplateParameter.class);
    local.add(FuncEntryExit.class); // TODO: sure?

    priv.add(CompUse.class);
    priv.add(StateVariable.class);
    priv.add(ConstPrivate.class);
    priv.add(StateComposite.class);
    priv.add(StateSimple.class);
    priv.add(Transition.class);
    priv.add(FuncPrivateVoid.class);
    priv.add(FuncPrivateRet.class);
    priv.add(FuncProtVoid.class); // TODO: sure?
    priv.add(FuncProtRet.class); // TODO: sure?

    {
      @SuppressWarnings("rawtypes")
      Set<Class> all = new HashSet<Class>();
      all.addAll(global);
      all.addAll(local);
      all.addAll(priv);
      assert (all.size() == (global.size() + local.size() + priv.size()));
    }
  }

  @Override
  public void init(KnowledgeBase base) {
  }

  static public Scope get(Fun obj) {
    if (global.contains(obj.getClass())) {
      return Scope.global;
    } else if (local.contains(obj.getClass())) {
      return Scope.local;
    } else if (priv.contains(obj.getClass())) {
      return Scope.privat;
    } else {
      RError.err(ErrorType.Fatal, obj.getInfo(), "Unhandled class: " + obj.getClass().getCanonicalName());
      return null;
    }
  }

}
