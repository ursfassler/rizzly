package fun.doc;

import java.util.Collection;

import util.Pair;

import common.Designator;

import fun.knowledge.KnowledgeBase;
import fun.other.RizzlyFile;

public class DocWriter {

  static public void print(Collection<Pair<Designator, RizzlyFile>> files, KnowledgeBase kb) {
    ComponentFilePrinter.printCodeStyle(kb.getRootdir());
    CompositionGraphPrinter.printStyle(kb.getRootdir() + ComponentFilePrinter.CompositionStyleName);
    for (Pair<Designator, RizzlyFile> file : files) {
      ComponentFilePrinter printer = new ComponentFilePrinter(kb);
      printer.createDoc(file.second, file.first);
      printer.makeSource(file.second);
      printer.makePicture(file.second);
      printer.print(file.second, file.first);
    }
  }

}
