package evl.hfsm.reduction;

import java.util.ArrayList;

import common.Designator;
import common.ElementInfo;

import evl.Evl;
import evl.NullTraverser;
import evl.hfsm.ImplHfsm;
import evl.knowledge.KnowledgeBase;
import evl.other.Namespace;
import evl.type.Type;
import evl.type.TypeRef;
import evl.variable.StateVariable;

public class HfsmToFsm extends NullTraverser<Void, Namespace> {
  KnowledgeBase kb;

  public HfsmToFsm(KnowledgeBase kb) {
    super();
    this.kb = kb;
  }

  public static void process(Namespace classes, KnowledgeBase kb) {
    HfsmToFsm reduction = new HfsmToFsm(kb);
    reduction.visitItr(classes, null);
  }

  @Override
  protected Void visitDefault(Evl obj, Namespace param) {
    return null;
  }

  @Override
  protected Void visitItr(Iterable<? extends Evl> list, Namespace param) {
    // against comodification error
    ArrayList<Evl> old = new ArrayList<Evl>();
    for (Evl itr : list) {
      old.add(itr);
    }
    return super.visitItr(old, param);
  }

  @Override
  protected Void visitNamespace(Namespace obj, Namespace param) {
    visitItr(obj, obj);
    return null;
  }

  @Override
  protected Void visitImplHfsm(ImplHfsm obj, Namespace param) {

    QueryDownPropagator.process(obj, kb);

    TransitionRedirecter.process(obj.getTopstate());
    TransitionDownPropagator.process(obj, kb);

    obj.getTopstate().setInitial(InitStateGetter.get(obj.getTopstate()));

    {
      StateVarPathGetter svpg = new StateVarPathGetter();
      svpg.process(obj);
      StateTypeBuilder stb = new StateTypeBuilder(param);
      Type stateType = stb.traverse(obj.getTopstate(), new Designator(obj.getName()));
      StateVariable var = new StateVariable(new ElementInfo(), "data", new TypeRef(new ElementInfo(), stateType) );
      obj.getTopstate().getVariable().add(var);
      StateVarReplacer.process(obj, var, svpg.getVpath());
    }

    EntryExitUpdater.process(obj);
    StateFuncUplifter.process(obj);
    TransitionUplifter.process(obj);
    LeafStateUplifter.process(obj);

    return null;
  }

}
