package fun.pass;

import pass.EvlPass;
import evl.data.Named;
import evl.data.Namespace;
import evl.data.component.Component;
import evl.data.component.hfsm.State;
import evl.data.expression.Expression;
import evl.data.expression.reference.Reference;
import evl.data.expression.reference.SimpleRef;
import evl.data.type.Type;
import evl.knowledge.KnowledgeBase;
import fun.other.RawComponent;
import fun.traverser.ExprReplacer;

public class SimplifyRef extends EvlPass {

  @Override
  public void process(Namespace evl, KnowledgeBase kb) {
    SimplifyTypeRefWorker worker = new SimplifyTypeRefWorker();
    worker.traverse(evl, null);
  }

}

class SimplifyTypeRefWorker extends ExprReplacer<Void> {

  @Override
  protected Expression visitReference(Reference obj, Void param) {
    if (typeToSimplify(obj.link) && obj.offset.isEmpty()) {
      return new SimpleRef(obj.getInfo(), obj.link);
    } else {
      return super.visitReference(obj, param);
    }
  }

  private boolean typeToSimplify(Named link) {
    return (link instanceof Type) || (link instanceof Component) || (link instanceof RawComponent) || (link instanceof State);
  }

}
