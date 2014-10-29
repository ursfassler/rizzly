package fun.hfsm;

import java.util.HashMap;

import common.Designator;

import fun.Fun;
import fun.NullTraverser;
import fun.function.FuncHeader;

@Deprecated
public class FullStateName extends NullTraverser<Void, Designator> {
  final private HashMap<State, Designator> names = new HashMap<State, Designator>();

  static public HashMap<State, Designator> get(State obj) {
    FullStateName know = new FullStateName();
    know.traverse(obj, new Designator());
    return know.getFullNames();
  }

  public HashMap<State, Designator> getFullNames() {
    return names;
  }

  @Override
  protected Void visitDefault(Fun obj, Designator param) {
    throw new RuntimeException("not yet implemented: " + obj.getClass().getCanonicalName());
  }

  @Override
  protected Void visitTransition(Transition obj, Designator param) {
    return null;
  }

  @Override
  protected Void visitStateSimple(StateSimple obj, Designator param) {
    return null;
  }

  @Override
  protected Void visitFunctionHeader(FuncHeader obj, Designator param) {
    return null;
  }

  @Override
  protected Void visitStateComposite(StateComposite obj, Designator param) {
    visitList(obj.getItemList(), param);
    return null;
  }

  @Override
  protected Void visitState(State obj, Designator param) {
    assert (false);
    // param = new Designator(param, obj.getName());
    names.put(obj, param);
    return super.visitState(obj, param);
  }

}
