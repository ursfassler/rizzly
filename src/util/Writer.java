package util;

public interface Writer {
  /** Write plain text **/
  public void wr(String text);

  /** Write a keyword **/
  public void kw(String name);

  /** Write a comment **/
  public void wc(String text);

  /** Write a link */
  public void wl(String text, String hint, String file, String id);

  /** Write a link */
  public void wl(String text, String hint, String file);

  /** Write an anchor (link target) */
  public void wa(String name, String id);

  public void sectionSeparator();

  public void nl();

  public void incIndent();

  public void decIndent();

}
