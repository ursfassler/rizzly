package ast.pass;

import pass.AstPass;
import ast.data.Ast;
import ast.data.Namespace;
import ast.data.component.composition.CompUse;
import ast.data.component.hfsm.State;
import ast.data.component.hfsm.StateComposite;
import ast.data.component.hfsm.Transition;
import ast.data.expression.reference.BaseRef;
import ast.data.expression.reference.SimpleRef;
import ast.data.function.ret.FuncReturnType;
import ast.data.type.composed.NamedElement;
import ast.data.variable.Variable;
import ast.knowledge.KnowledgeBase;
import ast.traverser.DefTraverser;

public class CheckSimpleTypeRef extends AstPass {

  @Override
  public void process(Namespace ast, KnowledgeBase kb) {
    CheckSimpleTypeRefWorker worker = new CheckSimpleTypeRefWorker();
    worker.traverse(ast, null);
  }

}

class CheckSimpleTypeRefWorker extends DefTraverser<Void, Void> {

  private void checkRef(BaseRef<? extends Ast> ref) {
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
