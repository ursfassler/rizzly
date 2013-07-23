package fun.traverser;

import common.Direction;

import fun.composition.Connection;
import fun.expression.Expression;
import fun.expression.reference.Reference;
import fun.expression.reference.ReferenceLinked;
import fun.expression.reference.ReferenceUnlinked;
import fun.function.FuncWithReturn;
import fun.function.FunctionHeader;
import fun.hfsm.State;
import fun.hfsm.StateComposite;
import fun.hfsm.Transition;
import fun.statement.CallStmt;
import fun.type.base.TypeAlias;
import fun.type.composed.NamedElement;
import fun.type.genfunc.Array;
import fun.type.genfunc.TypeType;
import fun.variable.Variable;

public abstract class RefReplacer<T> extends ExprReplacer<T> {

  @Override
  protected abstract Reference visitReferenceUnlinked(ReferenceUnlinked obj, T param);

  @Override
  protected abstract Reference visitReferenceLinked(ReferenceLinked obj, T param);

  @Override
  protected Expression visitCallStmt(CallStmt obj, T param) {
    obj.setCall((Reference) visit(obj.getCall(), param));
    return null;
  }

  @Override
  protected Expression visitConnection(Connection obj, T param) {
    obj.setEndpoint(Direction.in, (Reference) visit(obj.getEndpoint(Direction.in), param));
    obj.setEndpoint(Direction.out, (Reference) visit(obj.getEndpoint(Direction.out), param));
    return null;
  }

  @Override
  protected Expression visitArray(Array obj, T param) {
    assert (false);
    obj.setType((Reference) visit(obj.getType(), param));
    return obj;
  }

  @Override
  protected Expression visitTypeType(TypeType obj, T param) {
    obj.setType((Reference) visit(obj.getType(), param));
    return obj;
  }

  @Override
  protected Expression visitTransition(Transition obj, T param) {
    obj.setSrc((Reference) visit(obj.getSrc(), param));
    obj.setDst((Reference) visit(obj.getDst(), param));
    obj.setEvent((Reference) visit(obj.getEvent(), param));
    visitList(obj.getParam(), param);
    obj.setGuard(visit(obj.getGuard(), param));
    visit(obj.getBody(), param);
    return null;
  }

  @Override
  protected Expression visitStateComposite(StateComposite obj, T param) {
    obj.setInitial((Reference) visit(obj.getInitial(), param));
    return super.visitStateComposite(obj, param);
  }

  @Override
  protected Expression visitState(State obj, T param) {
    obj.setEntryFuncRef((Reference) visit(obj.getEntryFuncRef(), param));
    obj.setExitFuncRef((Reference) visit(obj.getExitFuncRef(), param));
    return super.visitState(obj, param);
  }

  @Override
  protected Expression visitNamedElement(NamedElement obj, T param) {
    obj.setType((Reference) visit(obj.getType(), param));
    return super.visitNamedElement(obj, param);
  }

  @Override
  protected Expression visitTypeAlias(TypeAlias obj, T param) {
    obj.setRef((Reference) visit(obj.getRef(), param));
    return super.visitTypeAlias(obj, param);
  }

  @Override
  protected Expression visitVariable(Variable obj, T param) {
    obj.setType((Reference) visit(obj.getType(), param));
    return super.visitVariable(obj, param);
  }

  @Override
  protected Expression visitFunctionHeader(FunctionHeader obj, T param) {
    if (obj instanceof FuncWithReturn) {
      ((FuncWithReturn) obj).setRet(visit(((FuncWithReturn) obj).getRet(), param));
    }
    return super.visitFunctionHeader(obj, param);
  }

}
