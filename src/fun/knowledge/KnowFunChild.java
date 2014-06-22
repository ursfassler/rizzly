package fun.knowledge;

import java.util.HashSet;
import java.util.Set;

import error.ErrorType;
import error.RError;
import fun.Fun;
import fun.NullTraverser;
import fun.composition.ImplComposition;
import fun.hfsm.ImplHfsm;
import fun.hfsm.State;
import fun.hfsm.StateComposite;
import fun.hfsm.StateSimple;
import fun.other.Component;
import fun.other.ImplElementary;
import fun.other.Named;
import fun.other.Namespace;
import fun.other.RizzlyFile;
import fun.type.base.EnumType;
import fun.type.composed.UnionType;

public class KnowFunChild extends KnowledgeEntry {
  private static final KnowFunChildTraverser kct = new KnowFunChildTraverser();

  @Override
  public void init(KnowledgeBase base) {
  }

  public Named get(Named root, Iterable<String> path) {
    for (String child : path) {
      root = get(root, child);
    }
    return root;
  }

  public Named get(Fun sub, String name) {
    return getOrFind(sub, name, true);
  }

  public Named find(Fun sub, String name) {
    return getOrFind(sub, name, false);
  }

  private Named getOrFind(Fun sub, String name, boolean raiseError) {
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
    Set<Named> rset = new HashSet<Named>();
    addIfFound(obj.getElement().find(param), rset);
    return rset;
  }

  @Override
  protected Set<Named> visitImplElementary(ImplElementary obj, String param) {
    Set<Named> rset = new HashSet<Named>();
    addIfFound(obj.getConstant().find(param), rset);
    addIfFound(obj.getVariable().find(param), rset);
    addIfFound(obj.getFunction().find(param), rset);
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
    addIfFound(obj.getItemList().find(param), rset);
    return rset;
  }

  @Override
  protected Set<Named> visitStateSimple(StateSimple obj, String param) {
    return new HashSet<Named>();
  }

  @Override
  protected Set<Named> visitStateComposite(StateComposite obj, String param) {
    return new HashSet<Named>();
  }

  @Override
  protected Set<Named> visitUnionType(UnionType obj, String param) {
    return retopt(obj.getElement().find(param));
  }

  @Override
  protected Set<Named> visitNamespace(Namespace obj, String param) {
    return retopt(obj.find(param));
  }

  private void addIfFound(Named item, Set<Named> rset) {
    if (item != null) {
      rset.add(item);
    }
  }

  @Override
  protected Set<Named> visitComponent(Component obj, String param) {
    Set<Named> rset = super.visitComponent(obj, param);
    addIfFound(obj.getIface().find(param), rset);
    return rset;
  }

  @Override
  protected Set<Named> visitRizzlyFile(RizzlyFile obj, String param) {
    Set<Named> rset = new HashSet<Named>();
    addIfFound(obj.getComp().find(param), rset);
    addIfFound(obj.getConstant().find(param), rset);
    addIfFound(obj.getFunction().find(param), rset);
    addIfFound(obj.getType().find(param), rset);
    return rset;
  }

}
