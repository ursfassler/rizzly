package fun.doc;

import java.util.LinkedList;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import common.Designator;

import fun.Fun;
import fun.other.Named;

public class XmlWriter implements Writer {
  private Document doc;
  private Element root;
  private int newlines = 1;
  private LinkedList<Boolean> wroteSecSep = new LinkedList<Boolean>();

  public final static String CL_LINK = "link";
  public final static String CL_KEYWORD = "keyword";
  public final static String CL_COMMENT = "comment";
  public final static String extension = ".html";

  public XmlWriter(Element root) {
    super();
    this.doc = root.getOwnerDocument();
    this.root = root;
    wroteSecSep.push(true);
  }

  public void wr(String s) {
    newlines = 0;
    root.appendChild(doc.createTextNode(s));
    wroteSecSep.set(0, false);
  }

  /**
   * Write a comment
   * 
   * @param text
   */
  public void wc(String text) {
    newlines = 0;
    Element kw = doc.createElement("span");
    kw.setAttribute("class", CL_COMMENT);
    kw.appendChild(doc.createTextNode(text));
    root.appendChild(kw);
    wroteSecSep.set(0, false);
  }

  /**
   * Write a keyword
   * 
   * @param s
   */
  public void kw(String s) {
    newlines = 0;
    Element kw = doc.createElement("span");
    kw.setAttribute("class", CL_KEYWORD);
    kw.appendChild(doc.createTextNode(s));
    root.appendChild(kw);
    wroteSecSep.set(0, false);
  }

  public void wl(Named dst, String title, Designator file) {
    newlines = 0;
    Element kw = doc.createElement("a");
    kw.setAttribute("class", CL_LINK);
    kw.setAttribute("title", title);
    kw.setAttribute("href", file + extension + "#" + getId(dst));
    kw.appendChild(doc.createTextNode(dst.getName()));
    root.appendChild(kw);
    wroteSecSep.set(0, false);
  }

  public void wa(Named obj) {
    newlines = 0;
    Element kw = doc.createElement("a");
    kw.setAttribute("name", getId(obj));
    kw.appendChild(doc.createTextNode(obj.getName()));
    root.appendChild(kw);
    wroteSecSep.set(0, false);
  }

  public void nl() {
    if (newlines < 1) {
      root.appendChild(doc.createElement("br"));
      newlines++;
    } else {
      // assert (newlines == 0); // only for debugging
    }
  }

  public void sectionSeparator() {
    if (!wroteSecSep.peek()) {
      // assert (newlines == 1); //TODO reimplement
      root.appendChild(doc.createElement("br"));
      newlines++;
      wroteSecSep.set(0, true);
    }
  }

  public void incIndent() {
    Element div = doc.createElement("div");
    root.appendChild(div);
    root = div;
    wr(" "); // FIXME all tested browsers have had problems with empty div tags (add indention); fix CSS or so
    wroteSecSep.push(true);
  }

  public void decIndent() {
    root = (Element) root.getParentNode();
    assert (root.getNodeName().equals("div"));
    wroteSecSep.poll();
  }

  private String getId(Fun obj) {
    return "_" + Integer.toHexString(obj.hashCode());
  }

}
