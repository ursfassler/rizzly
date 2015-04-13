package fun.pass;

import pass.EvlPass;
import evl.data.Evl;
import evl.data.Namespace;
import evl.data.component.composition.CompUse;
import evl.data.component.hfsm.State;
import evl.data.component.hfsm.StateComposite;
import evl.data.component.hfsm.Transition;
import evl.data.expression.reference.BaseRef;
import evl.data.expression.reference.SimpleRef;
import evl.data.function.ret.FuncReturnType;
import evl.data.type.composed.NamedElement;
import evl.data.variable.Variable;
import evl.knowledge.KnowledgeBase;
import evl.traverser.DefTraverser;

public class CheckSimpleTypeRef extends EvlPass {

  @Override
  public void process(Namespace evl, KnowledgeBase kb) {
    CheckSimpleTypeRefWorker worker = new CheckSimpleTypeRefWorker();
    worker.traverse(evl, null);
  }

}

class CheckSimpleTypeRefWorker extends DefTraverser<Void, Void> {

  private void checkRef(BaseRef<? extends Evl> ref) {
    assert (ref instanceof SimpleRef);
  }

  @Override
  protected Void visitVariable(Variable obj, Void param) {
    checkRef((BaseRef) obj.type);
    return super.visitVariable(obj, param);
  }

  @Override
  protected Void visitNamedElement(NamedElement obj, Void param) {
    checkRef((BaseRef) obj.typeref);
    return super.visitNamedElement(obj, param);
  }

  @Override
  protected Void visitState(State obj, Void param) {
    checkRef(obj.entryFunc);
    checkRef(obj.exitFunc);
    return super.visitState(obj, param);
  }

  @Override
  protected Void visitStateComposite(StateComposite obj, Void param) {
    checkRef((BaseRef) obj.initial);
    return super.visitStateComposite(obj, param);
  }

  @Override
  protected Void visitTransition(Transition obj, Void param) {
    checkRef((BaseRef) obj.src);
    checkRef((BaseRef) obj.dst);
    return super.visitTransition(obj, param);
  }

  @Override
  protected Void visitFuncReturnType(FuncReturnType obj, Void param) {
    checkRef((BaseRef) obj.type);
    return super.visitFuncReturnType(obj, param);
  }

  @Override
  protected Void visitCompUse(CompUse obj, Void param) {
    checkRef((BaseRef) obj.compRef);
    return super.visitCompUse(obj, param);
  }
}
