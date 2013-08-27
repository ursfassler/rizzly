/*
 * Part of upcompiler. Copyright (c) 2012, Urs Fässler, Licensed under the GNU Genera Public License, v3
 * @author: urs@bitzgi.ch
 */

package joGraph;

import java.util.List;

import org.apache.commons.lang.StringEscapeUtils;
import org.jgrapht.Graph;

abstract public class HtmlGraphWriter<T, E> {
  private Writer wr;
  private int rows = -1;
  private static final String[] style = { "solid", "dashed", "dotted", "invis", "bold", "tapered" };

  public void print(Graph<T, E> g) {
    wrHeader();
    for (T v : g.vertexSet()) {
      wrVertex(v);
    }
    for (E e : g.edgeSet()) {
      wrEdge(g.getEdgeSource(e), g.getEdgeTarget(e), 0);
    }
    wrFooter();
  }

  abstract protected void wrVertex(T v);

  protected void wrFooter() {
    wr.decIndent();
    wr.wr("}");
    wr.nl();
  }

  protected void wrRow(List<String> entries) {
    rowStart();
    for (String cell : entries) {
      wrCell(cell);
    }
    rowEnd();
  }

  protected void wrRow(String cell) {
    rowStart();
    wrCell(cell);
    rowEnd();
  }

  protected void wrCell(String cell) {
    wr.wr("<TD>");
    wr.wr(escape(cell));
    wr.wr("</TD>");
  }

  protected void rowStart() {
    rows++;
    wr.incIndent();
    wr.wr("<TR>");
  }

  protected void rowEnd() {
    wr.wr("</TR>");
    wr.nl();
    wr.decIndent();
  }

  protected void wrHeader() {
    wr.wr("digraph {");
    wr.nl();
    wr.incIndent();
    wr.wr("node [ shape=none ]");
    wr.nl();
    wr.nl();
  }

  public HtmlGraphWriter(Writer wr) {
    super();
    this.wr = wr;
  }

  protected void wrEdge(T src, T dst, int type) {
    wr.wr(getId(src));
    wr.wr(" -> ");
    wr.wr(getId(dst));
    wr.wr(" [style=\"");
    wr.wr(style[type]);
    wr.wr("\"]");
    wr.nl();
  }

  protected void wrVertexStart(T obj) {
    assert (rows == -1);
    rows = 0;
    wr.wr(getId(obj) + "[ ");
    wr.wr("label=<");
    wr.nl();
    wr.incIndent();
    wr.wr("<TABLE BORDER=\"0\" CELLBORDER=\"1\" CELLSPACING=\"0\" CELLPADDING=\"4\">");
    wr.nl();
  }

  protected void wrVertexEnd() {
    assert (rows >= 0);
    if (rows == 0) {
      wrRow("");
    }
    rows = -1;
    wr.wr("</TABLE>");
    wr.nl();
    wr.decIndent();
    wr.wr(">]; ");
    wr.nl();
  }

  private String escape(String text) {
    return StringEscapeUtils.escapeHtml(text);
  }

  private String getId(T obj) {
    return "_" + obj.hashCode();
  }

}
