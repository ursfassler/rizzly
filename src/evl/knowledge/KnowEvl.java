package evl.knowledge;

import common.Designator;
import common.ElementInfo;

import error.ErrorType;
import error.RError;
import evl.Evl;
import evl.other.Named;
import evl.other.Namespace;

public class KnowEvl extends KnowledgeEntry {
  private KnowledgeBase base;

  @Override
  public void init(KnowledgeBase base) {
    this.base = base;
  }

  public Evl get(Designator ref, ElementInfo info) {
    Named named = base.getRoot();

    for (String itr : ref.toList()) {
      named = ((Namespace) named).find(itr);
      if (named == null) {
        RError.err(ErrorType.Error, info, "Namespace not found: " + ref);
      }
    }

    return named;
  }

}
