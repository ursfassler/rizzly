package evl.hfsm.reduction;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import common.Designator;
import common.ElementInfo;

import evl.DefTraverser;
import evl.expression.reference.RefItem;
import evl.expression.reference.RefName;
import evl.expression.reference.Reference;
import evl.function.impl.FuncPrivateRet;
import evl.function.impl.FuncPrivateVoid;
import evl.hfsm.ImplHfsm;
import evl.hfsm.State;
import evl.hfsm.Transition;
import evl.variable.StateVariable;
import evl.variable.Variable;


public class StateVarReplacer extends DefTraverser<Void, Void> {
  final private Map<StateVariable, Designator> vpath;
  final private Variable dataVar;

  public StateVarReplacer(Variable dataVar, Map<StateVariable, Designator> vpath) {
    super();
    this.vpath = vpath;
    this.dataVar = dataVar;
  }

  static public void process(ImplHfsm obj, Variable dataVar, Map<StateVariable, Designator> vpath) {
    StateVarReplacer know = new StateVarReplacer(dataVar, vpath);
    know.traverse(obj, null);
  }

  @Override
  protected Void visitImplHfsm(ImplHfsm obj, Void param) {
    visit(obj.getTopstate(), param);
    return null;
  }

  @Override
  protected Void visitState(State obj, Void param) {
    visitItr(obj.getFunction(), param);
    visitItr(obj.getItem(), param);
    return null;
  }

  @Override
  protected Void visitTransition(Transition obj, Void param) {
    visit(obj.getGuard(), param);
    visit(obj.getBody(), param);
    return null;
  }

  @Override
  protected Void visitFuncPrivateVoid(FuncPrivateVoid obj, Void param) {
    visit(obj.getBody(), param);
    return null;
  }

  @Override
  protected Void visitFuncPrivateRet(FuncPrivateRet obj, Void param) {
    visit(obj.getBody(), param);
    return null;
  }

  @Override
  protected Void visitReference(Reference obj, Void param) {
    if (obj.getLink() == dataVar) {
      visitItr(obj.getOffset(), param);
      return null;
    }
    super.visitReference(obj, param);
    if (obj.getLink() instanceof StateVariable) {
      Designator offset = vpath.get(obj.getLink());
      assert (offset != null);

      List<RefItem> addOfs = new ArrayList<RefItem>();
      for (String itr : offset) {
        addOfs.add(new RefName(new ElementInfo(), itr));
      }

      obj.setLink(dataVar);
      obj.getOffset().addAll(addOfs);
    }
    return null;
  }

}
