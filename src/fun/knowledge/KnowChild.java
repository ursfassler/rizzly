package fun.knowledge;

import java.util.HashSet;
import java.util.Set;

import error.ErrorType;
import error.RError;
import fun.DefTraverser;
import fun.Fun;
import fun.expression.reference.Reference;
import fun.other.FunList;
import fun.other.Named;
import fun.other.Template;
import fun.variable.CompUse;

public class KnowChild extends KnowledgeEntry {

  @Override
  public void init(KnowledgeBase base) {
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

  public FunList<Named> all(Fun obj) {
    KnowFunChildTraverser kct = new KnowFunChildTraverser();
    kct.traverse(obj, null);
    return kct.getList();
  }

  private Fun getOrFind(Fun sub, String name, boolean raiseError) {
    KnowFunChildTraverser kct = new KnowFunChildTraverser();
    kct.traverse(sub, null);

    Set<Named> rset = new HashSet<Named>();
    for (Named itr : kct.getList()) {
      if (itr.getName().equals(name)) {
        rset.add(itr);
      }
    }

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

enum KfctState {
  AddChildren, AddThis
}

class KnowFunChildTraverser extends DefTraverser<Void, KfctState> {
  final private FunList<Named> list = new FunList<Named>();

  public FunList<Named> getList() {
    return list;
  }

  @Override
  public Void traverse(Fun obj, KfctState param) {
    assert (param == null);
    super.traverse(obj, KfctState.AddChildren);
    return null;
  }

  @Override
  protected Void visit(Fun obj, KfctState param) {
    assert (param != null);
    switch (param) {
      case AddChildren:
        super.visit(obj, KfctState.AddThis);
        break;
      case AddThis:
        if (obj instanceof Named) {
          list.add((Named) obj);
        }
        break;
      default:
        throw new RuntimeException("Unhandled state: " + param);
    }
    return null;
  }

  @Override
  protected Void visitDeclaration(Template obj, KfctState param) {
    visitList(obj.getTempl(), param);
    visit(obj.getObject(), KfctState.AddChildren);
    return null;
  }

  @Override
  protected Void visitCompUse(CompUse obj, KfctState param) {
    visit(obj.getType(), KfctState.AddChildren);
    return null;
  }

  @Override
  protected Void visitReference(Reference obj, KfctState param) {
    assert (obj.getOffset().isEmpty());
    visit(obj.getLink(), KfctState.AddChildren);
    return null;
  }

}
