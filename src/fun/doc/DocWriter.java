package fun.doc;

import fun.knowledge.KnowledgeBase;
import fun.other.RizzlyFile;

public class DocWriter {

  static public void print(Iterable<RizzlyFile> fileList, KnowledgeBase kb) {
    ComponentFilePrinter.printCodeStyle(kb.getRootdir());
    CompositionGraphPrinter.printStyle(kb.getRootdir() + ComponentFilePrinter.CompositionStyleName);
    for (RizzlyFile file : fileList) {
      ComponentFilePrinter printer = new ComponentFilePrinter(kb);
      printer.createDoc(file);
      printer.makeSource(file);
      printer.makePicture(file);
      printer.print(file);
    }
  }

}
