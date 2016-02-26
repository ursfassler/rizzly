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

package ast.pass.input.xml.parser.type;

import ast.data.type.Type;
import ast.pass.input.xml.infrastructure.ParserDispatcher;
import ast.pass.input.xml.infrastructure.ParsersImplementation;
import ast.pass.input.xml.infrastructure.XmlParser;
import ast.pass.input.xml.scanner.ExpectionParser;
import error.RizzlyError;

public class TypeParser extends ParserDispatcher {

  public TypeParser(ExpectionParser stream, XmlParser parser, RizzlyError error) {
    super(Type.class, new ParsersImplementation(error), stream, parser, error);
    add(new IntegerParser(stream, parser, error));
  }

}
