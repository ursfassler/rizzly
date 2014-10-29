package evl.copy;

import evl.Evl;
import evl.other.EvlList;

public final class Copy {

  public static <T extends Evl> T copy(T obj) {
    CopyEvl copier = new CopyEvl();
    T nobj = copier.copy(obj);
    Relinker.relink(nobj, copier.getCopied());
    return nobj;
  }

  public static <T extends Evl> EvlList<T> copy(EvlList<T> obj) {
    CopyEvl copier = new CopyEvl();
    EvlList<T> nobj = copier.copy(obj);
    Relinker.relink(nobj, copier.getCopied());
    return nobj;
  }

}
