package evl.doc;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import common.Designator;

import evl.composition.ImplComposition;
import evl.doc.compgraph.Positioning;
import evl.doc.compgraph.WorldComp;
import evl.knowledge.KnowledgeBase;
import evl.other.Named;

public class ComponentFilePrinter {
  private Document doc;
  private Element body;
  private KnowledgeBase kb;
  public final static String CodeStyleName = "codestyle.css";
  public final static String CompositionStyleName = "composition.css";

  public ComponentFilePrinter(KnowledgeBase kb) {
    super();
    this.kb = kb;
  }

  public void write(Designator path, Named comp) {
    createDoc(path, comp);
    makeSource(path, comp);
    makePicture(path, comp);
    print(path, comp);
  }

  public void print(Designator path, Named comp) {
    path = new Designator(path, comp.getName());
    try {
      String filename = kb.getRootdir() + path.toString(".") + ".html";
      TransformerFactory transformerFactory = TransformerFactory.newInstance();
      Transformer transformer;
      transformer = transformerFactory.newTransformer();
      DOMSource source = new DOMSource(doc);
      StreamResult result = new StreamResult(new File(filename));

      transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
      transformer.setOutputProperty(OutputKeys.INDENT, "yes");
      transformer.setOutputProperty(OutputKeys.DOCTYPE_SYSTEM, "http://www.w3.org/2002/04/xhtml-math-svg/xhtml-math-svg.dtd");
      transformer.setOutputProperty(OutputKeys.DOCTYPE_PUBLIC, "-//W3C//DTD XHTML 1.1 plus MathML 2.0 plus SVG 1.1//EN");

      transformer.transform(source, result);
    } catch (TransformerConfigurationException e) {
      e.printStackTrace();
    } catch (TransformerException e) {
      e.printStackTrace();
    }
  }

  public void createDoc(Designator path, Named comp) {
    path = new Designator(path, comp.getName());

    try {
      DocumentBuilderFactory dbfac = DocumentBuilderFactory.newInstance();
      DocumentBuilder docBuilder;
      docBuilder = dbfac.newDocumentBuilder();
      doc = docBuilder.newDocument();

      // Root element.
      Element root = doc.createElement("html");
      root.setAttribute("xmlns", "http://www.w3.org/1999/xhtml");

      Element head = doc.createElement("head");
      root.appendChild(head);
      Element title = doc.createElement("title");
      title.appendChild(doc.createTextNode(path.toString()));
      head.appendChild(title);
      Element meta = doc.createElement("meta");
      head.appendChild(meta);
      meta.setAttribute("http-equiv", "Content-Type");
      meta.setAttribute("content", "text/html;charset=utf-8");

      addStyle(head, CodeStyleName);
      addStyle(head, CompositionStyleName);

      // head.appendChild(makeStyle());

      body = doc.createElement("body");
      root.appendChild(body);
      doc.appendChild(root);

      title = doc.createElement("h1");
      title.appendChild(doc.createTextNode(path.toString()));
      body.appendChild(title);
    } catch (ParserConfigurationException e) {
      e.printStackTrace();
    }
  }

  public void addStyle(Element head, String name) {
    Element style = doc.createElement("link");
    style.setAttribute("rel", "stylesheet");
    style.setAttribute("type", "text/css");
    style.setAttribute("href", name);
    head.appendChild(style);
  }

  public void makeSource(Designator path, Named comp) {
    Element title = doc.createElement("h2");
    title.appendChild(doc.createTextNode("Source"));
    body.appendChild(title);

    Element pre = doc.createElement("pre");
    body.appendChild(pre);
    RXmlPrinter.print(comp, pre, kb);
  }

  public void makePicture(Designator path, Named comp) {
    if (comp instanceof ImplComposition) {
      Element title = doc.createElement("h2");
      title.appendChild(doc.createTextNode("Picture"));
      body.appendChild(title);

      WorldComp g = CompositionGraphMaker.make((ImplComposition) comp, kb);
      Positioning.doPositioning(g);
      CompositionGraphPrinter pr = new CompositionGraphPrinter(doc);
      body.appendChild(pr.makeSvg(g));
    }
  }

  public static void printCodeStyle(String path) {
    try {
      PrintStream wr = new PrintStream(path + CodeStyleName);
      wr.println(".keyword {");
      wr.println("  font-weight:bold;");
      wr.println("  color:darkred;");
      wr.println("}");
      wr.println("pre {");
      wr.println("  border-width: 1px;");
      wr.println("  border-style: solid;");
      wr.println("  border-color: gray;");
      wr.println("}");
      wr.println("a[href] {");
      wr.println("  text-decoration: none;");
      wr.println("  color: darkblue;");
      wr.println("}");
      wr.close();
    } catch (FileNotFoundException e) {
      e.printStackTrace();
    }

  }
}
