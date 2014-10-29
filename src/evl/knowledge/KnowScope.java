package evl.knowledge;

import java.util.HashSet;
import java.util.Set;

import util.Range;

import common.Scope;

import error.ErrorType;
import error.RError;
import evl.Evl;
import evl.composition.ImplComposition;
import evl.function.header.FuncCtrlInDataIn;
import evl.function.header.FuncCtrlInDataOut;
import evl.function.header.FuncCtrlOutDataIn;
import evl.function.header.FuncCtrlOutDataOut;
import evl.function.header.FuncGlobal;
import evl.function.header.FuncPrivateRet;
import evl.function.header.FuncPrivateVoid;
import evl.function.header.FuncSubHandlerEvent;
import evl.function.header.FuncSubHandlerQuery;
import evl.hfsm.ImplHfsm;
import evl.hfsm.StateComposite;
import evl.hfsm.StateSimple;
import evl.hfsm.Transition;
import evl.other.ImplElementary;
import evl.other.Queue;
import evl.type.base.ArrayType;
import evl.type.base.BooleanType;
import evl.type.base.EnumElement;
import evl.type.base.EnumType;
import evl.type.base.StringType;
import evl.type.composed.RecordType;
import evl.type.composed.UnionType;
import evl.type.special.VoidType;
import evl.variable.ConstGlobal;
import evl.variable.ConstPrivate;
import evl.variable.FuncVariable;
import evl.variable.StateVariable;
import fun.variable.CompUse;

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

    global.add(Range.class);
    global.add(VoidType.class);
    global.add(RecordType.class);
    global.add(UnionType.class);
    global.add(ArrayType.class);
    global.add(StringType.class);
    global.add(EnumType.class);
    global.add(BooleanType.class);
    global.add(EnumElement.class);
    global.add(ImplElementary.class);
    global.add(ImplComposition.class);
    global.add(ImplHfsm.class);

    global.add(FuncCtrlInDataIn.class);
    global.add(FuncCtrlOutDataOut.class);
    global.add(FuncCtrlInDataOut.class);
    global.add(FuncCtrlOutDataIn.class);

    priv.add(Queue.class);
    priv.add(CompUse.class);
    priv.add(StateVariable.class);
    priv.add(ConstPrivate.class);
    priv.add(FuncPrivateRet.class);
    priv.add(FuncPrivateVoid.class);
    priv.add(FuncSubHandlerQuery.class); // TODO: sure?
    priv.add(FuncSubHandlerEvent.class); // TODO: sure?
    priv.add(StateComposite.class);
    priv.add(StateSimple.class);
    priv.add(Transition.class);

    local.add(FuncVariable.class);

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

  static public Scope get(Evl obj) {
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
