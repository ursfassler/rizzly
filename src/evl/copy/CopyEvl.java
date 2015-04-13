/**
 *  This file is part of Rizzly.
 *
 *  Rizzly is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Rizzly is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with Rizzly.  If not, see <http://www.gnu.org/licenses/>.
 */

package evl.copy;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import common.Direction;

import evl.data.Evl;
import evl.data.EvlList;
import evl.data.Named;
import evl.data.Namespace;
import evl.data.component.composition.AsynchroniusConnection;
import evl.data.component.composition.CompUse;
import evl.data.component.composition.EndpointRaw;
import evl.data.component.composition.Queue;
import evl.data.component.composition.SubCallbacks;
import evl.data.component.composition.SynchroniusConnection;
import evl.data.component.elementary.ImplElementary;
import evl.data.component.hfsm.StateComposite;
import evl.data.component.hfsm.StateSimple;
import evl.data.component.hfsm.Transition;
import evl.data.expression.Expression;
import evl.data.expression.NamedValue;
import evl.data.expression.reference.RefItem;
import evl.data.function.Function;
import evl.data.function.ret.FuncReturnNone;
import evl.data.function.ret.FuncReturnTuple;
import evl.data.function.ret.FuncReturnType;
import evl.data.statement.CaseOpt;
import evl.data.statement.CaseOptRange;
import evl.data.statement.CaseOptValue;
import evl.data.statement.IfOption;
import evl.data.statement.Statement;
import evl.data.type.Type;
import evl.data.type.base.EnumElement;
import evl.data.type.composed.NamedElement;
import evl.data.variable.Variable;
import evl.traverser.NullTraverser;
import fun.other.RawComposition;
import fun.other.RawElementary;
import fun.other.RawHfsm;
import fun.other.Template;

public class CopyEvl extends NullTraverser<Evl, Void> {
  // / keeps the old -> new Named objects in order to relink references
  final private Map<Named, Named> copied = new HashMap<Named, Named>();
  final private CopyFunction func = new CopyFunction(this);
  final private CopyVariable var = new CopyVariable(this);
  final private CopyExpression expr = new CopyExpression(this);
  final private CopyType type = new CopyType(this);
  final private CopyStatement stmt = new CopyStatement(this);
  final private CopyRef ref = new CopyRef(this);

  public Map<Named, Named> getCopied() {
    return copied;
  }

  @SuppressWarnings("unchecked")
  public <T extends Evl> T copy(T obj) {
    return (T) visit(obj, null);
  }

  public <T extends Evl> Collection<T> copy(Collection<T> obj) {
    ArrayList<T> ret = new ArrayList<T>();
    for (T itr : obj) {
      ret.add(copy(itr));
    }
    return ret;
  }

  public <T extends Evl> EvlList<T> copy(EvlList<T> obj) {
    EvlList<T> ret = new EvlList<T>();
    for (T itr : obj) {
      ret.add(copy(itr));
    }
    return ret;
  }

  @Override
  protected Evl visitDefault(Evl obj, Void param) {
    throw new RuntimeException("not yet implemented: " + obj.getClass().getCanonicalName());
  }

  @Override
  protected Evl visit(Evl obj, Void param) {
    if (copied.containsKey(obj)) {
      Evl ret = copied.get(obj);
      assert (ret != null);
      return ret;
    } else {
      Evl nobj = super.visit(obj, param);
      if (obj instanceof Named) {
        copied.put((Named) obj, (Named) nobj);
      }
      return nobj;
    }
  }

  @Override
  protected Evl visitQueue(Queue obj, Void param) {
    return new Queue();
  }

  @Override
  protected Evl visitSubCallbacks(SubCallbacks obj, Void param) {
    SubCallbacks ret = new SubCallbacks(obj.getInfo(), copy(obj.compUse));
    ret.func.addAll(copy(obj.func));
    return ret;
  }

  @Override
  protected Evl visitFunction(Function obj, Void param) {
    return func.traverse(obj, param);
  }

  @Override
  protected Evl visitVariable(Variable obj, Void param) {
    return var.traverse(obj, param);
  }

  @Override
  protected Evl visitExpression(Expression obj, Void param) {
    return expr.traverse(obj, param);
  }

  @Override
  protected Evl visitType(Type obj, Void param) {
    return type.traverse(obj, param);
  }

  @Override
  protected Evl visitStatement(Statement obj, Void param) {
    return stmt.traverse(obj, param);
  }

  @Override
  protected Evl visitRefItem(RefItem obj, Void param) {
    return ref.traverse(obj, param);
  }

  @Override
  protected Evl visitFuncReturnTuple(FuncReturnTuple obj, Void param) {
    return new FuncReturnTuple(obj.getInfo(), copy(obj.param));
  }

  @Override
  protected Evl visitFuncReturnType(FuncReturnType obj, Void param) {
    return new FuncReturnType(obj.getInfo(), copy(obj.type));
  }

  @Override
  protected Evl visitFuncReturnNone(FuncReturnNone obj, Void param) {
    return new FuncReturnNone(obj.getInfo());
  }

  @Override
  protected Evl visitNamespace(Namespace obj, Void param) {
    Namespace ret = new Namespace(obj.getInfo(), obj.name);
    ret.children.addAll(copy(obj.children));
    return ret;
  }

  @Override
  protected Evl visitImplElementary(ImplElementary obj, Void param) {
    ImplElementary ret = new ImplElementary(obj.getInfo(), obj.name, copy(obj.entryFunc), copy(obj.exitFunc));

    ret.function.addAll(copy(obj.function));
    ret.iface.addAll(copy(obj.iface));

    ret.queue = copy(obj.queue);
    ret.type.addAll(copy(obj.type));
    ret.variable.addAll(copy(obj.variable));
    ret.constant.addAll(copy(obj.constant));
    ret.component.addAll(copy(obj.component));
    ret.subCallback.addAll(copy(obj.subCallback));

    return ret;
  }

  @Override
  protected Evl visitTransition(Transition obj, Void param) {
    return new Transition(obj.getInfo(), copy(obj.src), copy(obj.dst), copy(obj.eventFunc), copy(obj.guard), copy(obj.param), copy(obj.body));
  }

  @Override
  protected Evl visitEnumElement(EnumElement obj, Void param) {
    return new EnumElement(obj.getInfo(), obj.name);
  }

  @Override
  protected Evl visitIfOption(IfOption obj, Void param) {
    return new IfOption(obj.getInfo(), copy(obj.condition), copy(obj.code));
  }

  @Override
  protected Evl visitCaseOpt(CaseOpt obj, Void param) {
    return new CaseOpt(obj.getInfo(), copy(obj.value), copy(obj.code));
  }

  @Override
  protected Evl visitCaseOptValue(CaseOptValue obj, Void param) {
    return new CaseOptValue(obj.getInfo(), copy(obj.value));
  }

  @Override
  protected Evl visitCaseOptRange(CaseOptRange obj, Void param) {
    return new CaseOptRange(obj.getInfo(), copy(obj.start), copy(obj.end));
  }

  @Override
  protected Evl visitNamedElement(NamedElement obj, Void param) {
    return new NamedElement(obj.getInfo(), obj.name, copy(obj.typeref));
  }

  @Override
  protected Evl visitNamedValue(NamedValue obj, Void param) {
    return new NamedValue(obj.getInfo(), obj.name, copy(obj.value));
  }

  @Override
  protected Evl visitCompUse(CompUse obj, Void param) {
    return new CompUse(obj.getInfo(), obj.name, copy(obj.compRef)); // we keep
    // link to
    // old type
  }

  @Override
  protected Evl visitRawHfsm(RawHfsm obj, Void param) {
    RawHfsm ret = new RawHfsm(obj.getInfo(), obj.name, copy(obj.getTopstate()));

    ret.getIface().addAll(copy(obj.getIface()));

    return ret;
  }

  @Override
  protected Evl visitRawComposition(RawComposition obj, Void param) {
    RawComposition ret = new RawComposition(obj.getInfo(), obj.name);

    ret.getIface().addAll(copy(obj.getIface()));
    ret.getInstantiation().addAll(copy(obj.getInstantiation()));
    ret.getConnection().addAll(copy(obj.getConnection()));

    return ret;
  }

  @Override
  protected Evl visitRawElementary(RawElementary obj, Void param) {
    RawElementary ret = new RawElementary(obj.getInfo(), obj.name);

    ret.getIface().addAll(copy(obj.getIface()));
    ret.getDeclaration().addAll(copy(obj.getDeclaration()));
    ret.getInstantiation().addAll(copy(obj.getInstantiation()));
    ret.setEntryFunc(copy(obj.getEntryFunc()));
    ret.setExitFunc(copy(obj.getExitFunc()));

    return ret;
  }

  @Override
  protected Evl visitStateSimple(StateSimple obj, Void param) {
    evl.data.component.hfsm.StateSimple ret = new StateSimple(obj.getInfo(), obj.name, copy(obj.entryFunc), copy(obj.exitFunc));

    ret.item.addAll(copy(obj.item));

    return ret;
  }

  @Override
  protected Evl visitStateComposite(StateComposite obj, Void param) {
    evl.data.component.hfsm.StateComposite ret = new StateComposite(obj.getInfo(), obj.name, copy(obj.entryFunc), copy(obj.exitFunc), copy(obj.initial));

    ret.item.addAll(copy(obj.item));

    return ret;
  }

  @Override
  protected Evl visitTemplate(Template obj, Void param) {
    return new Template(obj.getInfo(), obj.name, copy(obj.getTempl()), copy(obj.getObject()));
  }

  @Override
  protected Evl visitSynchroniusConnection(SynchroniusConnection obj, Void param) {
    return new SynchroniusConnection(obj.getInfo(), copy(obj.endpoint.get(Direction.in)), copy(obj.endpoint.get(Direction.out)));
  }

  @Override
  protected Evl visitAsynchroniusConnection(AsynchroniusConnection obj, Void param) {
    return new AsynchroniusConnection(obj.getInfo(), copy(obj.endpoint.get(Direction.in)), copy(obj.endpoint.get(Direction.out)));
  }

  @Override
  protected Evl visitEndpointRaw(EndpointRaw obj, Void param) {
    return new EndpointRaw(obj.getInfo(), copy(obj.ref));
  }

}
