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

package ast.knowledge;

import java.util.HashSet;
import java.util.Set;

import ast.ElementInfo;
import ast.data.Ast;
import ast.data.AstList;
import ast.data.Named;
import ast.data.Namespace;
import ast.data.component.Component;
import ast.data.component.composition.CompUse;
import ast.data.component.composition.ImplComposition;
import ast.data.component.elementary.ImplElementary;
import ast.data.component.hfsm.ImplHfsm;
import ast.data.component.hfsm.State;
import ast.data.component.hfsm.StateComposite;
import ast.data.component.hfsm.StateSimple;
import ast.data.expression.reference.SimpleRef;
import ast.data.function.Function;
import ast.data.type.base.EnumType;
import ast.data.type.composed.NamedElement;
import ast.data.type.composed.RecordType;
import ast.data.type.composed.UnionType;
import ast.data.type.composed.UnsafeUnionType;
import ast.data.type.special.ComponentType;
import ast.data.variable.Variable;
import ast.traverser.NullTraverser;
import ast.traverser.other.ClassGetter;
import error.ErrorType;
import error.RError;

public class KnowChild extends KnowledgeEntry {
  private KnowChildTraverser kct;

  @Override
  public void init(KnowledgeBase base) {
    kct = new KnowChildTraverser();
  }

  public Ast get(Ast root, Iterable<String> path, ElementInfo info) {
    for (String child : path) {
      root = get(root, child, info);
    }
    return root;
  }

  public Ast get(Ast sub, String name, ElementInfo info) {
    return getOrFind(sub, name, true, info);
  }

  public Ast find(Ast sub, String name) {
    return getOrFind(sub, name, false, null);
  }

  private Ast getOrFind(Ast sub, String name, boolean raiseError, ElementInfo info) {
    Set<Ast> rset = kct.traverse(sub, name);
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

class KnowChildTraverser extends NullTraverser<Set<Ast>, String> {

  public Set<Ast> retopt(Named res) {
    Set<Ast> rset = new HashSet<Ast>();
    if (res != null) {
      rset.add(res);
    }
    return rset;
  }

  @Override
  protected Set<Ast> visitDefault(Ast obj, String param) {
    throw new RuntimeException("Not yet implemented: " + obj.getClass().getCanonicalName());
    // RError.err(ErrorType.Warning, obj.getInfo(),
    // "Element can not have a named child");
    // return new HashSet<Ast>();
  }

  @Override
  protected Set<Ast> visitSimpleRef(SimpleRef obj, String param) {
    return visit(obj.link, param);
  }

  @Override
  protected Set<Ast> visitEnumType(EnumType obj, String param) {
    return retopt(obj.find(param));
  }

  @Override
  protected Set<Ast> visitComponent(Component obj, String param) {
    Set<Ast> rset = super.visitComponent(obj, param);
    addIfFound(obj.iface.find(param), rset);
    addIfFound(obj.function.find(param), rset);
    return rset;
  }

  @Override
  protected Set<Ast> visitImplElementary(ImplElementary obj, String param) {
    Set<Ast> rset = new HashSet<Ast>();
    addIfFound(obj.component.find(param), rset);
    addIfFound(obj.type.find(param), rset);
    addIfFound(obj.constant.find(param), rset);
    addIfFound(obj.variable.find(param), rset);
    addIfFound(obj.subCallback.find(param), rset);
    return rset;
  }

  @Override
  protected Set<Ast> visitImplComposition(ImplComposition obj, String param) {
    Set<Ast> rset = new HashSet<Ast>();
    addIfFound(obj.component.find(param), rset);
    return rset;
  }

  @Override
  protected Set<Ast> visitImplHfsm(ImplHfsm obj, String param) {
    Set<Ast> rset = visit(obj.topstate, param);
    if (obj.topstate.name.equals(param)) {
      rset.add(obj.topstate);
    }
    return rset;
  }

  @Override
  protected Set<Ast> visitState(State obj, String param) {
    Set<Ast> rset = super.visitState(obj, param);
    addIfFound(obj.item.find(param), rset);
    return rset;
  }

  @Override
  protected Set<Ast> visitStateSimple(StateSimple obj, String param) {
    return new HashSet<Ast>();
  }

  @Override
  protected Set<Ast> visitStateComposite(StateComposite obj, String param) {
    Set<Ast> rset = new HashSet<Ast>();
    AstList<State> children = new AstList<State>(ClassGetter.filter(State.class, obj.item));
    addIfFound(children.find(param), rset);
    return rset;
  }

  @Override
  protected Set<Ast> visitVariable(Variable obj, String param) {
    Ast typ = obj.type;
    return visit(typ, param);
  }

  @Override
  protected Set<Ast> visitCompUse(CompUse obj, String param) {
    return visit(obj.compRef, param);
  }

  @Override
  protected Set<Ast> visitRecordType(RecordType obj, String param) {
    return retopt(obj.element.find(param));
  }

  @Override
  protected Set<Ast> visitUnionType(UnionType obj, String param) {
    Set<Ast> rset = retopt(obj.element.find(param));
    if (obj.tag.name == param) {
      rset.add(obj.tag);
    }
    return rset;
  }

  @Override
  protected Set<Ast> visitUnsafeUnionType(UnsafeUnionType obj, String param) {
    return retopt(obj.element.find(param));
  }

  @Override
  protected Set<Ast> visitComponentType(ComponentType obj, String param) {
    Set<Ast> rset = new HashSet<Ast>();
    addIfFound(obj.input.find(param), rset);
    addIfFound(obj.output.find(param), rset);
    return rset;
  }

  @Override
  protected Set<Ast> visitNamedElement(NamedElement obj, String param) {
    return visit(obj.typeref, param);
  }

  @Override
  protected Set<Ast> visitNamespace(Namespace obj, String param) {
    Set<Ast> rset = new HashSet<Ast>();
    addIfFound(obj.children.find(param), rset);
    return rset;
  }

  private void addIfFound(Ast item, Set<Ast> rset) {
    if (item != null) {
      rset.add(item);
    }
  }

  @Override
  protected Set<Ast> visitFunction(Function obj, String param) {
    return new HashSet<Ast>();
  }

}
