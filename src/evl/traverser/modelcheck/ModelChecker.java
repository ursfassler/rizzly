package evl.traverser.modelcheck;

import java.util.ArrayList;

import evl.DefTraverser;
import evl.Evl;
import evl.composition.ImplComposition;
import evl.expression.Expression;
import evl.function.FunctionBase;
import evl.hfsm.ImplHfsm;
import evl.knowledge.KnowledgeBase;
import evl.other.ImplElementary;
import evl.other.ListOfNamed;
import evl.other.Named;
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

  public static void processList(ListOfNamed<? extends Named> variable, KnowledgeBase kb) {
    ModelChecker adder = new ModelChecker(kb);
    for (Named itr : new ArrayList<Named>(variable.getList())) {
      adder.traverse(itr, null);
    }
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
  protected Void visitFunctionBase(FunctionBase obj, Void sym) {
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
