package evl.doc;

import common.Designator;

import evl.Evl;
import evl.NullTraverser;
import evl.knowledge.KnowPath;
import evl.knowledge.KnowledgeBase;
import evl.other.Named;
import evl.other.Namespace;

public class DocWriter extends NullTraverser<Void, Void> {
  private KnowledgeBase kb;
  private KnowPath kp;

  public DocWriter(KnowledgeBase kb) {
    super();
    this.kb = kb;
    kp = kb.getEntry(KnowPath.class);
    ComponentFilePrinter.printCodeStyle(kb.getRootdir());
    CompositionGraphPrinter.printStyle(kb.getRootdir() + ComponentFilePrinter.CompositionStyleName);
  }

  static public void print(Namespace classes, KnowledgeBase kb) {
    DocWriter printer = new DocWriter(kb);
    printer.traverse(classes, null);
  }

  @Override
  protected Void visitDefault(Evl obj, Void param) {
    // throw new RuntimeException("not yet implemented: " + obj.getClass().getCanonicalName());
    assert (obj instanceof Named);
    Named named = (Named) obj;
    ComponentFilePrinter printer = new ComponentFilePrinter(kb);
    Designator path = kp.get(named);
    printer.createDoc(path, named);
    printer.makeSource(path, named);
    printer.makePicture(path, named);
    printer.print(path, named);
    return null;
  }

  @Override
  protected Void visitNamespace(Namespace obj, Void param) {
    visitItr(obj, param);
    return null;
  }

}
