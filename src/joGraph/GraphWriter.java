/*
 * Part of upcompiler. Copyright (c) 2012, Urs Fässler, Licensed under the GNU Genera Public License, v3
 * @author: urs@bitzgi.ch
 */

package joGraph;

import org.apache.commons.lang.StringEscapeUtils;

abstract public class GraphWriter {
  private Writer wr;
  private static final String[] style = { "solid", "dashed", "dotted", "invis", "bold", "tapered" };

  protected void wrFooter() {
    wr.decIndent();
    wr.wr("}");
    wr.nl();
  }

  protected void wrHeader(Object func) {
    wr.wr("digraph \"");
    wr.wr(NumPrint.toString(func.hashCode()));
    wr.wr("\" {");
    wr.nl();
    wr.incIndent();
    wr.wr("node [ shape=rect ]");
    wr.nl();
    wr.nl();
  }

  public GraphWriter(Writer wr) {
    super();
    this.wr = wr;
  }

  protected void wrEdge(Object src, Object dst, int type) {
    wr.wr(getId(src));
    wr.wr(" -> ");
    wr.wr(getId(dst));
    wr.wr(" [style=\"");
    wr.wr(style[type]);
    wr.wr("\"]");
    wr.nl();
  }

  protected void wrVertex(Object obj) {
    wr.wr(getId(obj) + "[ ");
    wr.wr("label=\"");
    wr.wr(obj.getClass().getSimpleName());
    wr.wr(": ");
    wr.wr(escape(obj.toString()));
    wr.wr("\"]; ");
    wr.nl();
  }

  private String escape(String text) {
    return StringEscapeUtils.escapeHtml(text);
  }

  private String getId(Object obj) {
    return "_" + obj.hashCode();
  }

}
