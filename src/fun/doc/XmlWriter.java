package fun.doc;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Text;

import common.Designator;

import fun.Fun;
import fun.other.Named;

public class XmlWriter implements Writer {
  private Document doc;
  private Element root;
  private Text text = null;
  private int indent = 0;
  private boolean spaceAdded = false;

  public final static String CL_LINK = "link";
  public final static String CL_KEYWORD = "keyword";
  public static final String extension = ".html";

  public XmlWriter(Element root) {
    super();
    this.doc = root.getOwnerDocument();
    this.root = root;
  }

  public void append(String s) {
    if (text == null) {
      text = doc.createTextNode(s);
      root.appendChild(text);
    } else {
      text.setData(text.getData() + s);
    }
  }

  public void wr(String s) {
    if (!spaceAdded) {
      doIndent();
      spaceAdded = true;
    }
    append(s);
  }

  public void kw(String s) {
    if (!spaceAdded) {
      doIndent();
      spaceAdded = true;
    }
    text = null;
    Element kw = doc.createElement("span");
    kw.setAttribute("class", CL_KEYWORD);
    kw.appendChild(doc.createTextNode(s));
    root.appendChild(kw);
  }

  public void wl(Named dst, String title, Designator file) {
    if (!spaceAdded) {
      doIndent();
      spaceAdded = true;
    }
    text = null;
    Element kw = doc.createElement("a");
    kw.setAttribute("class", CL_LINK);
    kw.setAttribute("title", title);
    kw.setAttribute("href", file + extension + "#" + getId(dst));
    kw.appendChild(doc.createTextNode(dst.getName()));
    root.appendChild(kw);
  }

  public void wa(Named obj) {
    if (!spaceAdded) {
      doIndent();
      spaceAdded = true;
    }
    text = null;
    Element kw = doc.createElement("a");
    kw.setAttribute("name", getId(obj));
    kw.appendChild(doc.createTextNode(obj.getName()));
    root.appendChild(kw);
  }

  public void nl() {
    // root.appendChild(doc.createElement("br"));
    append("\n");
    spaceAdded = false;
  }

  public void emptyLine() {
    nl();
    nl(); // FIXME make it correct
  }

  private void doIndent() {
    for (int i = 0; i < indent; i++) {
      append("  ");
    }
  }

  public void incIndent() {
    indent++;
  }

  public void decIndent() {
    indent--;
  }

  private String getId(Fun obj) {
    return "_" + Integer.toHexString(obj.hashCode());
  }

}
