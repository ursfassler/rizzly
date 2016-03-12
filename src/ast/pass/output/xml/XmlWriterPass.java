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

package ast.pass.output.xml;

import java.io.FileNotFoundException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.Collection;

import javax.xml.stream.XMLStreamException;

import ast.data.Ast;
import ast.data.Namespace;
import ast.knowledge.KnowledgeBase;
import ast.pass.AstPass;
import ast.pass.output.xml.visitor.IdWriter;
import ast.pass.output.xml.visitor.Write;
import ast.repository.query.IdForReferenced.IdReaderFactory;
import ast.visitor.VisitExecutor;
import ast.visitor.VisitExecutorImplementation;
import ast.visitor.Visitor;
import main.Configuration;

public class XmlWriterPass implements AstPass {
  private final String filename;

  @Deprecated
  public XmlWriterPass(Configuration configuration) {
    filename = configuration.getRootPath() + configuration.getNamespace() + ".xml";
  }

  public XmlWriterPass(String filename) {
    this.filename = filename;
  }

  @Override
  public void process(Namespace ast, KnowledgeBase kb) {
    Collection<String> xmlNamespaces = getXmlNamespaces(ast);

    try {
      OutputStream stream = new PrintStream(filename);
      XmlFileWriter writer = new XmlFileWriter(stream);
      write(ast, writer, xmlNamespaces);
    } catch (FileNotFoundException e1) {
      e1.printStackTrace();
    } catch (XMLStreamException e) {
      e.printStackTrace();
    }
  }

  private void write(Namespace ast, XmlFileWriter writer, Collection<String> xmlNamespaces) {
    VisitExecutor executor = VisitExecutorImplementation.instance();
    IdReader astId = (new IdReaderFactory()).produce(ast);

    writer.beginNode("rizzly");
    writeXmlNamespaces(xmlNamespaces, writer);

    Write visitor = new Write(writer, astId, new IdWriter(writer, astId), executor);
    executor.visit(visitor, ast.children);
    writer.endNode();
  }

  private void writeXmlNamespaces(Collection<String> xmlNamespaces, XmlFileWriter writer) {
    for (String namespace : xmlNamespaces) {
      String prefix = NamespacePrefix.get(namespace);
      writer.writeNamespacePrefix(prefix, namespace);
    }
  }

  private Collection<String> getXmlNamespaces(Namespace ast) {
    // TODO simplify
    IdReader idReader = new IdReader() {
      @Override
      public boolean hasId(Ast item) {
        return true;
      }

      @Override
      public String getId(Ast item) {
        return "";
      }
    };
    Visitor idWriter = new Visitor() {
    };
    VisitExecutor executor = VisitExecutorImplementation.instance();

    NamespaceCollector collector = new NamespaceCollector();

    Write visitor = new Write(collector, idReader, idWriter, executor);
    executor.visit(visitor, ast);

    return collector.getNamespaces();
  }
}
