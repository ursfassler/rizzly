package evl.hfsm.reduction;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import common.Designator;

import evl.Evl;
import evl.NullTraverser;
import evl.function.Function;
import evl.function.header.FuncPrivateRet;
import evl.function.header.FuncPrivateVoid;
import evl.hfsm.ImplHfsm;
import evl.hfsm.State;
import evl.hfsm.StateItem;
import evl.knowledge.KnowledgeBase;

/**
 * Moves all functions of all states to the top-state.
 * 
 * @author urs
 * 
 */
public class StateFuncUplifter extends NullTraverser<Void, Designator> {
  final private List<Function> func = new ArrayList<Function>();

  public StateFuncUplifter(KnowledgeBase kb) {
    super();
  }

  static public void process(ImplHfsm obj, KnowledgeBase kb) {
    StateFuncUplifter know = new StateFuncUplifter(kb);
    know.traverse(obj, null);
  }

  public List<Function> getFunc() {
    return func;
  }

  @Override
  protected Void visitDefault(Evl obj, Designator param) {
    if (obj instanceof StateItem) {
      return null;
    } else {
      throw new RuntimeException("not yet implemented: " + obj.getClass().getCanonicalName());
    }
  }

  @Override
  protected Void visitImplHfsm(ImplHfsm obj, Designator param) {
    visit(obj.getTopstate(), new Designator());
    obj.getTopstate().getItem().addAll((Collection<? extends StateItem>) func);
    return null;
  }

  @Override
  protected Void visitState(State obj, Designator param) {
    param = new Designator(param, obj.getName());
    // visit(obj.getEntryCode(), param);//TODO correct? It is no longer a function and should not exist at this point
    // visit(obj.getExitCode(), param);
    visitList(obj.getItem(), param);

    obj.getItem().removeAll(func);

    return null;
  }

  @Override
  protected Void visitFuncPrivateVoid(FuncPrivateVoid obj, Designator param) {
    param = new Designator(param, obj.getName());
    obj.setName(param.toString(Designator.NAME_SEP));
    func.add(obj);
    return null;
  }

  @Override
  protected Void visitFuncPrivateRet(FuncPrivateRet obj, Designator param) {
    param = new Designator(param, obj.getName());
    obj.setName(param.toString(Designator.NAME_SEP));
    func.add(obj);
    return null;
  }
}
