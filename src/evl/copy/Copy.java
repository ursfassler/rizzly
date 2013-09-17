package evl.copy;

import java.util.Collection;

import evl.Evl;

public final class Copy {

  public static <T extends Evl> T copy(T obj) {
    CopyEvl copier = new CopyEvl();
    T nobj = copier.copy(obj);
    Relinker.relink(nobj, copier.getCopied());
    return nobj;
  }

  public static <T extends Evl> Collection<T> copy(Collection<T> obj) {
    CopyEvl copier = new CopyEvl();
    Collection<T> nobj = copier.copy(obj);
    Relinker.relink(nobj, copier.getCopied());
    return nobj;
  }

}
