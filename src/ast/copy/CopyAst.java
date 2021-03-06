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

package ast.copy;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import ast.data.Ast;
import ast.data.AstList;
import ast.data.Named;
import ast.data.Namespace;
import ast.data.component.composition.AsynchroniusConnection;
import ast.data.component.composition.ComponentUse;
import ast.data.component.composition.EndpointRaw;
import ast.data.component.composition.Queue;
import ast.data.component.composition.SubCallbacks;
import ast.data.component.composition.SynchroniusConnection;
import ast.data.component.elementary.ImplElementary;
import ast.data.component.hfsm.StateComposite;
import ast.data.component.hfsm.StateSimple;
import ast.data.component.hfsm.Transition;
import ast.data.expression.Expression;
import ast.data.expression.ReferenceExpression;
import ast.data.expression.value.NamedValue;
import ast.data.function.Function;
import ast.data.function.ret.FuncReturnNone;
import ast.data.function.ret.FuncReturnTuple;
import ast.data.function.ret.FunctionReturnType;
import ast.data.raw.RawComposition;
import ast.data.raw.RawElementary;
import ast.data.raw.RawHfsm;
import ast.data.reference.LinkedAnchor;
import ast.data.reference.OffsetReference;
import ast.data.reference.RefItem;
import ast.data.reference.UnlinkedAnchor;
import ast.data.statement.CaseOpt;
import ast.data.statement.CaseOptRange;
import ast.data.statement.CaseOptValue;
import ast.data.statement.IfOption;
import ast.data.statement.Statement;
import ast.data.template.Template;
import ast.data.type.Type;
import ast.data.type.base.EnumElement;
import ast.data.type.composed.NamedElement;
import ast.data.variable.Variable;
import ast.dispatcher.NullDispatcher;

public class CopyAst extends NullDispatcher<Ast, Void> {
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
  public <T extends Ast> T copy(T obj) {
    return (T) visit(obj, null);
  }

  public <T extends Ast> Collection<T> copy(Collection<T> obj) {
    ArrayList<T> ret = new ArrayList<T>();
    for (T itr : obj) {
      ret.add(copy(itr));
    }
    return ret;
  }

  public <T extends Ast> AstList<T> copy(AstList<T> obj) {
    AstList<T> ret = new AstList<T>();
    for (T itr : obj) {
      ret.add(copy(itr));
    }
    return ret;
  }

  @Override
  protected Ast visitDefault(Ast obj, Void param) {
    throw new RuntimeException("not yet implemented: " + obj.getClass().getCanonicalName());
  }

  @Override
  protected Ast visit(Ast obj, Void param) {
    if (copied.containsKey(obj)) {
      Ast ret = copied.get(obj);
      assert (ret != null);
      return ret;
    } else {
      Ast nobj = super.visit(obj, param);
      nobj.metadata().add(obj.metadata());
      if (obj instanceof Named) {
        copied.put((Named) obj, (Named) nobj);
      }
      return nobj;
    }
  }

  @Override
  protected Ast visitQueue(Queue obj, Void param) {
    return new Queue();
  }

  @Override
  protected Ast visitSubCallbacks(SubCallbacks obj, Void param) {
    SubCallbacks ret = new SubCallbacks(copy(obj.compUse));
    ret.func.addAll(copy(obj.func));
    return ret;
  }

  @Override
  protected Ast visitFunction(Function obj, Void param) {
    return func.traverse(obj, param);
  }

  @Override
  protected Ast visitVariable(Variable obj, Void param) {
    return var.traverse(obj, param);
  }

  @Override
  protected Ast visitExpression(Expression obj, Void param) {
    return expr.traverse(obj, param);
  }

  @Override
  protected Ast visitType(Type obj, Void param) {
    return type.traverse(obj, param);
  }

  @Override
  protected Ast visitStatement(Statement obj, Void param) {
    return stmt.traverse(obj, param);
  }

  @Override
  protected Ast visitRefItem(RefItem obj, Void param) {
    return ref.traverse(obj, param);
  }

  @Override
  protected Ast visitFuncReturnTuple(FuncReturnTuple obj, Void param) {
    return new FuncReturnTuple(copy(obj.param));
  }

  @Override
  protected Ast visitFuncReturnType(FunctionReturnType obj, Void param) {
    return new FunctionReturnType(copy(obj.type));
  }

  @Override
  protected Ast visitFuncReturnNone(FuncReturnNone obj, Void param) {
    return new FuncReturnNone();
  }

  @Override
  protected Ast visitNamespace(Namespace obj, Void param) {
    Namespace ret = new Namespace(obj.getName());
    ret.children.addAll(copy(obj.children));
    return ret;
  }

  @Override
  protected Ast visitImplElementary(ImplElementary obj, Void param) {
    ImplElementary ret = new ImplElementary(obj.getName(), copy(obj.entryFunc), copy(obj.exitFunc));

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
  protected Ast visitTransition(Transition obj, Void param) {
    return new Transition(copy(obj.src), copy(obj.dst), copy(obj.eventFunc), copy(obj.guard), copy(obj.param), copy(obj.body));
  }

  @Override
  protected Ast visitEnumElement(EnumElement obj, Void param) {
    return new EnumElement(obj.getName());
  }

  @Override
  protected Ast visitIfOption(IfOption obj, Void param) {
    return new IfOption(copy(obj.condition), copy(obj.code));
  }

  @Override
  protected Ast visitCaseOpt(CaseOpt obj, Void param) {
    return new CaseOpt(copy(obj.value), copy(obj.code));
  }

  @Override
  protected Ast visitCaseOptValue(CaseOptValue obj, Void param) {
    return new CaseOptValue(copy(obj.value));
  }

  @Override
  protected Ast visitCaseOptRange(CaseOptRange obj, Void param) {
    return new CaseOptRange(copy(obj.start), copy(obj.end));
  }

  @Override
  protected Ast visitNamedElement(NamedElement obj, Void param) {
    return new NamedElement(obj.getName(), copy(obj.typeref));
  }

  @Override
  protected Ast visitNamedValue(NamedValue obj, Void param) {
    return new NamedValue(obj.name, copy(obj.value));
  }

  @Override
  protected Ast visitCompUse(ComponentUse obj, Void param) {
    return new ComponentUse(obj.getName(), copy(obj.getCompRef())); // we keep
    // link to
    // old type
  }

  @Override
  protected Ast visitRawHfsm(RawHfsm obj, Void param) {
    RawHfsm ret = new RawHfsm(obj.getName(), copy(obj.getTopstate()));

    ret.getIface().addAll(copy(obj.getIface()));

    return ret;
  }

  @Override
  protected Ast visitRawComposition(RawComposition obj, Void param) {
    RawComposition ret = new RawComposition(obj.getName());

    ret.getIface().addAll(copy(obj.getIface()));
    ret.getInstantiation().addAll(copy(obj.getInstantiation()));
    ret.getConnection().addAll(copy(obj.getConnection()));

    return ret;
  }

  @Override
  protected Ast visitRawElementary(RawElementary obj, Void param) {
    RawElementary ret = new RawElementary(obj.getName());

    ret.getIface().addAll(copy(obj.getIface()));
    ret.getDeclaration().addAll(copy(obj.getDeclaration()));
    ret.getInstantiation().addAll(copy(obj.getInstantiation()));
    ret.setEntryFunc(copy(obj.getEntryFunc()));
    ret.setExitFunc(copy(obj.getExitFunc()));

    return ret;
  }

  @Override
  protected Ast visitStateSimple(StateSimple obj, Void param) {
    StateSimple ret = new StateSimple(obj.getName(), copy(obj.entryFunc), copy(obj.exitFunc));

    ret.item.addAll(copy(obj.item));

    return ret;
  }

  @Override
  protected Ast visitStateComposite(StateComposite obj, Void param) {
    ast.data.component.hfsm.StateComposite ret = new StateComposite(obj.getName(), copy(obj.entryFunc), copy(obj.exitFunc), copy(obj.initial));

    ret.item.addAll(copy(obj.item));

    return ret;
  }

  @Override
  protected Ast visitTemplate(Template obj, Void param) {
    return new Template(obj.getName(), copy(obj.getTempl()), copy(obj.getObject()));
  }

  @Override
  protected Ast visitSynchroniusConnection(SynchroniusConnection obj, Void param) {
    return new SynchroniusConnection(copy(obj.getSrc()), copy(obj.getDst()));
  }

  @Override
  protected Ast visitAsynchroniusConnection(AsynchroniusConnection obj, Void param) {
    return new AsynchroniusConnection(copy(obj.getSrc()), copy(obj.getDst()));
  }

  @Override
  protected Ast visitEndpointRaw(EndpointRaw obj, Void param) {
    return new EndpointRaw(copy(obj.getRef()));
  }

  @Override
  protected Ast visitRefExpr(ReferenceExpression obj, Void param) {
    throw new RuntimeException("not yet implemented");
  }

  @Override
  protected Ast visitOffsetReference(OffsetReference obj, Void param) {
    return new OffsetReference(copy(obj.getAnchor()), copy(obj.getOffset()));
  }

  @Override
  protected Ast visitUnlinkedAnchor(UnlinkedAnchor obj, Void param) {
    return new UnlinkedAnchor(obj.getLinkName());
  }

  @Override
  protected Ast visitLinkedAnchor(LinkedAnchor obj, Void param) {
    return new LinkedAnchor(obj.getLink());
  }

}
