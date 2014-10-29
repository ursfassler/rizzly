package evl.hfsm.reduction;

import java.util.Map;

import common.ElementInfo;

import evl.DefTraverser;
import evl.expression.reference.RefName;
import evl.expression.reference.Reference;
import evl.function.header.FuncPrivateRet;
import evl.function.header.FuncPrivateVoid;
import evl.hfsm.ImplHfsm;
import evl.hfsm.State;
import evl.hfsm.Transition;
import evl.knowledge.KnowledgeBase;
import evl.other.EvlList;
import evl.traverser.RefTypeGetter;
import evl.type.Type;
import evl.type.composed.NamedElement;
import evl.variable.StateVariable;
import evl.variable.Variable;

public class StateVarReplacer extends DefTraverser<Void, Void> {
  final private Variable dataVar;
  final private Map<StateVariable, EvlList<NamedElement>> epath;
  final private RefTypeGetter rtg;

  public StateVarReplacer(Variable dataVar, Map<StateVariable, EvlList<NamedElement>> epath, KnowledgeBase kb) {
    super();
    this.epath = epath;
    this.dataVar = dataVar;
    rtg = new RefTypeGetter(kb);
  }

  static public void process(ImplHfsm obj, Variable dataVar, Map<StateVariable, EvlList<NamedElement>> epath, KnowledgeBase kb) {
    StateVarReplacer know = new StateVarReplacer(dataVar, epath, kb);
    know.traverse(obj, null);
  }

  @Override
  protected Void visitImplHfsm(ImplHfsm obj, Void param) {
    visit(obj.getTopstate(), param);
    return null;
  }

  @Override
  protected Void visitState(State obj, Void param) {
    visitList(obj.getItem(), param);
    return null;
  }

  @Override
  protected Void visitTransition(Transition obj, Void param) {
    visit(obj.getGuard(), param);
    visit(obj.getBody(), param);
    return null;
  }

  @Override
  protected Void visitFuncPrivateVoid(FuncPrivateVoid obj, Void param) {
    visit(obj.getBody(), param);
    return null;
  }

  @Override
  protected Void visitFuncPrivateRet(FuncPrivateRet obj, Void param) {
    visit(obj.getBody(), param);
    return null;
  }

  @Override
  protected Void visitReference(Reference obj, Void param) {
    if (obj.getLink() == dataVar) {
      visitList(obj.getOffset(), param);
      return null;
    }
    super.visitReference(obj, param);
    if (obj.getLink() instanceof StateVariable) {
      EvlList<NamedElement> eofs = epath.get(obj.getLink());
      assert (eofs != null);

      assert (obj.getOffset().isEmpty()); // FIXME not always true (e.g. for access to struct)

      Type type = dataVar.getType().getLink();

      obj.setLink(dataVar);
      for (NamedElement itr : eofs) {
        RefName ref = new RefName(ElementInfo.NO, itr.getName());

        type = rtg.traverse(ref, type);  // sanity check

        obj.getOffset().add(ref);
      }

    }
    return null;
  }

}
