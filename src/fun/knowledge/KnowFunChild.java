package fun.knowledge;

import java.util.HashSet;
import java.util.Set;

import common.Direction;

import error.ErrorType;
import error.RError;
import fun.Fun;
import fun.NullTraverser;
import fun.composition.ImplComposition;
import fun.expression.reference.ReferenceLinked;
import fun.expression.reference.ReferenceUnlinked;
import fun.function.FunctionHeader;
import fun.generator.InterfaceGenerator;
import fun.hfsm.ImplHfsm;
import fun.hfsm.State;
import fun.hfsm.StateComposite;
import fun.hfsm.StateSimple;
import fun.other.Component;
import fun.other.ImplElementary;
import fun.other.Interface;
import fun.other.ListOfNamed;
import fun.other.Named;
import fun.other.Namespace;
import fun.type.base.EnumType;
import fun.type.composed.RecordType;
import fun.type.composed.UnionType;
import fun.variable.Variable;

public class KnowFunChild extends KnowledgeEntry {
  private KnowFunChildTraverser kct;

  @Override
  public void init(KnowledgeBase base) {
    kct = new KnowFunChildTraverser(base);
  }

  public Fun get(Fun root, Iterable<String> path) {
    for (String child : path) {
      root = get(root, child);
    }
    return root;
  }

  public Fun get(Fun sub, String name) {
    return getOrFind(sub, name, true);
  }

  public Fun find(Fun sub, String name) {
    return getOrFind(sub, name, false);
  }

  private Fun getOrFind(Fun sub, String name, boolean raiseError) {
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

class KnowFunChildTraverser extends NullTraverser<Set<Named>, String> {
  private KnowledgeBase kb;

  public KnowFunChildTraverser(KnowledgeBase kb) {
    super();
    this.kb = kb;
  }

  public Set<Named> retopt(Named res) {
    Set<Named> rset = new HashSet<Named>();
    if (res != null) {
      rset.add(res);
    }
    return rset;
  }

  @Override
  protected Set<Named> visitDefault(Fun obj, String param) {
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
    ListOfNamed<FunctionHeader> internalFunc = new ListOfNamed<FunctionHeader>(obj.getFunction().getItems(FunctionHeader.class, false));
    addIfFound(internalFunc.find(param), rset);
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
    addIfFound(obj.getBfunc().find(param), rset);
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
    KnowFun kt = kb.getEntry(KnowFun.class);
    Fun typ;
    if (obj.getType() instanceof ReferenceLinked) {
      ReferenceLinked ref = (ReferenceLinked) obj.getType();
      typ = ref.getLink();
    } else if (obj.getType() instanceof ReferenceUnlinked) {
      ReferenceUnlinked ref = (ReferenceUnlinked) obj.getType();
      typ = kt.get(ref.getName(), obj.getInfo());
    } else {
      RError.err(ErrorType.Fatal, obj.getInfo(), "Class not handled: " + obj.getType().getClass().getCanonicalName());
      return null;
    }
    assert (typ != null);
    return visit(typ, param);
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
  protected Set<Named> visitNamespace(Namespace obj, String param) {
    return retopt(obj.find(param));
  }

  @Override
  protected Set<Named> visitInterfaceGenerator(InterfaceGenerator obj, String param) {
    Set<Named> rset = visit(obj.getTemplate(), param);
    addIfFound(obj.getParam().find(param), rset);
    return rset;
  }

  @Override
  protected Set<Named> visitInterface(Interface obj, String param) {
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
