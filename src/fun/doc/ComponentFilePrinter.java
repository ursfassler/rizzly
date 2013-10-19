package fun.doc;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.util.List;

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

import fun.composition.ImplComposition;
import fun.doc.compgraph.Positioning;
import fun.doc.compgraph.WorldComp;
import fun.generator.ComponentGenerator;
import fun.knowledge.KnowFunPath;
import fun.knowledge.KnowledgeBase;
import fun.other.Component;
import fun.other.RizzlyFile;

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

  public void write(RizzlyFile comp) {
    createDoc(comp);
    makeSource(comp);
    makePicture(comp);
    print(comp);
  }

  public void print(RizzlyFile comp) {
    Designator path = comp.getName();
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

  public void createDoc(RizzlyFile comp) {
    Designator path = comp.getName();

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

  public void makeSource(RizzlyFile comp) {
    Element title = doc.createElement("h2");
    title.appendChild(doc.createTextNode("Source"));
    body.appendChild(title);

    Element pre = doc.createElement("div");
    pre.setAttribute("class", "code");
    body.appendChild(pre);
    RXmlPrinter.print(comp, pre, kb);
  }

  public void makePicture(RizzlyFile file) {
    KnowFunPath kp = kb.getEntry(KnowFunPath.class);
    List<ComponentGenerator> compgens = file.getCompfunc().getItems(ComponentGenerator.class);
    for (ComponentGenerator compgen : compgens) {
      Component comp = compgen.getTemplate();
      if (comp instanceof ImplComposition) {
        Element title = doc.createElement("h2");
        title.appendChild(doc.createTextNode("Picture"));
        body.appendChild(title);

        Designator path = kp.get(compgen);
        WorldComp g = CompositionGraphMaker.make( path, compgen.getName(), (ImplComposition) comp, kb);
        Positioning.doPositioning(g);
        CompositionGraphPrinter pr = new CompositionGraphPrinter(doc);
        body.appendChild(pr.makeSvg(g));
      }
    }
  }

  public static void printCodeStyle(String path) {
    try {
      PrintStream wr = new PrintStream(path + CodeStyleName);
      wr.println(".code {");
      wr.println("  font-family: Sans-Serif;");
      wr.println("  border-width: 1px;");
      wr.println("  border-style: solid;");
      wr.println("  border-color: gray;");
      wr.println("}");
      wr.println();
      wr.println(".code div {");
      wr.println("  padding-left: 1em;");
      wr.println("}");
      wr.println();
      wr.println("." + XmlWriter.CL_KEYWORD + " {");
      wr.println("  font-weight:bold;");
      wr.println("  color:darkred;");
      wr.println("}");
      wr.println();
      wr.println("." + XmlWriter.CL_COMMENT + " {");
      wr.println("  color:green;");
      wr.println("}");
      wr.println();
      wr.println("a[href] {");
      wr.println("  text-decoration: none;");
      wr.println("  color: darkblue;");
      wr.println("}");
      wr.println();
      wr.close();
    } catch (FileNotFoundException e) {
      e.printStackTrace();
    }

  }
}
