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

package evl.knowledge;

import java.util.HashSet;
import java.util.Set;

import common.ElementInfo;

import error.ErrorType;
import error.RError;
import evl.Evl;
import evl.NullTraverser;
import evl.composition.ImplComposition;
import evl.expression.reference.SimpleRef;
import evl.function.Function;
import evl.hfsm.ImplHfsm;
import evl.hfsm.State;
import evl.hfsm.StateComposite;
import evl.hfsm.StateSimple;
import evl.other.CompUse;
import evl.other.Component;
import evl.other.EvlList;
import evl.other.ImplElementary;
import evl.other.Named;
import evl.other.Namespace;
import evl.type.base.EnumType;
import evl.type.composed.NamedElement;
import evl.type.composed.RecordType;
import evl.type.composed.UnionType;
import evl.type.composed.UnsafeUnionType;
import evl.type.special.ComponentType;
import evl.variable.Variable;

public class KnowChild extends KnowledgeEntry {
  private KnowChildTraverser kct;

  @Override
  public void init(KnowledgeBase base) {
    kct = new KnowChildTraverser();
  }

  public Evl get(Evl sub, String name, ElementInfo info) {
    return getOrFind(sub, name, true, info);
  }

  public Evl find(Evl sub, String name) {
    return getOrFind(sub, name, false, null);
  }

  private Evl getOrFind(Evl sub, String name, boolean raiseError, ElementInfo info) {
    Set<Evl> rset = kct.traverse(sub, name);
    if (rset.isEmpty()) {
      if (raiseError) {
        RError.err(ErrorType.Fatal, info, "Name not found: " + name);
      }
      return null;
    }
    if (rset.size() == 1) {
      return rset.iterator().next();
    }
    if (raiseError) {
      RError.err(ErrorType.Fatal, info, "Name not unique: " + name);
    }
    return null;
  }

}

class KnowChildTraverser extends NullTraverser<Set<Evl>, String> {

  public Set<Evl> retopt(Named res) {
    Set<Evl> rset = new HashSet<Evl>();
    if (res != null) {
      rset.add(res);
    }
    return rset;
  }

  @Override
  protected Set<Evl> visitDefault(Evl obj, String param) {
    throw new RuntimeException("Not yet implemented: " + obj.getClass().getCanonicalName());
    // RError.err(ErrorType.Warning, obj.getInfo(), "Element can not have a named child");
    // return new HashSet<Evl>();
  }

  @Override
  protected Set<Evl> visitSimpleRef(SimpleRef obj, String param) {
    return visit(obj.getLink(), param);
  }

  @Override
  protected Set<Evl> visitEnumType(EnumType obj, String param) {
    return retopt(obj.find(param));
  }

  @Override
  protected Set<Evl> visitComponent(Component obj, String param) {
    Set<Evl> rset = super.visitComponent(obj, param);
    addIfFound(obj.getIface().find(param), rset);
    addIfFound(obj.getFunction().find(param), rset);
    return rset;
  }

  @Override
  protected Set<Evl> visitImplElementary(ImplElementary obj, String param) {
    Set<Evl> rset = new HashSet<Evl>();
    addIfFound(obj.getComponent().find(param), rset);
    addIfFound(obj.getType().find(param), rset);
    addIfFound(obj.getConstant().find(param), rset);
    addIfFound(obj.getVariable().find(param), rset);
    addIfFound(obj.getSubCallback().find(param), rset);
    return rset;
  }

  @Override
  protected Set<Evl> visitImplComposition(ImplComposition obj, String param) {
    Set<Evl> rset = new HashSet<Evl>();
    addIfFound(obj.getComponent().find(param), rset);
    return rset;
  }

  @Override
  protected Set<Evl> visitImplHfsm(ImplHfsm obj, String param) {
    Set<Evl> rset = visit(obj.getTopstate(), param);
    if (obj.getTopstate().getName().equals(param)) {
      rset.add(obj.getTopstate());
    }
    return rset;
  }

  @Override
  protected Set<Evl> visitState(State obj, String param) {
    Set<Evl> rset = super.visitState(obj, param);
    addIfFound(obj.getItem().find(param), rset);
    return rset;
  }

  @Override
  protected Set<Evl> visitStateSimple(StateSimple obj, String param) {
    return new HashSet<Evl>();
  }

  @Override
  protected Set<Evl> visitStateComposite(StateComposite obj, String param) {
    Set<Evl> rset = new HashSet<Evl>();
    EvlList<State> children = new EvlList<State>(obj.getItem().getItems(State.class));
    addIfFound(children.find(param), rset);
    return rset;
  }

  @Override
  protected Set<Evl> visitVariable(Variable obj, String param) {
    Evl typ = obj.getType();
    return visit(typ, param);
  }

  @Override
  protected Set<Evl> visitCompUse(CompUse obj, String param) {
    return visit(obj.getLink(), param);
  }

  @Override
  protected Set<Evl> visitRecordType(RecordType obj, String param) {
    return retopt(obj.getElement().find(param));
  }

  @Override
  protected Set<Evl> visitUnionType(UnionType obj, String param) {
    Set<Evl> rset = retopt(obj.getElement().find(param));
    if (obj.getTag().getName() == param) {
      rset.add(obj.getTag());
    }
    return rset;
  }

  @Override
  protected Set<Evl> visitUnsafeUnionType(UnsafeUnionType obj, String param) {
    return retopt(obj.getElement().find(param));
  }

  @Override
  protected Set<Evl> visitComponentType(ComponentType obj, String param) {
    Set<Evl> rset = new HashSet<Evl>();
    addIfFound(obj.getInput().find(param), rset);
    addIfFound(obj.getOutput().find(param), rset);
    return rset;
  }

  @Override
  protected Set<Evl> visitNamedElement(NamedElement obj, String param) {
    return visit(obj.getRef(), param);
  }

  @Override
  protected Set<Evl> visitNamespace(Namespace obj, String param) {
    Set<Evl> rset = new HashSet<Evl>();
    addIfFound(obj.getChildren().find(param), rset);
    return rset;
  }

  private void addIfFound(Evl item, Set<Evl> rset) {
    if (item != null) {
      rset.add(item);
    }
  }

  @Override
  protected Set<Evl> visitFunction(Function obj, String param) {
    return new HashSet<Evl>();
  }

}
