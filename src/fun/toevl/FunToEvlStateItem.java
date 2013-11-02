package fun.toevl;

import java.util.ArrayList;
import java.util.List;

import evl.expression.Expression;
import evl.expression.reference.RefName;
import evl.expression.reference.Reference;
import evl.function.FunctionBase;
import evl.hfsm.HfsmQueryFunction;
import evl.hfsm.State;
import evl.hfsm.StateItem;
import evl.other.IfaceUse;
import evl.statement.Block;
import fun.Fun;
import fun.NullTraverser;
import fun.function.FunctionHeader;
import fun.hfsm.QueryItem;
import fun.hfsm.StateComposite;
import fun.hfsm.StateSimple;
import fun.hfsm.Transition;
import fun.variable.Variable;

public class FunToEvlStateItem extends NullTraverser<StateItem, Void> {
  private FunToEvl fta;

  public FunToEvlStateItem(FunToEvl fta) {
    super();
    this.fta = fta;
  }

  @Override
  protected StateItem visitDefault(Fun obj, Void param) {
    throw new RuntimeException("not yet implemented: " + obj.getClass().getCanonicalName());
  }

  // ----------------------------------------------------------------------------
  @Override
  protected StateItem visitStateComposite(StateComposite obj, Void param) {
    evl.hfsm.StateComposite state = new evl.hfsm.StateComposite(obj.getInfo(), obj.getName());
    fta.map.put(obj, state);

    for (Variable var : obj.getVariable()) {
      state.getVariable().add((evl.variable.Variable) fta.traverse(var, null));
    }
    for (FunctionHeader use : obj.getBfunc()) {
      state.getFunction().add((FunctionBase) fta.traverse(use, null));
    }
    for (fun.hfsm.StateItem use : obj.getItem()) {
      state.getItem().add((evl.hfsm.StateItem) fta.traverse(use, null));
    }

    // it is here to break dependency cycle
    state.setEntryFunc((Reference) fta.traverse(obj.getEntryFuncRef(), null));
    state.setExitFunc((Reference) fta.traverse(obj.getExitFuncRef(), null));
    Reference initref = (Reference) fta.traverse(obj.getInitial(), null);
    assert (initref.getOffset().isEmpty());
    state.setInitial((State) initref.getLink());

    return state;
  }

  @Override
  protected StateItem visitStateSimple(StateSimple obj, Void param) {
    evl.hfsm.StateSimple state = new evl.hfsm.StateSimple(obj.getInfo(), obj.getName());
    fta.map.put(obj, state);
    for (Variable var : obj.getVariable()) {
      state.getVariable().add((evl.variable.Variable) fta.traverse(var, null));
    }
    for (FunctionHeader use : obj.getBfunc()) {
      state.getFunction().add((FunctionBase) fta.traverse(use, null));
    }
    for (fun.hfsm.StateItem use : obj.getItem()) {
      state.getItem().add((evl.hfsm.StateItem) fta.traverse(use, null));
    }

    // it is here to break dependency cycle
    state.setEntryFunc((Reference) fta.traverse(obj.getEntryFuncRef(), null));
    state.setExitFunc((Reference) fta.traverse(obj.getExitFuncRef(), null));

    return state;
  }

  @Override
  protected StateItem visitQueryItem(QueryItem obj, Void param) {
    return new evl.hfsm.QueryItem(obj.getNamespace(), (HfsmQueryFunction) fta.traverse(obj.getFunc(), null));
  }

  @Override
  protected StateItem visitTransition(Transition obj, Void param) {
    List<evl.variable.FuncVariable> args = new ArrayList<evl.variable.FuncVariable>(obj.getParam().size());
    for (fun.variable.FuncVariable itr : obj.getParam()) {
      args.add((evl.variable.FuncVariable) fta.traverse(itr, null));
    }
    evl.expression.reference.Reference src = (evl.expression.reference.Reference) fta.traverse(obj.getSrc(), null);
    evl.expression.reference.Reference dst = (evl.expression.reference.Reference) fta.traverse(obj.getDst(), null);
    evl.expression.reference.Reference evt = (evl.expression.reference.Reference) fta.traverse(obj.getEvent(), null);
    assert (src.getOffset().isEmpty());
    assert (dst.getOffset().isEmpty());
    assert (evt.getOffset().size() == 1);
    assert (evt.getOffset().get(0) instanceof RefName);

    Expression guard = (Expression) fta.traverse(obj.getGuard(), null);

    Block nbody = (Block) fta.traverse(obj.getBody(), null);

    return new evl.hfsm.Transition(obj.getInfo(), obj.getName(), (State) src.getLink(), (State) dst.getLink(), (IfaceUse) evt.getLink(), ((RefName) evt.getOffset().get(0)).getName(), guard, args, nbody);
  }
}
