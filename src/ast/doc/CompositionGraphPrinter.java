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

package ast.doc;

import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.util.ArrayList;

import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import ast.Designator;
import ast.doc.compgraph.Component;
import ast.doc.compgraph.Connection;
import ast.doc.compgraph.Interface;
import ast.doc.compgraph.PointF;
import ast.doc.compgraph.SubComponent;
import ast.doc.compgraph.Vertex;
import ast.doc.compgraph.WorldComp;

//DOM

public class CompositionGraphPrinter {
  private static final int SCALE = 2;
  private static final String WorldCompClassName = "World";
  private static final String SubCompClassName = "Component";
  private static final String InterfaceClassName = "Interface";
  private static final String NameClassName = "Name";
  private static final String ConnectorClassName = "Connector";
  public static final String xlinkNs = "http://www.w3.org/1999/xlink";
  public static final String svgNs = "http://www.w3.org/2000/svg";
  private static final String linkExtension = ".html";
  final private Document doc;

  public CompositionGraphPrinter(Document doc) {
    super();
    this.doc = doc;
  }

  public Element makeSvg(WorldComp comp) {
    // Root element.
    Element root = createElement("svg");
    root.setAttribute("xmlns", svgNs);
    // root.setAttribute("version", "1.1");
    // root.setAttribute("baseProfile", "tiny");

    root.setAttribute("width", Double.toString((comp.getSize().x + 2) * SCALE));
    root.setAttribute("height", Double.toString((comp.getSize().y + 2) * SCALE));
    // root.setAttribute("width", Integer.toString(comp.getSize().x + 2));
    // root.setAttribute("height", Integer.toString(comp.getSize().y + 2));
    root.setAttribute("viewBox", "-1 -1 " + (comp.getSize().x + 1) + " " + (comp.getSize().y + 1));

    root.appendChild(makeComponent(comp, 55));
    for (Component sub : comp.getComp()) {
      root.appendChild(makeComponent(sub, 35));
    }
    for (Connection con : comp.getConn()) {
      root.appendChild(makeConnection(con));
    }

    return root;
  }

  private Element createElement(String string) {
    return doc.createElement(string);
    // Element node = doc.createElementNS(svgNs, string);
    // node.setPrefix("svg");
    // return node;
  }

  private Element makeConnection(Connection con) {
    PointF src = con.getSrc().getOwner().getSrcPort(con);
    PointF dst = con.getDst().getOwner().getDstPort(con);

    Element line = createElement("path");
    String path = "M";

    path += " " + src.x + "," + src.y;

    for (Vertex v : con.getVias()) {
      path += " " + v.getPos().x + "," + v.getPos().y;
    }

    path += " " + dst.x + "," + dst.y;

    line.setAttribute("d", path);
    line.setAttribute("class", ConnectorClassName);

    return line;
  }

  private Element makeComponent(Component comp, int ifaceOffset) {
    Element top = createElement("g");
    movePos(top, comp.getPos());

    Element bg = createElement("rect");
    bg.setAttribute("width", Double.toString(comp.getSize().x - 20));
    bg.setAttribute("height", Double.toString(comp.getSize().y));
    bg.setAttribute("x", Double.toString(-comp.getSize().x / 2 + 10));
    bg.setAttribute("y", "0");
    bg.setAttribute("rx", "10");
    bg.setAttribute("ry", "10");
    top.appendChild(bg);

    if (comp instanceof WorldComp) {
      bg.setAttribute("class", WorldCompClassName);
      addText(top, 9, comp.getClassname());
    } else {
      bg.setAttribute("class", SubCompClassName);
      addText(top, 9, ((SubComponent) comp).getInstname());
      addText(top, 18, comp.getClassname(), comp.getPath());
    }

    makeIfaces(comp.getInput(), Interface.WIDTH / 2 - (comp.getSize().x / 2), ifaceOffset, top);
    makeIfaces(comp.getOutput(), (comp.getSize().x / 2) - Interface.WIDTH / 2, ifaceOffset, top);

    return top;
  }

  private void makeIfaces(ArrayList<Interface> ifaces, double x, int ifaceOffset, Element top) {
    int nr = 0;
    for (Interface iface : ifaces) {
      Element sif = makeIface(iface);
      movePos(sif, new PointF(x, ifaceOffset + nr * Component.Y_IFACE_DIST));
      top.appendChild(sif);
      nr++;
    }
  }

  private Element makeIface(Interface iface) {
    Element top = createElement("g");

    Element bg = createElement("rect");
    bg.setAttribute("width", Double.toString(Interface.WIDTH));
    bg.setAttribute("height", Double.toString(Interface.HEIGHT));
    bg.setAttribute("x", Double.toString(-Interface.WIDTH / 2));
    bg.setAttribute("y", Double.toString(-Interface.HEIGHT / 2));
    bg.setAttribute("class", InterfaceClassName);
    top.appendChild(bg);

    addText(top, 3, iface.getInstname());

    return top;
  }

  private void addText(Element bg, int top, String text) {
    Element t = createElement("text");
    t.setAttribute("x", "0");
    t.setAttribute("y", Integer.toString(top));
    t.setAttribute("class", NameClassName);
    bg.appendChild(t);

    t.appendChild(doc.createTextNode(text));
  }

  private void addText(Element bg, int top, String text, Designator designator) {
    Element a = createElement("a");
    Attr attr = doc.createAttributeNS(xlinkNs, "href");
    attr.setNodeValue(designator.toString(".") + linkExtension + "#" + text);
    attr.setPrefix("xlink");
    a.setAttributeNode(attr);
    addText(a, top, text);
    bg.appendChild(a);
  }

  static private void movePos(Element top, PointF pos) {
    top.setAttribute("transform", "translate(" + pos.x + "," + pos.y + ")");
  }

  static public void printStyle(String filename) {
    try {
      PrintStream wr = new PrintStream(filename);

      wr.println("." + WorldCompClassName + " {");
      wr.println("  stroke: black;");
      wr.println("  fill:   #ccccff;");
      wr.println("}");
      wr.println("." + SubCompClassName + " {");
      wr.println("  stroke: black;");
      wr.println("  fill:   #f2f2ff;");
      wr.println("}");
      wr.println("." + InterfaceClassName + " {");
      wr.println("  stroke: black;");
      wr.println("  fill: #fffa99;");
      wr.println("}");
      wr.println("." + NameClassName + " {");
      wr.println("  font-family:sans-serif;");
      wr.println("  text-anchor:middle;");
      wr.println("  font-size:8px;");
      wr.println("}");
      wr.println("." + ConnectorClassName + " {");
      wr.println("  stroke:black;");
      wr.println("  fill:none;");
      wr.println("  stroke-linejoin:round;");
      wr.println("  stroke-width:2;");
      wr.println("}");
      wr.println(".Connector:hover {");
      wr.println("  stroke:red;");
      wr.println("  stroke-width:5;");
      wr.println("}");

      wr.close();
    } catch (FileNotFoundException e) {
      e.printStackTrace();
    }
  }

}
