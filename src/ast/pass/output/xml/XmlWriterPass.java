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

import javax.xml.stream.XMLStreamException;

import main.Configuration;
import ast.data.Namespace;
import ast.knowledge.KnowledgeBase;
import ast.pass.AstPass;
import ast.pass.output.xml.visitor.Write;

public class XmlWriterPass extends AstPass {

  public XmlWriterPass(Configuration configuration) {
    super(configuration);
  }

  @Override
  public void process(Namespace ast, KnowledgeBase kb) {
    String filename = configuration.getRootPath() + configuration.getNamespace() + ".xml";
    try {
      OutputStream stream = new PrintStream(filename);
      XmlFileWriter writer = new XmlFileWriter(stream);
      write(ast, writer);
    } catch (FileNotFoundException e1) {
      e1.printStackTrace();
    } catch (XMLStreamException e) {
      e.printStackTrace();
    }
  }

  private void write(Namespace ast, XmlFileWriter writer) {
    writer.beginNode("rizzly");
    Write visitor = new Write(writer);
    ast.children.accept(visitor);
    writer.endNode();
  }

}
