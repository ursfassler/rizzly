package evl.traverser;

import util.SimpleGraph;

import common.Designator;

import evl.DefTraverser;
import evl.expression.reference.RefCall;
import evl.expression.reference.RefItem;
import evl.expression.reference.RefName;
import evl.expression.reference.Reference;
import evl.function.FunctionBase;
import evl.other.ImplElementary;
import evl.other.Named;
import evl.other.NamedList;
import evl.other.Namespace;

/**
 * Returns a callgraph of the entire (sub-) tree
 * 
 * @author urs
 * 
 */
public class DesCallgraphMaker extends DefTraverser<Void, Designator> {

  private SimpleGraph<Designator> callgraph = new SimpleGraph<Designator>();

  public static SimpleGraph<Designator> make(ImplElementary inst) {
    DesCallgraphMaker reduction = new DesCallgraphMaker();
    reduction.traverse(inst, new Designator());
    return reduction.callgraph;
  }

  @Override
  protected Void visitNamedList(NamedList<Named> obj, Designator param) {
    return super.visitNamedList(obj, new Designator(param, obj.getName()));
  }

  @Override
  protected Void visitNamespace(Namespace obj, Designator param) {
    throw new RuntimeException("not yet implemented");
  }

  @Override
  protected Void visitFunctionBase(FunctionBase obj, Designator param) {
    assert ( param != null );
    param = new Designator(param, obj.getName());
    assert ( param.size() <= 3 );
    callgraph.addVertex(param);
    return super.visitFunctionBase(obj, param);
  }

  @Override
  protected Void visitReference(Reference obj, Designator param) {
    super.visitReference(obj, param);

    Designator func = getIfFunc(obj);
    if( func != null ) {
      assert ( func.size() <= 3 );
      assert ( param.size() <= 3 );
      callgraph.addVertex(func);
      callgraph.addEdge(param, func);
    }
    return null;
  }

  static private Designator getIfFunc(Reference obj) {
    if( obj.getOffset().isEmpty() ) {
      return null;
    }
    Designator ret = new Designator(obj.getLink().getName());
    for( RefItem ref : obj.getOffset() ) {
      if( ref instanceof RefCall ) {
        break;
      } else if( ref instanceof RefName ) {
        ret = new Designator(ret, ( (RefName) ref ).getName());
      } else {
        return null; // no function call
      }
    }
    return ret;
  }
}
