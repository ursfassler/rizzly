package evl.traverser.typecheck;

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
import evl.traverser.typecheck.specific.ExpressionTypeChecker;
import evl.traverser.typecheck.specific.FunctionTypeChecker;
import evl.traverser.typecheck.specific.HfsmTypeChecker;
import evl.traverser.typecheck.specific.StatementTypeChecker;
import evl.variable.Variable;

public class TypeChecker extends DefTraverser<Void, Void> {
  private KnowledgeBase kb;

  public TypeChecker(KnowledgeBase kb) {
    super();
    this.kb = kb;
  }

  static public void process(Evl obj, KnowledgeBase kb, Void sym) {
    TypeChecker adder = new TypeChecker(kb);
    adder.traverse(obj, sym);
  }

  public static void processList(ListOfNamed<? extends Named> variable, KnowledgeBase kb) {
    TypeChecker adder = new TypeChecker(kb);
    for (Named itr : new ArrayList<Named>(variable.getList())) {
      adder.traverse(itr, null);
    }
  }

  @Override
  protected Void visitImplElementary(ImplElementary obj, Void sym) {
    visitItr(obj.getVariable(), sym);
    visitItr(obj.getConstant(), sym);
    visitItr(obj.getInternalFunction(), sym);
    visitItr(obj.getInputFunc(), sym);
    visitItr(obj.getSubComCallback(), sym);
    return null;
  }

  @Override
  protected Void visitImplComposition(ImplComposition obj, Void sym) {
    return null; // why not type checked (here)? > in CompInterfaceTypeChecker
  }

  @Override
  protected Void visitImplHfsm(ImplHfsm obj, Void param) {
    HfsmTypeChecker.process(obj, kb);
    return null;
  }

  @Override
  protected Void visitFunctionBase(FunctionBase obj, Void sym) {
    FunctionTypeChecker.process(obj, kb);
    return null;
  }

  @Override
  protected Void visitVariable(Variable obj, Void sym) {
    StatementTypeChecker.process(obj, kb);
    return null;
  }

  @Override
  protected Void visitExpression(Expression obj, Void sym) {
    ExpressionTypeChecker.process(obj, kb);
    return null;
  }

}
