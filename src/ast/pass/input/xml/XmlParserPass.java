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

package ast.pass.input.xml;

import java.io.FileNotFoundException;
import java.io.FileReader;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import ast.copy.Relinker;
import ast.data.Namespace;
import ast.knowledge.KnowledgeBase;
import ast.pass.AstPass;
import ast.pass.input.xml.infrastructure.ParsersImplementation;
import ast.pass.input.xml.infrastructure.XmlParserImplementation;
import ast.pass.input.xml.linker.XmlIdMatcher;
import ast.pass.input.xml.parser.RizzlyFileParser;
import ast.pass.input.xml.parser.XmlTopParser;
import ast.pass.input.xml.parser.expression.ExpressionParser;
import ast.pass.input.xml.parser.function.ProcedureParser;
import ast.pass.input.xml.parser.reference.AnchorParser;
import ast.pass.input.xml.parser.reference.RefItemParser;
import ast.pass.input.xml.parser.reference.ReferenceParser;
import ast.pass.input.xml.parser.statement.StatementParser;
import ast.pass.input.xml.parser.type.TypeParser;
import ast.pass.input.xml.parser.variable.VariableParser;
import ast.pass.input.xml.scanner.ExpectionParser;
import ast.pass.input.xml.scanner.ExpectionParserImplementation;
import ast.pass.input.xml.scanner.XmlFileReader;
import ast.pass.input.xml.scanner.XmlToken;
import error.RError;
import error.RizzlyError;
import parser.PeekNReader;
import parser.TokenReader;

public class XmlParserPass implements AstPass {
  private final RizzlyError error = RError.instance();
  private final String filename;

  public XmlParserPass(String filename) {
    this.filename = filename;
  }

  public String getFilename() {
    return filename;
  }

  @Override
  public void process(Namespace ast, KnowledgeBase kb) {
    TokenReader<XmlToken> stream = xmlReader(getFilename());
    PeekNReader<XmlToken> peekReader = new PeekNReader<XmlToken>(stream);
    ExpectionParser expect = new ExpectionParserImplementation(peekReader, error);
    XmlParserImplementation parser = new XmlParserImplementation(expect, new ParsersImplementation(error), error);
    XmlIdMatcher matcher = new XmlIdMatcher(error);
    addParsers(parser, expect, matcher);
    XmlTopParser topParser = new XmlTopParser(expect, parser, error);

    ast.children.clear();
    Namespace ns = topParser.parse();
    Relinker.relink(ns, matcher.getMapping());
    ast.children.addAll(ns.children);
  }

  private void addParsers(XmlParserImplementation xmlParser, ExpectionParser stream, XmlIdMatcher matcher) {
    xmlParser.add(new RizzlyFileParser(stream, error));
    xmlParser.add(new VariableParser(stream, matcher, xmlParser, error));
    xmlParser.add(new ReferenceParser(stream, xmlParser, error));
    xmlParser.add(new AnchorParser(stream, matcher, xmlParser, error));
    xmlParser.add(new RefItemParser(stream, xmlParser, error));
    xmlParser.add(new ExpressionParser(stream, xmlParser, error));
    xmlParser.add(new TypeParser(stream, matcher, xmlParser, error));
    xmlParser.add(new ProcedureParser(stream, matcher, xmlParser, error));
    xmlParser.add(new StatementParser(stream, xmlParser, error));
  }

  private TokenReader<XmlToken> xmlReader(String filename) {
    XMLInputFactory factory = XMLInputFactory.newInstance();

    try {
      XMLStreamReader streamReader = factory.createXMLStreamReader(new FileReader(filename));
      return new XmlFileReader(streamReader, filename);
    } catch (FileNotFoundException e1) {
      e1.printStackTrace();
    } catch (XMLStreamException e1) {
      e1.printStackTrace();
    }

    return null;
  }

}
