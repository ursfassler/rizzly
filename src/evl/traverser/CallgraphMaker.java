package evl.traverser;

import java.util.HashSet;
import java.util.Set;

import util.SimpleGraph;
import evl.DefTraverser;
import evl.Evl;
import evl.NullTraverser;
import evl.expression.reference.RefCall;
import evl.expression.reference.RefIndex;
import evl.expression.reference.RefItem;
import evl.expression.reference.RefName;
import evl.expression.reference.Reference;
import evl.function.FuncWithReturn;
import evl.function.FunctionBase;
import evl.hfsm.Transition;
import evl.knowledge.KnowBaseItem;
import evl.knowledge.KnowChild;
import evl.knowledge.KnowledgeBase;
import evl.type.Type;
import evl.type.base.ArrayType;
import evl.variable.Variable;

/**
 * Returns a callgraph of the entire (sub-) tree
 * 
 * @author urs
 * 
 */
public class CallgraphMaker extends DefTraverser<Void, Evl> {
  private SimpleGraph<Evl> callgraph = new SimpleGraph<Evl>();
  private KnowledgeBase kb;

  public CallgraphMaker(KnowledgeBase kb) {
    super();
    this.kb = kb;
  }

  public static SimpleGraph<Evl> make(Evl inst, KnowledgeBase kb) {
    CallgraphMaker reduction = new CallgraphMaker(kb);
    reduction.traverse(inst, null);
    return reduction.callgraph;
  }

  @Override
  protected Void visitFunctionBase(FunctionBase obj, Evl param) {
    assert (param == null);
    callgraph.addVertex(obj);
    return super.visitFunctionBase(obj, obj);
  }

  @Override
  protected Void visitTransition(Transition obj, Evl param) {
    assert (param == null);

    callgraph.addVertex(obj.getGuard());
    visit(obj.getGuard(), obj.getGuard());

    callgraph.addVertex(obj.getBody());
    visit(obj.getBody(), obj.getBody());

    return null;
  }

  @Override
  protected Void visitReference(Reference obj, Evl param) {
    super.visitReference(obj, param);

    if (param != null) {
      Set<FunctionBase> target = new HashSet<FunctionBase>();

      Evl item = obj.getLink();
      for (RefItem itr : obj.getOffset()) {
        item = RefGetter.process(itr, item, target, kb);
      }

      for (FunctionBase head : target) {
        callgraph.addVertex(head);
        callgraph.addEdge(param, head);
      }
    }
    return null;
  }

}

class RefGetter extends NullTraverser<Evl, Evl> {
  private Set<FunctionBase> target;
  private KnowChild kfc;
  private KnowBaseItem kbi;

  static public Evl process(RefItem refitm, Evl last, Set<FunctionBase> target, KnowledgeBase kb) {
    RefGetter refChecker = new RefGetter(kb, target);
    return refChecker.traverse(refitm, last);
  }

  public RefGetter(KnowledgeBase kb, Set<FunctionBase> target) {
    super();
    this.target = target;
    this.kfc = kb.getEntry(KnowChild.class);
    kbi = kb.getEntry(KnowBaseItem.class);
  }

  @Override
  protected Evl visitDefault(Evl obj, Evl param) {
    throw new RuntimeException("not yet implemented: " + obj.getClass().getCanonicalName());
  }

  @Override
  protected Evl visitRefCall(RefCall obj, Evl param) {
    if (param instanceof Type) {
      // convert function
      return param;
    }
    FunctionBase header = (FunctionBase) param;
    target.add(header);
    if (header instanceof FuncWithReturn) {
      return ((FuncWithReturn) header).getRet().getRef();
    } else {
      return kbi.getVoidType();
    }
  }

  @Override
  protected Evl visitRefName(RefName obj, Evl param) {
    return kfc.get(param, obj.getName(), obj.getInfo());
  }

  @Override
  protected Evl visitRefIndex(RefIndex obj, Evl param) {
    Variable var = (Variable) param;
    Type type = var.getType().getRef();
    ArrayType arrayType = (ArrayType) type;
    return arrayType.getType().getRef();
  }

}
