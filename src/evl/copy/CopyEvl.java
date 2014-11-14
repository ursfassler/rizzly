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

import evl.Evl;
import evl.NullTraverser;
import evl.expression.Expression;
import evl.expression.reference.RefItem;
import evl.function.Function;
import evl.hfsm.StateItem;
import evl.hfsm.Transition;
import evl.other.EvlList;
import evl.other.ImplElementary;
import evl.other.Named;
import evl.other.Queue;
import evl.other.RizzlyProgram;
import evl.other.SubCallbacks;
import evl.statement.CaseOpt;
import evl.statement.CaseOptRange;
import evl.statement.CaseOptValue;
import evl.statement.IfOption;
import evl.statement.Statement;
import evl.type.Type;
import evl.type.base.EnumElement;
import evl.type.composed.NamedElement;
import evl.variable.Variable;

class CopyEvl extends NullTraverser<Evl, Void> {
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
      nobj.properties().putAll(obj.properties());
      return nobj;
    }
  }

  @Override
  protected Evl visitRizzlyProgram(RizzlyProgram obj, Void param) {
    RizzlyProgram ret = new RizzlyProgram(obj.getName());
    ret.getConstant().addAll(copy(obj.getConstant()));
    ret.getFunction().addAll(copy(obj.getFunction()));
    ret.getType().addAll(copy(obj.getType()));
    ret.getVariable().addAll(copy(obj.getVariable()));
    return ret;
  }

  @Override
  protected Evl visitQueue(Queue obj, Void param) {
    return new Queue();
  }

  @Override
  protected Evl visitSubCallbacks(SubCallbacks obj, Void param) {
    SubCallbacks ret = new SubCallbacks(obj.getInfo(), copy(obj.getCompUse()));
    ret.getFunc().addAll(copy(obj.getFunc()));
    return ret;
  }

  @Override
  protected Evl visitFunctionImpl(Function obj, Void param) {
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
  protected Evl visitImplElementary(ImplElementary obj, Void param) {
    ImplElementary ret = new ImplElementary(obj.getInfo(), obj.getName(), copy(obj.getEntryFunc()), copy(obj.getExitFunc()));

    ret.getFunction().addAll(copy(obj.getFunction()));
    ret.getIface().addAll(copy(obj.getIface()));

    ret.setQueue(copy(obj.getQueue()));
    ret.getVariable().addAll(copy(obj.getVariable()));
    ret.getConstant().addAll(copy(obj.getConstant()));
    ret.getComponent().addAll(copy(obj.getComponent()));
    ret.getSubCallback().addAll(copy(obj.getSubCallback()));

    return ret;
  }

  @Override
  protected StateItem visitTransition(Transition obj, Void param) {
    return new Transition(obj.getInfo(), copy(obj.getSrc()), copy(obj.getDst()), copy(obj.getEventFunc()), copy(obj.getGuard()), copy(obj.getParam()), copy(obj.getBody()));
  }

  @Override
  protected Evl visitEnumElement(EnumElement obj, Void param) {
    return new EnumElement(obj.getInfo(), obj.getName());
  }

  @Override
  protected Evl visitIfOption(IfOption obj, Void param) {
    return new IfOption(obj.getInfo(), copy(obj.getCondition()), copy(obj.getCode()));
  }

  @Override
  protected Evl visitCaseOpt(CaseOpt obj, Void param) {
    return new CaseOpt(obj.getInfo(), copy(obj.getValue()), copy(obj.getCode()));
  }

  @Override
  protected Evl visitCaseOptValue(CaseOptValue obj, Void param) {
    return new CaseOptValue(obj.getInfo(), copy(obj.getValue()));
  }

  @Override
  protected Evl visitCaseOptRange(CaseOptRange obj, Void param) {
    return new CaseOptRange(obj.getInfo(), copy(obj.getStart()), copy(obj.getEnd()));
  }

  @Override
  protected Evl visitNamedElement(NamedElement obj, Void param) {
    return new NamedElement(obj.getInfo(), obj.getName(), copy(obj.getRef()));
  }

}
