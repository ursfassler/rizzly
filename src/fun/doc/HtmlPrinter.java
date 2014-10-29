package fun.doc;

import org.w3c.dom.Element;

import util.HtmlWriter;
import util.Writer;

import common.Designator;

import fun.Fun;
import fun.knowledge.KnowFunPath;
import fun.knowledge.KnowledgeBase;
import fun.other.Named;

public class HtmlPrinter extends FunPrinter {
  private KnowFunPath kfp;

  public HtmlPrinter(Writer xw, KnowledgeBase kb) {
    super(xw);
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

    // TODO verify
    // RizzlyFile file = kff.get(obj);
    //
    // assert (file != null);
    // assert (fullpath.size() >= file.getFullName().size());
    //
    // Designator locpath = new Designator(fullpath.toList().subList(file.getFullName().size(), fullpath.size()));
    // locpath = new Designator(locpath, obj.getName());
    //
    Designator locpath = new Designator(fullpath, obj.getName());

    return locpath.toString();
  }

}
