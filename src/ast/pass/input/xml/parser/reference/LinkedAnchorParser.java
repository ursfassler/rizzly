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

package ast.pass.input.xml.parser.reference;

import ast.data.Ast;
import ast.data.reference.LinkedAnchor;
import ast.pass.input.xml.infrastructure.Parser;
import ast.pass.input.xml.infrastructure.XmlParser;
import ast.pass.input.xml.linker.LinkDummy;
import ast.pass.input.xml.linker.LinkDummyRecorder;
import ast.pass.input.xml.scanner.ExpectionParser;
import error.RizzlyError;

public class LinkedAnchorParser implements Parser {
  private static final String Name = "LinkedAnchor";
  private final ExpectionParser stream;
  private final LinkDummyRecorder linkDummyRecorder;
  private final XmlParser parser;
  private final RizzlyError error;

  public LinkedAnchorParser(ExpectionParser stream, LinkDummyRecorder linkDummyRecorder, XmlParser parser, RizzlyError error) {
    this.stream = stream;
    this.linkDummyRecorder = linkDummyRecorder;
    this.parser = parser;
    this.error = error;
  }

  @Override
  public Parser parserFor(String name) {
    return Name.equals(name) ? this : null;
  }

  @Override
  public Parser parserFor(Class<? extends Ast> type) {
    return type == LinkedAnchor.class ? this : null;
  }

  @Override
  public LinkedAnchor parse() {
    stream.elementStart(Name);
    String linkId = stream.attribute("link");
    stream.elementEnd();

    LinkDummy link = new LinkDummy();
    link.setName(linkId);

    linkDummyRecorder.add(link);

    return new LinkedAnchor(link);
  }

}
