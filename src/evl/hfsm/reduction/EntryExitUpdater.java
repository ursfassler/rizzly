package evl.hfsm.reduction;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import common.Designator;
import common.ElementInfo;

import evl.Evl;
import evl.NullTraverser;
import evl.expression.Expression;
import evl.expression.reference.RefCall;
import evl.expression.reference.Reference;
import evl.function.FunctionBase;
import evl.function.impl.FuncPrivateVoid;
import evl.hfsm.ImplHfsm;
import evl.hfsm.QueryItem;
import evl.hfsm.State;
import evl.hfsm.StateComposite;
import evl.hfsm.StateSimple;
import evl.hfsm.Transition;
import evl.other.ListOfNamed;
import evl.other.Named;
import evl.statement.Block;
import evl.statement.CallStmt;
import evl.statement.Statement;
import evl.variable.FuncVariable;


/**
 * For every state, a new entry and exit function is added. This new function contains calls to the parent entry or exit
 * function. After this pass, it is safe to move the leaf states up.
 *
 * @author urs
 *
 */
public class EntryExitUpdater extends NullTraverser<Void, EePar> {
  private final static String hfsmEntryFuncName = Designator.NAME_SEP + "hfsmEntry";
  private final static String hfsmExitFuncName = Designator.NAME_SEP + "hfsmExit";

  static public void process(ImplHfsm obj) {
    EntryExitUpdater know = new EntryExitUpdater();
    know.traverse(obj, null);
  }

  @Override
  protected Void visitDefault(Evl obj, EePar param) {
    throw new RuntimeException("not yet implemented: " + obj.getClass().getCanonicalName());
  }

  @Override
  protected Void visitTransition(Transition obj, EePar param) {
    return null;
  }

  @Override
  protected Void visitQueryItem(QueryItem obj, EePar param) {
    return null;
  }

  @Override
  protected Void visitImplHfsm(ImplHfsm obj, EePar param) {
    visit(obj.getTopstate(), new EePar());
    return null;
  }

  @Override
  protected Void visitStateSimple(StateSimple obj, EePar param) {
    changeEe(obj, param);
    return null;
  }

  public void changeEe(State obj, EePar param) {
    FuncPrivateVoid entry = makeFunc(hfsmEntryFuncName, param.entry);
    obj.getFunction().add(entry);
    obj.setEntryFunc(new Reference(new ElementInfo(), entry));

    FuncPrivateVoid exit = makeFunc(hfsmExitFuncName, param.exit);
    obj.getFunction().add(exit);
    obj.setExitFunc(new Reference(new ElementInfo(), exit));
  }

  public FuncPrivateVoid makeFunc(String name, List<FunctionBase> list) {
    FuncPrivateVoid func = new FuncPrivateVoid(new ElementInfo(), name, new ListOfNamed<FuncVariable>());
    Block body = new Block(new ElementInfo());

    for (FunctionBase cf : list) {
      Statement stmt = makeCall(cf);
      body.getStatements().add(stmt);
    }

    func.setBody(body);
    return func;
  }

  private Statement makeCall(FunctionBase func) {
    assert (func.getParam().isEmpty());
    Reference ref = new Reference(new ElementInfo(), func);
    ref.getOffset().add(new RefCall(new ElementInfo(), new ArrayList<Expression>()));
    return new CallStmt(new ElementInfo(), ref);
  }

  @Override
  protected Void visitStateComposite(StateComposite obj, EePar param) {
    visitItr(obj.getItem(), param);
    changeEe(obj, param);
    return null;
  }

  @Override
  protected Void visitState(State obj, EePar param) {
    param = new EePar(param);
    param.entry.addLast(getFunc(obj.getEntryFunc()));
    param.exit.addFirst(getFunc(obj.getExitFunc()));
    return super.visitState(obj, param);
  }

  private FunctionBase getFunc(Reference ref) {
    assert (ref.getOffset().isEmpty());
    Named link = ref.getLink();
    assert (link instanceof FunctionBase);
    return (FunctionBase) link;
  }

}

class EePar {
  final public LinkedList<FunctionBase> entry;
  final public LinkedList<FunctionBase> exit;

  public EePar() {
    entry = new LinkedList<FunctionBase>();
    exit = new LinkedList<FunctionBase>();
  }

  public EePar(EePar parent) {
    entry = new LinkedList<FunctionBase>(parent.entry);
    exit = new LinkedList<FunctionBase>(parent.exit);
  }

}
