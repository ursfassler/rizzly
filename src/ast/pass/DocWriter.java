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

package ast.pass;

import pass.AstPass;
import ast.data.Ast;
import ast.data.Named;
import ast.data.Namespace;
import ast.data.file.RizzlyFile;
import ast.data.template.Template;
import ast.data.type.Type;
import ast.doc.ComponentFilePrinter;
import ast.doc.CompositionGraphPrinter;
import ast.knowledge.KnowledgeBase;
import ast.traverser.NullTraverser;

import common.Designator;

public class DocWriter extends AstPass {

  @Override
  public void process(ast.data.Namespace root, KnowledgeBase kb) {
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
  protected Void visitDefault(Ast obj, Designator param) {
    throw new RuntimeException("not yet implemented: " + obj.getClass().getName());
  }

  @Override
  protected Void visitNamespace(Namespace obj, Designator param) {
    visitList(obj.children, param);
    return null;
  }

  @Override
  protected Void visit(Ast obj, Designator param) {
    if (obj instanceof Named) {
      param = new Designator(param, ((Named) obj).name);
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
