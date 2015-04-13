package ast.pass.others;

import ast.data.Named;
import ast.data.Namespace;
import ast.data.component.Component;
import ast.data.component.hfsm.State;
import ast.data.expression.Expression;
import ast.data.expression.reference.Reference;
import ast.data.expression.reference.SimpleRef;
import ast.data.raw.RawComponent;
import ast.data.type.Type;
import ast.knowledge.KnowledgeBase;
import ast.pass.AstPass;
import ast.traverser.other.ExprReplacer;

public class SimplifyRef extends AstPass {

  @Override
  public void process(Namespace ast, KnowledgeBase kb) {
    SimplifyTypeRefWorker worker = new SimplifyTypeRefWorker();
    worker.traverse(ast, null);
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
