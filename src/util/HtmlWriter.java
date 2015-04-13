/**
 *  This file is part of Rizzly.
 *
 *  Rizzly is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Rizzly is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with Rizzly.  If not, see <http://www.gnu.org/licenses/>.
 */

package util;

import java.util.LinkedList;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class HtmlWriter implements Writer {
  private Document doc;
  private Element root;
  private int newlines = 1;
  private LinkedList<Boolean> wroteSecSep = new LinkedList<Boolean>();

  public final static String CL_LINK = "link";
  public final static String CL_KEYWORD = "keyword";
  public final static String CL_COMMENT = "comment";
  public final static String extension = ".html";

  public HtmlWriter(Element root) {
    super();
    this.doc = root.getOwnerDocument();
    this.root = root;
    wroteSecSep.push(true);
  }

  @Override
  public void wr(String s) {
    newlines = 0;
    root.appendChild(doc.createTextNode(s));
    wroteSecSep.set(0, false);
  }

  @Override
  public void wc(String text) {
    newlines = 0;
    Element kw = doc.createElement("span");
    kw.setAttribute("class", CL_COMMENT);
    kw.appendChild(doc.createTextNode(text));
    root.appendChild(kw);
    wroteSecSep.set(0, false);
  }

  @Override
  public void kw(String s) {
    newlines = 0;
    Element kw = doc.createElement("span");
    kw.setAttribute("class", CL_KEYWORD);
    kw.appendChild(doc.createTextNode(s));
    root.appendChild(kw);
    wroteSecSep.set(0, false);
  }

  @Override
  public void wl(String text, String hint, String file, String id) {
    newlines = 0;
    Element kw = doc.createElement("a");
    kw.setAttribute("class", CL_LINK);
    kw.setAttribute("title", hint);
    kw.setAttribute("href", file + extension + "#" + id);
    kw.appendChild(doc.createTextNode(text));
    root.appendChild(kw);
    wroteSecSep.set(0, false);
  }

  @Override
  public void wl(String text, String hint, String file) {
    newlines = 0;
    Element kw = doc.createElement("a");
    kw.setAttribute("class", CL_LINK);
    kw.setAttribute("title", hint);
    kw.setAttribute("href", file + extension);
    kw.appendChild(doc.createTextNode(text));
    root.appendChild(kw);
    wroteSecSep.set(0, false);
  }

  @Override
  public void wa(String text, String id) {
    newlines = 0;
    Element kw = doc.createElement("a");
    kw.setAttribute("name", id);
    kw.appendChild(doc.createTextNode(text));
    root.appendChild(kw);
    wroteSecSep.set(0, false);
  }

  @Override
  public void nl() {
    if (newlines < 1) {
      root.appendChild(doc.createElement("br"));
      newlines++;
    } else {
      // assert (newlines == 0); // only for debugging
    }
  }

  @Override
  public void sectionSeparator() {
    if (!wroteSecSep.peek()) {
      // assert (newlines == 1); //TODO reimplement
      root.appendChild(doc.createElement("br"));
      newlines++;
      wroteSecSep.set(0, true);
    }
  }

  @Override
  public void incIndent() {
    Element div = doc.createElement("div");
    root.appendChild(div);
    root = div;
    wr(" "); // FIXME all tested browsers have had problems with empty div tags
             // (add indention); fix CSS or so
    wroteSecSep.push(true);
  }

  @Override
  public void decIndent() {
    root = (Element) root.getParentNode();
    assert (root.getNodeName().equals("div"));
    wroteSecSep.poll();
  }

}
