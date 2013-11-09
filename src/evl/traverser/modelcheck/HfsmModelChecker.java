package evl.traverser.modelcheck;

import evl.Evl;
import evl.NullTraverser;
import evl.hfsm.HfsmQueryFunction;
import evl.hfsm.ImplHfsm;
import evl.hfsm.State;
import evl.hfsm.Transition;
import evl.knowledge.KnowBaseItem;
import evl.knowledge.KnowledgeBase;
import evl.other.SubCallbacks;

//TODO check for unused states
//TODO check if a transition is never used
//TODO check that all queries are defined
//TODO check that no event is handled within a state
public class HfsmModelChecker extends NullTraverser<Void, Void> {
  private KnowledgeBase kb;
  private KnowBaseItem kbi;

  public HfsmModelChecker(KnowledgeBase kb) {
    super();
    this.kb = kb;
    kbi = kb.getEntry(KnowBaseItem.class);
  }

  public static void process(ImplHfsm obj, KnowledgeBase kb) {
    HfsmModelChecker check = new HfsmModelChecker(kb);
    check.traverse(obj, null);
  }

  @Override
  protected Void visitDefault(Evl obj, Void sym) {
    throw new RuntimeException("not yet implemented: " + obj.getClass().getSimpleName());
  }

  @Override
  protected Void visitSubCallbacks(SubCallbacks obj, Void param) {
    visitItr(obj, param);
    return null;
  }

  @Override
  protected Void visitImplHfsm(ImplHfsm obj, Void param) {
    visit(obj.getTopstate(), param);
    return null;
  }

  @Override
  protected Void visitState(State obj, Void param) {
    visitItr(obj.getItem(), param);
    return null;
  }

  @Override
  protected Void visitTransition(Transition obj, Void param) {
    // TODO check that guard does not write state
    return null;
  }

  @Override
  protected Void visitHfsmQueryFunction(HfsmQueryFunction obj, Void param) {
    // TODO check that state is not written
    return null;
  }

}
