package evl.traverser;

import java.util.HashSet;
import java.util.Set;

import evl.DefTraverser;
import evl.Evl;
import evl.expression.reference.Reference;
import evl.function.FunctionBase;
import evl.other.Named;
import evl.type.Type;
import evl.variable.Constant;
import evl.variable.StateVariable;


public class DepCollector extends DefTraverser<Void, Void> {
  private Set<Named> visited = new HashSet<Named>();

  public static Set<Named> process(Evl top) {
    DepCollector collector = new DepCollector();
    collector.traverse(top, null);
    return collector.visited;
  }

  public static Set<Named> process(Set<FunctionBase> pubfunc) {
    DepCollector collector = new DepCollector();
    for (FunctionBase func : pubfunc) {
      collector.traverse(func, null);
    }
    return collector.visited;
  }

  @Override
  protected Void visit(Evl obj, Void param) {
    if (!visited.contains(obj)) {
      super.visit(obj, param);
    }
    return null;
  }

  @Override
  protected Void visitConstant(Constant obj, Void param) {
    visited.add(obj);
    return super.visitConstant(obj, param);
  }

  @Override
  protected Void visitStateVariable(StateVariable obj, Void param) {
    visited.add(obj);
    return super.visitStateVariable(obj, param);
  }

  @Override
  protected Void visitFunctionBase(FunctionBase obj, Void param) {
    visited.add(obj);
    return super.visitFunctionBase(obj, param);
  }

  @Override
  protected Void visitType(Type obj, Void param) {
    visited.add(obj);
    return super.visitType(obj, param);
  }

  @Override
  protected Void visitReference(Reference obj, Void param) {
    super.visitReference(obj, param);
    visit(obj.getLink(), param);
    return null;
  }

}
