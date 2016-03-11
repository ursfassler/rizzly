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

package ast.pass.input.xml.parser.meta;

import ast.meta.MetaListImplementation;
import ast.meta.SourcePosition;
import ast.pass.input.xml.infrastructure.XmlParseError;
import ast.pass.input.xml.scanner.ExpectionParser;
import error.ErrorType;
import error.RizzlyError;

public class SourcePositionParser implements MetaParser {
  private static final String Name = "SourcePosition";
  private final ExpectionParser stream;
  private final RizzlyError error;

  public SourcePositionParser(ExpectionParser stream, RizzlyError error) {
    this.stream = stream;
    this.error = error;
  }

  @Override
  public String getElementName() {
    return Name;
  }

  @Override
  public SourcePosition parse() {
    stream.elementStart(Name);
    String filename = stream.attribute("filename");
    String lineText = stream.attribute("line");
    String rowText = stream.attribute("row");
    stream.elementEnd();

    int line = toInt(lineText);
    int row = toInt(rowText);

    return new SourcePosition(filename, line, row);
  }

  private int toInt(String value) {
    try {
      return Integer.parseInt(value);
    } catch (NumberFormatException e) {
      error.err(ErrorType.Error, "can not convert attribute to int: \"" + value + "\"", new MetaListImplementation());
      throw new XmlParseError();
    }
  }

}
