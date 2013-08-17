package common;

public class NameFactory {
  static private int nr = 0;

  static public synchronized String getNew() {
    nr++;
    return "_anon" + nr;
  }
}