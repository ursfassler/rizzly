package fun.doc;

import org.w3c.dom.Element;

import util.HtmlWriter;
import util.Writer;

import common.Designator;

import fun.Fun;
import fun.expression.reference.Reference;
import fun.knowledge.KnowFunFile;
import fun.knowledge.KnowFunPath;
import fun.knowledge.KnowledgeBase;
import fun.other.Named;
import fun.other.RizzlyFile;

public class HtmlPrinter extends FunPrinter {
  private KnowFunFile kff;
  private KnowFunPath kfp;

  public HtmlPrinter(Writer xw, KnowledgeBase kb) {
    super(xw);
    kff = kb.getEntry(KnowFunFile.class);
    kfp = kb.getEntry(KnowFunPath.class);
  }

  public static void print(Fun ast, Element parent, KnowledgeBase kb) {
    HtmlPrinter pp = new HtmlPrinter(new HtmlWriter(parent), kb);
    pp.traverse(ast, null);
  }

  @Override
  protected String getId(Named obj) {
    Designator fullpath = kfp.find(obj);
    if ((fullpath == null) || (fullpath.size() == 0)) {
      // internal type or so
      return "_" + Integer.toHexString(obj.hashCode());
    }

    RizzlyFile file = kff.get(obj);

    assert (file != null);
    assert (fullpath.size() >= file.getFullName().size());

    Designator locpath = new Designator(fullpath.toList().subList(file.getFullName().size(), fullpath.size()));
    locpath = new Designator(locpath, obj.getName());

    return locpath.toString();
  }

  @Override
  protected Designator getObjPath(Reference obj) {
    RizzlyFile file = kff.find(obj.getLink()); // FIXME find better way to handle built in functions and so
    Designator path;
    if (file == null) {
      path = new Designator();
    } else {
      path = file.getFullName();
    }
    return path;
  }

}
