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

package fun.pass;

import pass.FunPass;

import common.Designator;

import fun.Fun;
import fun.NullTraverser;
import fun.doc.ComponentFilePrinter;
import fun.doc.CompositionGraphPrinter;
import fun.knowledge.KnowledgeBase;
import fun.other.Named;
import fun.other.Namespace;
import fun.other.RizzlyFile;
import fun.other.Template;
import fun.type.Type;

public class DocWriter extends FunPass {

  @Override
  public void process(Namespace root, KnowledgeBase kb) {
    ComponentFilePrinter.printCodeStyle(kb.getDebugDir());
    CompositionGraphPrinter.printStyle(kb.getDebugDir() + ComponentFilePrinter.CompositionStyleName);

    DocWriterWorker worker = new DocWriterWorker(kb);
    worker.traverse(root, new Designator());
  }

}

class DocWriterWorker extends NullTraverser<Void, Designator> {
  final private KnowledgeBase kb;

  public DocWriterWorker(KnowledgeBase kb) {
    super();
    this.kb = kb;
  }

  @Override
  protected Void visitDefault(Fun obj, Designator param) {
    throw new RuntimeException("not yet implemented: " + obj.getClass().getName());
  }

  @Override
  protected Void visitNamespace(Namespace obj, Designator param) {
    visitList(obj.getChildren(), param);
    return null;
  }

  @Override
  protected Void visit(Fun obj, Designator param) {
    if (obj instanceof Named) {
      param = new Designator(param, ((Named) obj).getName());
    }
    return super.visit(obj, param);
  }

  @Override
  protected Void visitRizzlyFile(RizzlyFile obj, Designator param) {
    ComponentFilePrinter printer = new ComponentFilePrinter(kb);
    printer.createDoc(obj, param);
    printer.makeSource(obj);
    printer.makePicture(obj);
    printer.print(obj, param);
    return null;
  }

  @Override
  protected Void visitType(Type obj, Designator param) {
    return null;
  }

  @Override
  protected Void visitTemplate(Template obj, Designator param) {
    return null;
  }

}
