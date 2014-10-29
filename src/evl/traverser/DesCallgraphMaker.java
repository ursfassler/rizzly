package evl.traverser;

import util.SimpleGraph;

import common.Designator;

import evl.DefTraverser;
import evl.composition.ImplComposition;
import evl.expression.reference.RefCall;
import evl.expression.reference.RefItem;
import evl.expression.reference.RefName;
import evl.expression.reference.Reference;
import evl.function.Function;
import evl.hfsm.ImplHfsm;
import evl.knowledge.KnowPath;
import evl.knowledge.KnowledgeBase;
import evl.other.Component;
import evl.other.ImplElementary;
import evl.other.Namespace;
import evl.other.RizzlyProgram;

/**
 * Returns a callgraph of the entire (sub-) tree
 * 
 * @author urs
 * 
 */
// TODO verify output
public class DesCallgraphMaker extends DefTraverser<Void, Designator> {
  final private KnowPath kp;
  private SimpleGraph<Designator> callgraph = new SimpleGraph<Designator>();

  public DesCallgraphMaker(KnowledgeBase kb) {
    super();
    this.kp = kb.getEntry(KnowPath.class);
  }

  public static SimpleGraph<Designator> make(ImplElementary inst, KnowledgeBase kb) {
    DesCallgraphMaker reduction = new DesCallgraphMaker(kb);
    reduction.traverse(inst, new Designator());
    return reduction.callgraph;
  }

  @Override
  protected Void visitNamespace(Namespace obj, Designator param) {
    throw new RuntimeException("not yet implemented");
  }

  @Override
  protected Void visitFunctionImpl(Function obj, Designator param) {
    assert (param != null);
    param = kp.get(obj);
    callgraph.addVertex(param);
    return super.visitFunctionImpl(obj, param);
  }

  @Override
  protected Void visitReference(Reference obj, Designator param) {
    super.visitReference(obj, param);

    Designator func = getIfFunc(obj);
    if (func != null) {
      callgraph.addVertex(func);
      callgraph.addEdge(param, func);
    }
    return null;
  }

  private Designator getIfFunc(Reference obj) {
    if (obj.getOffset().isEmpty()) {
      return null;
    }
    Designator ret = kp.get(obj.getLink());
    for (RefItem ref : obj.getOffset()) {
      if (ref instanceof RefCall) {
        break;
      } else if (ref instanceof RefName) {
        ret = new Designator(ret, ((RefName) ref).getName());
      } else {
        return null; // no function call
      }
    }
    return ret;
  }

  @Override
  protected Void visitComponent(Component obj, Designator param) {
    visitList(obj.getFunction(), param);
    return null;
  }

  @Override
  protected Void visitImplComposition(ImplComposition obj, Designator param) {
    return super.visitImplComposition(obj, param);
  }

  @Override
  protected Void visitImplElementary(ImplElementary obj, Designator param) {
    throw new RuntimeException("what is correct?");
    // return super.visitImplElementary(obj, param);
    //
    // visitList(obj.getSubCallback(), param);
    // return null;
  }

  @Override
  protected Void visitImplHfsm(ImplHfsm obj, Designator param) {
    return super.visitImplHfsm(obj, param);
  }

  @Override
  protected Void visitRizzlyProgram(RizzlyProgram obj, Designator param) {
    return super.visitRizzlyProgram(obj, param);
  }

}
