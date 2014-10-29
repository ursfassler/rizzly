package evl.traverser.modelcheck;

import evl.DefTraverser;
import evl.Evl;
import evl.composition.ImplComposition;
import evl.expression.Expression;
import evl.function.Function;
import evl.hfsm.ImplHfsm;
import evl.knowledge.KnowledgeBase;
import evl.other.ImplElementary;
import evl.variable.Variable;

public class ModelChecker extends DefTraverser<Void, Void> {
  private KnowledgeBase kb;

  public ModelChecker(KnowledgeBase kb) {
    super();
    this.kb = kb;
  }

  static public void process(Evl obj, KnowledgeBase kb) {
    ModelChecker adder = new ModelChecker(kb);
    adder.traverse(obj, null);
  }

  @Override
  protected Void visitImplElementary(ImplElementary obj, Void sym) {
    return null;
  }

  @Override
  protected Void visitImplComposition(ImplComposition obj, Void sym) {
    return null; // why not type checked (here)? > in CompInterfaceTypeChecker
  }

  @Override
  protected Void visitImplHfsm(ImplHfsm obj, Void param) {
    HfsmModelChecker.process(obj, kb);
    return null;
  }

  @Override
  protected Void visitFunctionImpl(Function obj, Void sym) {
    return null;
  }

  @Override
  protected Void visitVariable(Variable obj, Void sym) {
    return null;
  }

  @Override
  protected Void visitExpression(Expression obj, Void sym) {
    return null;
  }

}
