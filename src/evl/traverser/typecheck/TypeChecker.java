package evl.traverser.typecheck;

import evl.DefTraverser;
import evl.Evl;
import evl.composition.ImplComposition;
import evl.expression.Expression;
import evl.function.Function;
import evl.hfsm.ImplHfsm;
import evl.knowledge.KnowledgeBase;
import evl.other.ImplElementary;
import evl.traverser.typecheck.specific.ExpressionTypeChecker;
import evl.traverser.typecheck.specific.FunctionTypeChecker;
import evl.traverser.typecheck.specific.StatementTypeChecker;
import evl.type.base.EnumType;
import evl.variable.Variable;

public class TypeChecker extends DefTraverser<Void, Void> {
  private KnowledgeBase kb;

  public TypeChecker(KnowledgeBase kb) {
    super();
    this.kb = kb;
  }

  static public void process(Evl obj, KnowledgeBase kb) {
    TypeChecker adder = new TypeChecker(kb);
    adder.traverse(obj, null);
  }

  @Override
  protected Void visitImplElementary(ImplElementary obj, Void sym) {
    visitList(obj.getVariable(), sym);
    visitList(obj.getConstant(), sym);
    visitList(obj.getFunction(), sym);
    visitList(obj.getSubCallback(), sym);
    return null;
  }

  @Override
  protected Void visitImplComposition(ImplComposition obj, Void sym) {
    throw new RuntimeException("should not reach this point");
  }

  @Override
  protected Void visitImplHfsm(ImplHfsm obj, Void param) {
    throw new RuntimeException("should not reach this point");
  }

  @Override
  protected Void visitFunctionImpl(Function obj, Void sym) {
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

  @Override
  protected Void visitEnumType(EnumType obj, Void param) {
    return null; // we do not type check them (assignment of number does not work)
  }

}
