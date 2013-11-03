/*
 * Part of upcompiler. Copyright (c) 2012, Urs Fässler, Licensed under the GNU Genera Public License, v3
 * @author: urs@bitzgi.ch
 */

package joGraph;

public class NumPrint {
  public static String toString(long num) {
    String res = "";
    if (num < 0) {
      res += "-";
      num = -num;
    }
    res += "0x";
    res += Long.toHexString(num);
    return res;
  }
}
