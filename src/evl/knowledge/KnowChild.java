package evl.knowledge;

import java.util.HashSet;
import java.util.Set;

import common.Direction;

import error.ErrorType;
import error.RError;
import evl.Evl;
import evl.NullTraverser;
import evl.composition.ImplComposition;
import evl.hfsm.ImplHfsm;
import evl.hfsm.State;
import evl.hfsm.StateComposite;
import evl.hfsm.StateSimple;
import evl.other.CompUse;
import evl.other.Component;
import evl.other.IfaceUse;
import evl.other.ImplElementary;
import evl.other.Interface;
import evl.other.ListOfNamed;
import evl.other.Named;
import evl.other.Namespace;
import evl.type.base.EnumType;
import evl.type.composed.NamedElement;
import evl.type.composed.RecordType;
import evl.type.composed.UnionType;
import evl.type.special.ComponentType;
import evl.type.special.InterfaceType;
import evl.variable.Variable;

public class KnowChild extends KnowledgeEntry {
  private KnowChildTraverser kct;

  @Override
  public void init(KnowledgeBase base) {
    kct = new KnowChildTraverser();
  }

  public Evl get(Evl sub, String name) {
    return getOrFind(sub, name, true);
  }

  public Evl find(Evl sub, String name) {
    return getOrFind(sub, name, false);
  }

  private Evl getOrFind(Evl sub, String name, boolean raiseError) {
    Set<Named> rset = kct.traverse(sub, name);
    if (rset.isEmpty()) {
      if (raiseError) {
        RError.err(ErrorType.Fatal, sub.getInfo(), "Name not found: " + name);
      }
      return null;
    }
    if (rset.size() == 1) {
      return rset.iterator().next();
    }
    if (raiseError) {
      RError.err(ErrorType.Fatal, sub.getInfo(), "Name not unique: " + name);
    }
    return null;
  }

}

class KnowChildTraverser extends NullTraverser<Set<Named>, String> {

  public Set<Named> retopt(Named res) {
    Set<Named> rset = new HashSet<Named>();
    if (res != null) {
      rset.add(res);
    }
    return rset;
  }

  @Override
  protected Set<Named> visitDefault(Evl obj, String param) {
    throw new RuntimeException("not yet implemented: " + obj.getClass().getCanonicalName());
  }

  @Override
  protected Set<Named> visitEnumType(EnumType obj, String param) {
    return retopt(obj.find(param));
  }

  @Override
  protected Set<Named> visitImplElementary(ImplElementary obj, String param) {
    Set<Named> rset = new HashSet<Named>();
    addIfFound(obj.getComponent().find(param), rset);
    addIfFound(obj.getConstant().find(param), rset);
    addIfFound(obj.getVariable().find(param), rset);
    return rset;
  }

  @Override
  protected Set<Named> visitComponentType(ComponentType obj, String param) {
    Set<Named> rset = new HashSet<Named>();
    addIfFound(obj.getIface(Direction.in).find(param), rset);
    addIfFound(obj.getIface(Direction.out).find(param), rset);
    return rset;
  }

  @Override
  protected Set<Named> visitImplComposition(ImplComposition obj, String param) {
    Set<Named> rset = new HashSet<Named>();
    addIfFound(obj.getComponent().find(param), rset);
    return rset;
  }

  @Override
  protected Set<Named> visitImplHfsm(ImplHfsm obj, String param) {
    Set<Named> rset = visit(obj.getTopstate(), param);
    if (obj.getTopstate().getName().equals(param)) {
      rset.add(obj.getTopstate());
    }
    return rset;
  }

  @Override
  protected Set<Named> visitState(State obj, String param) {
    Set<Named> rset = super.visitState(obj, param);
    addIfFound(obj.getVariable().find(param), rset);
    addIfFound(obj.getFunction().find(param), rset);
    return rset;
  }

  @Override
  protected Set<Named> visitStateSimple(StateSimple obj, String param) {
    return new HashSet<Named>();
  }

  @Override
  protected Set<Named> visitStateComposite(StateComposite obj, String param) {
    Set<Named> rset = new HashSet<Named>();
    ListOfNamed<State> children = new ListOfNamed<State>(obj.getItemList(State.class));
    addIfFound(children.find(param), rset);
    return rset;
  }

  @Override
  protected Set<Named> visitVariable(Variable obj, String param) {
    Evl typ = obj.getType();
    return visit(typ, param);
  }

  @Override
  protected Set<Named> visitCompUse(CompUse obj, String param) {
    return visit(obj.getLink(), param);
  }

  @Override
  protected Set<Named> visitIfaceUse(IfaceUse obj, String param) {
    return visit(obj.getLink(), param);
  }

  @Override
  protected Set<Named> visitRecordType(RecordType obj, String param) {
    return retopt(obj.getElement().find(param));
  }

  @Override
  protected Set<Named> visitUnionType(UnionType obj, String param) {
    return retopt(obj.getElement().find(param));
  }

  @Override
  protected Set<Named> visitNamedElement(NamedElement obj, String param) {
    return visit(obj.getType(), param);
  }

  @Override
  protected Set<Named> visitNamespace(Namespace obj, String param) {
    return retopt(obj.find(param));
  }

  @Override
  protected Set<Named> visitInterface(Interface obj, String param) {
    return retopt(obj.getPrototype().find(param));
  }

  @Override
  protected Set<Named> visitInterfaceType(InterfaceType obj, String param) {
    return retopt(obj.getPrototype().find(param));
  }

  private void addIfFound(Named item, Set<Named> rset) {
    if (item != null) {
      rset.add(item);
    }
  }

  @Override
  protected Set<Named> visitComponent(Component obj, String param) {
    Set<Named> rset = super.visitComponent(obj, param);
    addIfFound(obj.getIface(Direction.in).find(param), rset);
    addIfFound(obj.getIface(Direction.out).find(param), rset);
    return rset;
  }

}