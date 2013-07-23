package fun.knowledge;

import common.Designator;
import common.ElementInfo;

import error.ErrorType;
import error.RError;
import fun.Fun;
import fun.other.Named;
import fun.other.Namespace;

public class KnowFun extends KnowledgeEntry {
  private KnowledgeBase base;

  @Override
  public void init(KnowledgeBase base) {
    this.base = base;
  }

  public Fun get(Designator ref, ElementInfo info) {
    Named named = base.getRoot();

    for (String itr : ref.toList()) {
      named = ((Namespace) named).find(itr);
      if (named == null) {
        RError.err(ErrorType.Error, info, "Namespace not found: " + ref);
      }
    }

    return named;
  }

  public Fun find(Designator ref) {
    KnowFunChild kc = base.getEntry(KnowFunChild.class);

    Named named = base.getRoot();

    for (String itr : ref.toList()) {
      named = (Named) kc.find(named, itr);
      // named = ((Namespace) named).find(itr);
      if (named == null) {
        return null;
      }
    }

    return named;
  }

}
