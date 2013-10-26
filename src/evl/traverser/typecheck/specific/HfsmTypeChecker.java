package evl.traverser.typecheck.specific;

import evl.Evl;
import evl.NullTraverser;
import evl.function.FuncWithBody;
import evl.function.FunctionBase;
import evl.hfsm.HfsmQueryFunction;
import evl.hfsm.ImplHfsm;
import evl.hfsm.QueryItem;
import evl.hfsm.State;
import evl.hfsm.StateComposite;
import evl.hfsm.StateSimple;
import evl.hfsm.Transition;
import evl.knowledge.KnowBaseItem;
import evl.knowledge.KnowledgeBase;
import evl.other.Named;
import evl.other.NamedList;
import evl.variable.StateVariable;

//TODO check for unused states
//TODO check if a transition is never used
//TODO check that all queries are defined
//TODO check that no event is handled within a state
public class HfsmTypeChecker extends NullTraverser<Void, Void> {
  private KnowledgeBase kb;
  private KnowBaseItem kbi;

  public HfsmTypeChecker(KnowledgeBase kb) {
    super();
    this.kb = kb;
    kbi = kb.getEntry(KnowBaseItem.class);
  }

  public static void process(ImplHfsm obj, KnowledgeBase kb) {
    HfsmTypeChecker check = new HfsmTypeChecker(kb);
    check.traverse(obj, null);
  }

  @Override
  protected Void visitDefault(Evl obj, Void sym) {
    throw new RuntimeException("not yet implemented: " + obj.getClass().getSimpleName());
  }

  @Override
  protected Void visitNamedList(NamedList<Named> obj, Void param) {
    visitItr(obj, param);
    return null;
  }

  @Override
  protected Void visitImplHfsm(ImplHfsm obj, Void param) {
    visit(obj.getTopstate(), param);
    return null;
  }

  @Override
  protected Void visitStateSimple(StateSimple obj, Void param) {
    return null;
  }

  @Override
  protected Void visitStateComposite(StateComposite obj, Void param) {
    return null;
  }

  @Override
  protected Void visitState(State obj, Void param) {
    visitItr(obj.getVariable(), param);
    visitItr(obj.getFunction(), param);
    visitItr(obj.getItem(), param);
    // TODO add check if transition could be in a deeper state
    return null;
  }

  @Override
  protected Void visitFunctionBase(FunctionBase obj, Void param) {
    if (obj instanceof FuncWithBody) {
      FunctionTypeChecker.process(obj, kb);
    }
    return null;
  }

  @Override
  protected Void visitStateVariable(StateVariable obj, Void param) {
    StatementTypeChecker.process(obj, kb);
    return null;
  }

  @Override
  protected Void visitTransition(Transition obj, Void param) {
    ExpressionTypeChecker.process(obj.getGuard(), kb);
    // TODO not used since we narrowed ranges before? => remove Map<SsaVariable, NumSet>
    // Map<Variable, NumSet> varRange = RangeGetter.getSmallerRangeForTrue(obj.getGuard(), kb);
    // StatementTypeChecker.process(obj.getBody(), kbi.getVoidType(), varRange, kb);
    StatementTypeChecker.process(obj.getBody(), kbi.getVoidType(), kb);
    return null;
  }

  @Override
  protected Void visitQueryItem(QueryItem obj, Void param) {
    visit(obj.getFunc(), param);
    return null;
  }

  @Override
  protected Void visitHfsmQueryFunction(HfsmQueryFunction obj, Void param) {
    FunctionTypeChecker.process(obj, kb);
    return null;
  }

}
