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

import ast.data.reference.Anchor;
import ast.pass.input.xml.infrastructure.ParserDispatcher;
import ast.pass.input.xml.infrastructure.ParsersImplementation;
import ast.pass.input.xml.infrastructure.XmlParser;
import ast.pass.input.xml.linker.LinkDummyRecorder;
import ast.pass.input.xml.scanner.ExpectionParser;
import error.RizzlyError;

public class AnchorParser extends ParserDispatcher {

  public AnchorParser(ExpectionParser stream, LinkDummyRecorder linkDummyRecorder, XmlParser parser, RizzlyError error) {
    super(Anchor.class, new ParsersImplementation(error), stream, parser, error);
    add(new UnlinkedAnchorParser(stream, parser, error));
    add(new LinkedAnchorParser(stream, linkDummyRecorder, parser, error));
  }

}
