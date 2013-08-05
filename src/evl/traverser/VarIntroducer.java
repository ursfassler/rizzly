package evl.traverser;

import java.util.ArrayList;

import evl.Evl;
import evl.NullTraverser;
import evl.composition.ImplComposition;
import evl.function.FuncWithBody;
import evl.function.FunctionBase;
import evl.knowledge.KnowledgeBase;
import evl.other.ImplElementary;
import evl.other.Named;
import evl.other.NamedList;
import evl.other.Namespace;

/*
 * Introduces new variable for every assignment, a bit like SSA
 *
 */
public class VarIntroducer extends NullTraverser<Void, Void> {
  private StmtTraverser st;

  public VarIntroducer(KnowledgeBase kb) {
    super();
    this.st = new StmtTraverser(kb);
  }

  public static void process(Namespace classes, KnowledgeBase kb) {
    VarIntroducer cutter = new VarIntroducer(kb);
    cutter.traverse(classes, null);
  }

  @Override
  protected Void visitDefault(Evl obj, Void param) {
    // throw new RuntimeException("not yet implemented: " + obj.getClass().getCanonicalName());
    return null;
  }

  @Override
  protected Void visitItr(Iterable<? extends Evl> list, Void param) {
    // against comodification error
    ArrayList<Evl> old = new ArrayList<Evl>();
    for (Evl itr : list) {
      old.add(itr);
    }
    return super.visitItr(old, param);
  }

  @Override
  protected Void visitNamespace(Namespace obj, Void param) {
    visitItr(obj, param);
    return null;
  }

  @Override
  protected Void visitNamedList(NamedList<Named> obj, Void param) {
    visitItr(obj, param);
    return null;
  }

  @Override
  protected Void visitImplElementary(ImplElementary obj, Void param) {
    visitItr(obj.getInternalFunction(), param);
    visitItr(obj.getInputFunc(), param);
    visitItr(obj.getSubComCallback(), param);
    return null;
  }

  @Override
  protected Void visitFunctionBase(FunctionBase obj, Void param) {
    if (obj instanceof FuncWithBody) {
      st.traverse(obj, null);
    }
    return null;
  }

  @Override
  protected Void visitImplComposition(ImplComposition obj, Void param) {
    throw new RuntimeException("not yet implemented");
  }

}
